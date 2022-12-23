package advent

import advent.Day22.Direction.*
import advent.Day22.CubeFaceKey.*
import advent.Day22.CubeEdgeKey.*
import advent.Day22.Rotation.Clockwise
import advent.Day22.Rotation.Counterclockwise
import advent.Day22.Rotation.Half
import advent.Day22.Rotation.None
import kotlin.math.max
import kotlin.math.min

object Day22 : AdventDay {
    override val debugLevel: Int = 0

    fun runSample(
        tiles: List<String>,
        instLine: String,
        init: Triple<Int, Vector2, Int> = Triple(0, Vector2(0, 0), Direction.Right.ordinal)
    ): Triple<Int, Vector2, Int> {
        val cube = parseCube(tiles)
        val instructions = parseInstructions(instLine)
        val initialState = CubeState(CubePosition(cube.faces[init.first], init.second), Direction.values()[init.third])
        val finalState = instructions.fold(initialState) { acc, inst ->
            debugLn(acc.toString(cube), 2)
            cube.move(acc, inst)
        }
        return Triple(
            cube.faces.indexOf(finalState.position.face),
            finalState.position.location,
            finalState.orientation.ordinal
        )
    }

    override fun part1(input: List<String>): Any {
        val (board, instructions) = parseInput(input)
        debugLn("Starting: ${board.position+1}, ${orientationToString(board.orientation)}(${board.orientation})")
        val finalBoard = instructions.fold(board) { acc, instruction ->
            debug("Moving $instruction")
            val result = acc.move(instruction)
            debugLn(" to ${result.position+1}, ${orientationToString(result.orientation)}(${result.orientation})")
            result
        }
        debugLn("Final position: ${finalBoard.position+1}, orientation: ${orientationToString(finalBoard.orientation)}")
        return (finalBoard.position + 1).let { it.x * 4 + it.y * 1000 } + finalBoard.orientation.ordinal
    }

    override fun part2(input: List<String>): Any {
        val (cube, instructions) = parsePartTwo(input)
        val initialState = CubeState(CubePosition(cube.faces[0], Vector2(0, 0)), Right)
        debugLn("Executing ${instructions.size} instructions")
        val finalState = instructions.fold(initialState) { acc, inst ->
            debugLn(acc.toString(cube), 2)
            cube.move(acc, inst)
        }
//        println("Moves: $successfulMoves successful, $unsuccessfulMoves unsuccessful")
        debugLn("${cube.faces.indexOf(finalState.position.face)} ${finalState.position.location} ${finalState.orientation}(${finalState.orientation.ordinal})", 0)
        val finalPosition = finalState.position.face.faceLocationToRawLocation(finalState.position.location)
        return (finalPosition + 1).let { it.x * 4 + it.y * 1000 } + finalState.orientation.ordinal
    }

    private fun CubeState.toString(cube: Cube): String =
        "Face: ${cube.faces.indexOf(this.position.face)}, Pos: ${this.position.location}, Orientation: ${this.orientation}"

    private fun orientationToString(orientation: Direction): String = when (orientation) {
        Right -> "right"
        Down -> "down"
        Left -> "left"
        Up -> "up"
        else -> "ASDFSADFSAAFA"
    }

    private enum class Rotation(val oppositeIndex: Int, val rotateOrientation: (Int) -> Int) {
        Counterclockwise(1, { (it + 3) % 4}),
        Clockwise(0, { (it + 1) % 4}),
        Half(2, { (it + 2) % 4 }),
        None(3, { it });

        fun rotate(orientation: Int) = rotateOrientation(orientation)
        fun rotate(orientation: Direction) = Direction.values()[rotateOrientation(orientation.ordinal)]
        fun getOpposite(): Rotation = Rotation.values()[oppositeIndex]

        companion object {
            fun getRotation(from: Direction, to: Direction): Rotation =
                when (((from.ordinal - to.ordinal) + 4) % 4) {
                    0 -> None
                    1 -> Clockwise
                    2 -> Half
                    3 -> Counterclockwise
                    else -> throw RuntimeException("Invalid rotation: $from to $to")
                }
        }
    }

    private fun getRotation(from: String): Rotation = when (from) {
        "L" -> Counterclockwise
        "R" -> Clockwise
        else -> throw RuntimeException("Bad rotation: $from")
    }

    private sealed interface Instruction
    private data class Move(val distance: Int) : Instruction
    private data class Rotate(val rotation: Rotation) : Instruction

    private data class CubePosition(val face: Face, val location: Vector2)
    private data class CubeState(val position: CubePosition, val orientation: Direction)

    enum class CubeFaceKey(edges: () -> Map<Direction, CubeEdgeKey>) {
        TopFace({
            mapOf(
                Up to TopBack,
                Right to TopRight,
                Down to TopFront,
                Left to TopLeft
            )
        }),
        RightFace({
            mapOf(
                Up to TopRight,
                Right to BackRight,
                Down to BottomRight,
                Left to FrontRight
            )
        }),
        FrontFace({
            mapOf(
                Up to TopFront,
                Right to FrontRight,
                Down to BottomFront,
                Left to FrontLeft
            )
        }),
        LeftFace({
            mapOf(
                Up to TopLeft,
                Right to FrontLeft,
                Down to BottomLeft,
                Left to BackLeft
            )
        }),
        BackFace({
            mapOf(
                Up to TopBack,
                Right to BackLeft,
                Down to BottomBack,
                Left to BackRight
            )
        }),
        BottomFace({
            mapOf(
                Up to BottomFront,
                Right to BottomRight,
                Down to BottomBack,
                Left to BottomLeft
            )
        });

        val edges: Map<Direction, CubeEdgeKey> = edges()
        fun connectedFace(direction: Direction): Pair<CubeFaceKey, Direction> {
            val edge = edges[direction]!!
            val connectedFace = edge.connectedFace(this)
            return connectedFace to connectedFace.edges.entries.find { it.value == edge }!!.key
        }
        fun edgeDirection(edge: CubeEdgeKey): Direction = this.edges.entries.find { it.value == edge }!!.key
    }

    enum class CubeEdgeKey(lazyFaces: () -> Pair<CubeFaceKey, CubeFaceKey>) {
        TopBack({ TopFace to BackFace }),
        TopRight({ TopFace to RightFace }),
        TopFront({ TopFace to FrontFace }),
        TopLeft({ TopFace to LeftFace }),
        FrontRight({ FrontFace to RightFace }),
        FrontLeft({ FrontFace to LeftFace }),
        BackLeft({ BackFace to LeftFace }),
        BackRight({ BackFace to RightFace }),
        BottomBack({ BottomFace to BackFace }),
        BottomRight({ BottomFace to RightFace }),
        BottomFront({ BottomFace to FrontFace }),
        BottomLeft({ BottomFace to LeftFace });

        private val faces by lazy(lazyFaces)
        val first: CubeFaceKey by lazy { faces.first }
        val second: CubeFaceKey by lazy { faces.second }

        fun connectedFace(faceKey: CubeFaceKey): CubeFaceKey = if (this.first == faceKey) this.second else this.first
    }

    var successfulMoves = 0
    var unsuccessfulMoves = 0

    /**
     * Faces are as follows:
     * 0 -> Top
     * 1 -> Right
     * 2 -> Front
     * 3 -> Left
     * 4 -> Back
     * 5 -> Bottom
     */
    private data class Cube(val faces: List<Face>) {
        val dimension: Int = faces.first().let { it.bottomRight.x - it.topLeft.x }
        val edges: List<CubeEdge> = foldCubeAlongEdges(faces)

        fun edgeFor(faceEdge: FaceEdge): CubeEdge =
            edges.first { it.first == faceEdge || it.second == faceEdge }

        fun candidateMove(state: CubeState): CubeState {
            val position = state.position.location + state.orientation.vector
            debugLn("  Pos: $position, is on face: ${position.isOnFace()}", 2)
            return if (position.isOnFace()) {
                CubeState(CubePosition(state.position.face, position), state.orientation)
            } else {
                val currentFace = state.position.face
                val edge = edgeFor(currentFace.edge(state.orientation))
                val result = edge.transit(state)
                debugLn("  --> Transit from ${state.toString(this)} to ${result.toString(this)}", 2)
                result
            }
        }

        fun move(state: CubeState, instruction: Instruction): CubeState {
            debugLn("Executing instruction: $instruction", 2)
            return when (instruction) {
                is Rotate -> {
                    val orientation = instruction.rotation.rotate(state.orientation)
                    debugLn("  new orientation: $orientation", 2)
                    state.copy(orientation = orientation)
                }
                is Move -> {
                    var currentState = state
                    repeat(instruction.distance) {
                        val candidate = candidateMove(currentState)
                        debugLn("  Can move: ${!candidate.position.face.tiles[candidate.position.location]!!}", 2)
                        if (!candidate.position.face.tiles[candidate.position.location]!!) {
                            successfulMoves++
                            currentState = candidate
                        } else {
                            unsuccessfulMoves++
                        }
                    }
                    currentState
                }
            }
        }

        private fun Vector2.isOnFace(): Boolean = this.x in 0 until dimension && this.y in 0 until dimension
    }

    enum class Direction(private val oppositeOrdinal: Int, val vector: Vector2) {
        Right(2, Vector2(1, 0)),
        Down(3, Vector2(0, 1)),
        Left(0, Vector2(-1, 0)),
        Up(1, Vector2(0, -1));

        val opposite: Direction by lazy { Direction.values()[oppositeOrdinal] }
    }

    private data class Face(
        val faceCoordinate: Vector2,
        val topLeft: Vector2,
        val bottomRight: Vector2,
        val tiles: Map<Vector2, Boolean>
    ) {
        val dimension: Int = bottomRight.x - topLeft.x
        fun edge(direction: Direction): FaceEdge = edges[direction]!!
        private val edges: Map<Direction, FaceEdge> = Direction.values().associateWith { FaceEdge(this, it) }
        fun faceLocationToRawLocation(location: Vector2): Vector2 = location + topLeft
    }

    private data class CubeEdge(val key: CubeEdgeKey, val first: FaceEdge, val second: FaceEdge) {
        fun transit(origin: CubeState): CubeState {
            val currentFace = origin.position.face
            if (currentFace != first.face && currentFace != second.face)
                throw RuntimeException("Transiting from $origin across invalid edge $this")
            val reverse = currentFace == second.face
            val oldFaceEdge = if (reverse) second else first
            val newFaceEdge = if (reverse) first else second
            val newOrientation = newFaceEdge.side.opposite
            val leavingAt = when (origin.orientation) {
                Left, Right -> origin.position.location.y
                Up, Down -> origin.position.location.x
            }

            val max = newFaceEdge.face.dimension - 1
            val newPosition = when (newFaceEdge.side) {
                Left -> when (oldFaceEdge.side) {
                    Right, Up -> Vector2(0, leavingAt)
                    Left, Down -> Vector2(0, max - leavingAt)
                }
                Right -> when (oldFaceEdge.side) {
                    Left, Down -> Vector2(max, leavingAt)
                    Right, Up -> Vector2(max, max - leavingAt)
                }
                Up -> when (oldFaceEdge.side) {
                    Left, Down -> Vector2(leavingAt, 0)
                    Right, Up -> Vector2(max - leavingAt, 0)
                }
                Down -> when (oldFaceEdge.side)  {
                    Right, Up -> Vector2(leavingAt, max)
                    Left, Down -> Vector2(max - leavingAt, max)
                }
            }
            return CubeState(CubePosition(newFaceEdge.face, newPosition), newOrientation)
        }
    }

    /**
     *
     */
    private data class FaceEdge(val face: Face, val side: Direction)

    private fun findConnectedFacesBeforeFold(rawFaces: List<Face>): Map<Face, Map<Direction, Face>> {
        return rawFaces.associateWith { face ->
            Direction.values().map { face.faceCoordinate + it.vector to it }
                .mapNotNull { (coord, direction) ->
                    val otherFace = rawFaces.firstOrNull { otherFace -> otherFace.faceCoordinate == coord }
                    if (otherFace != null) {
                        otherFace to direction
                    } else null
                }.associate { (otherFace, direction) ->
                    direction to otherFace
                }

        }
    }

    private data class CubeFace(val face: Face, val edgeMap: Map<CubeEdgeKey, Direction>)
    private fun foldCubeAlongEdges(rawFaces: List<Face>): List<CubeEdge> {
        val connectedFaces = findConnectedFacesBeforeFold(rawFaces)
        val cubeFaces: MutableMap<CubeFaceKey, Pair<Face, Rotation>> = mutableMapOf()
        val incompleteEdges: MutableMap<CubeEdgeKey, FaceEdge> = mutableMapOf()
        val cubeEdges: MutableMap<CubeEdgeKey, CubeEdge> = mutableMapOf()

        // FaceKey, Face, and the rotation between the other face and the "Up" of the face key
        val seen = mutableSetOf<Face>(rawFaces[0])
        val frontier: MutableList<Triple<CubeFaceKey, Face, Rotation>> = mutableListOf(Triple(TopFace, rawFaces[0], None))
        while (frontier.isNotEmpty()) {
            val (faceKey, face, rotation) = frontier.removeFirst()
//            debugLn("Resolving face $faceKey with rotation $rotation")
            cubeFaces[faceKey] = face to rotation
            // Starting from the first face, fold one side in for each direction if possible
            Direction.values().forEach { direction ->
                val cubeDirection = rotation.rotate(direction)
//                debugLn("  Direction: $direction (actual: $cubeDirection)")
                val edgeKey = faceKey.edges[cubeDirection]!!
                val edge = FaceEdge(face, direction)
                if (!incompleteEdges.containsKey(edgeKey)) {
                    // If we haven't seen this edge yet, add an incomplete entry with this face
                    incompleteEdges[edgeKey] = edge
                } else {
                    // If we've already seen it, complete the edge.
                    cubeEdges[edgeKey] = CubeEdge(edgeKey, incompleteEdges[edgeKey]!!, edge)
                }

                // If this is connected to another face in this direction and we haven't visited it yet
                val otherFace = connectedFaces[face]!![direction]
                if (otherFace != null && !seen.contains(otherFace)) {
                    val otherFaceKey = edgeKey.connectedFace(faceKey)
                    val otherCubeEdgeDirection = otherFaceKey.edgeDirection(edgeKey)
                    val faceRotation = Rotation.getRotation(otherCubeEdgeDirection, cubeDirection.opposite)
//                    debugLn("    Edge $edgeKey moves from $faceKey to $otherFaceKey")
//                    debugLn("    Adding face $otherFaceKey, $faceRotation")
                    frontier += Triple(otherFaceKey, otherFace, faceRotation)
                    seen += otherFace
                }
            }
        }

        debugLn("Sides")
        cubeFaces.forEach { (key, value) ->
            debugLn("  $key: ${rawFaces.indexOf(value.first)}@${value.second}")
        }
        debugLn("Edges ${cubeEdges.size}")
        cubeEdges.forEach { (key, value) ->
            debugLn("  $key: (${rawFaces.indexOf(value.first.face)}@${value.first.side}), (${rawFaces.indexOf(value.second.face)}@${value.second.side})")
        }
        return CubeEdgeKey.values().map { cubeEdges[it]!! }.toList()
    }

    private data class Board(
        val tiles: Map<Vector2, Boolean>,
        val rowBounds: List<Pair<Int, Int>>,
        val position: Vector2,
        val orientation: Direction
    ) {
        fun move(instruction: Instruction): Board {
            var resultingPosition = position
            when (instruction) {
                is Move -> {
                    for (i in 1..instruction.distance) {
                        resultingPosition =
                            (resultingPosition + orientation.vector)
                                .let { vec: Vector2 ->
                                    val (realVec, tile) = if (tiles.containsKey(vec)) {
                                        vec to tiles[vec]
                                    } else {
                                        val wrapped = when (orientation) {
                                            Left -> Vector2(getLastInRow(vec.y), vec.y)
                                            Right -> Vector2(getFirstInRow(vec.y), vec.y)
                                            Down -> Vector2(vec.x, getFirstInColumn(vec.x))
                                            Up -> Vector2(vec.x, getLastInColumn(vec.x))
                                            else -> throw RuntimeException("Invalid orientation: $orientation")
                                        }
                                        wrapped to tiles[wrapped]
                                    }
                                    if (tile == null) {
                                        throw RuntimeException("Null tile wtf")
                                    } else if (tile) {
                                        resultingPosition
                                    } else {
                                        realVec
                                    }
                                }
                    }
                    return this.copy(position = resultingPosition)
                }
                is Rotate -> return this.copy(orientation = instruction.rotation.rotate(orientation))
            }

        }

        fun getFirstInRow(y: Int): Int = rowBounds[y].first
        fun getLastInRow(y: Int): Int = rowBounds[y].second
        fun getFirstInColumn(x: Int): Int =
            rowBounds.indices.first { tiles[Vector2(x, it)] != null }
        fun getLastInColumn(x: Int): Int =
            rowBounds.indices.last { tiles[Vector2(x, it)] != null }
    }

    val instructionRegex = Regex("(?:(\\d+)|([A-Z]))")
    private fun parseInstructions(line: String): List<Instruction> {
        var location = 0
        val instructions = mutableListOf<Instruction>()
        var instructionMatch = instructionRegex.matchAt(line, location)
        while (instructionMatch != null) {
            location += instructionMatch.value.length
            if (instructionMatch.groupValues[2].isBlank()) {
                instructions += Move(instructionMatch.groupValues[1].toInt())
            } else {
                instructions += Rotate(getRotation(instructionMatch.groupValues[2]))
            }
            instructionMatch = instructionRegex.matchAt(line, location)
        }
        return instructions.toList()
    }

    private fun parseInput(input: List<String>): Pair<Board, List<Instruction>> {
        val tiles = input.take(input.size - 2).mapIndexed { y, row ->
            row.mapIndexedNotNull { x, char ->
                when (char) {
                    '.' -> Vector2(x, y) to false
                    '#' -> Vector2(x, y) to true
                    else -> null
                }
            }
        }
        val rowBounds = tiles.map { it.first().first.x to it.last().first.x }
        val initialLocation = tiles[0].first().first

        return Board(tiles.flatten().toMap(), rowBounds, initialLocation, Right) to parseInstructions(input[input.lastIndex])
    }

    private val whitespaceRegex = Regex("\\s+")
    private fun parseCube(tileInput: List<String>): Cube {
        val gridWidth = tileInput.maxOf { it.length }
        val gridHeight = tileInput.size
        val minWidth: Int = tileInput.minOf {
            it.split(whitespaceRegex)
                .filter(String::isNotBlank)
                .minOf(String::length)
        }
        val minHeight: Int = (0 until gridWidth).map { column ->
            tileInput.joinToString("") { line ->
                if (column < line.length) {
                    line[column].toString()
                } else {
                    ""
                }
            }
        }.minOf {
            it.split(whitespaceRegex)
                .filter(String::isNotBlank)
                .minOf(String::length)
        }
        val minDimension = min(minWidth, minHeight)
        debugLn("$minWidth $minHeight $minDimension")
        val dimensionVector = Vector2(minDimension, minDimension)
        // Chunk into squares along X/Y
        val faces: List<Face> = tileInput
            // Chunk X
            .map { it.chunked(minDimension) }
            // Chunk Y
            .chunked(minDimension)
            .flatMapIndexed { chunkY, chunk ->
                chunk.flatMapIndexed { localY, rowChunk ->
                    rowChunk.mapIndexed { chunkX, row ->
                        val rowTiles = row.mapIndexedNotNull { localX, char ->
                            when (char) {
                                '.' -> Vector2(localX, localY) to false
                                '#' -> Vector2(localX, localY) to true
                                else -> null
                            }
                        }
                        chunkX to rowTiles
                    }
                }.groupBy({ it.first }) { it.second }.mapNotNull { (chunkX, groupedTiles) ->
                    val faceCoordinate = Vector2(chunkX, chunkY)
                    val topLeft = faceCoordinate * dimensionVector
                    val tiles = groupedTiles.flatten()
                    if (tiles.isNotEmpty()) {
                        Face(faceCoordinate, topLeft, topLeft + dimensionVector, tiles.toMap())
                    } else null
                }
            }.toList()

        debugLn("FACES")
        faces.map { Triple(it.topLeft, it.tiles.size, it.tiles) }.forEach {
            debugLn(it.first to it.second)
            (0 until minDimension).forEach { y ->
                (0 until minDimension).forEach { x ->
                    debug(it.third[Vector2(x, y)].let { if (it == true) '#' else '.' })
                }
                debugLn()
            }
        }
        debugLn("ENDFACES")
        return Cube(faces)
    }
    private fun parsePartTwo(input: List<String>): Pair<Cube, List<Instruction>> {
        return Pair(parseCube(input.takeWhile { it.isNotEmpty() }), parseInstructions(input[input.lastIndex]))
    }
}