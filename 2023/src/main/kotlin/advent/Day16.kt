package advent

import java.lang.Integer.max

object Day16 : AdventDay {
    override fun part1(input: List<String>): Any {
        val mirrors = parseMirrors(input)
        val grid = Grid.intGrid(input[0].length, input.size)
        val energized = followBeam(Vector2.zero, Direction.Right, grid, mirrors)
        return grid.elements.count { it > 0 } to energized.size
    }

    override fun part2(input: List<String>): Any {
        // TODO: Not sure how I broke this when refactoring it, but I did :(
        val mirrors = parseMirrors(input)
        val width = input[0].length - 1
        val height = input.size - 1
        val mirrorGrid = SparseGrid(mirrors.toMutableMap(), width + 1, height + 1)
        val mirrorResults: MutableMap<Pair<Vector2, Direction>, Set<Vector2>> = mutableMapOf()
        val down = mirrorGrid.getEdgeVectors(Direction.Up).maxOf { loc ->
            followBeamMemo(loc, Direction.Down, mirrorGrid, mirrorResults).size
        }
        println("Max down: $down")
        val up = mirrorGrid.getEdgeVectors(Direction.Down).maxOf { loc ->
            followBeamMemo(loc, Direction.Up, mirrorGrid, mirrorResults).size
        }
        println("Max up: $up")
        val right = mirrorGrid.getEdgeVectors(Direction.Left).maxOf { loc ->
            followBeamMemo(loc, Direction.Right, mirrorGrid, mirrorResults).size
        }
        println("Max right: $right")
        val left = mirrorGrid.getEdgeVectors(Direction.Right).maxOf { loc ->
            followBeamMemo(loc, Direction.Left, mirrorGrid, mirrorResults).size
        }
        println("Max left: $left")
        return max(max(down, up), max(right, left))
    }

    fun followBeamMemo(
        initialLoc: Vector2,
        initialDirection: Direction,
        mirrors: SparseGrid<Mirror>,
        mirrorResults: MutableMap<Pair<Vector2, Direction>, Set<Vector2>>,
    ): Set<Vector2> {
        fun resultsFor(loc: Vector2, direction: Direction): Set<Vector2> {
            return if (mirrorResults.containsKey(loc to direction)) {
                mirrorResults[loc to direction]!!
            } else {
                val results = mutableSetOf<Vector2>()
                mirrorResults[loc to direction] = results
                val mirrorLoc = mirrors.firstInDirection(loc, direction, true) {
                    mirrors[it]?.let { mirror -> mirror.traverse(direction) != null } ?: false
                }
                val mirror = mirrorLoc?.let { mirrors[it] }
                results += if (mirrorLoc == null || mirror == null) {
                    // If no mirrors in the way, energize the rest and then return nothing
                    (loc..(mirrors.getEdge(loc, direction))).toSet()
                } else {
                    val immediate = (loc..mirrorLoc).toSet()
                    val traversed = mirror.traverse(direction)!!
                        .map { mirrorLoc + it.vector to it }
                        .filter { mirrors.isOnGrid(it.first) }
                        .flatMap { resultsFor(it.first, it.second) }
                        .toSet()
                    immediate + traversed
                }
                results
            }
        }
        return resultsFor(initialLoc, initialDirection)
    }

    fun followBeam(loc: Vector2, initialDirection: Direction, grid: IntGrid, mirrors: Map<Vector2, Mirror>): Set<Vector2> {
        val energized: MutableSet<Vector2> = mutableSetOf(loc)
        val mirrorPaths: MutableSet<Pair<Vector2, Direction>> = mutableSetOf()
        depthFirstSearch(loc to initialDirection) { (loc, direction) ->
            val mirrorLoc = grid.firstInDirection(loc, direction, true) {
                mirrors[it]?.let { it.traverse(direction) != null } ?: false
            }
            val mirror = mirrors[mirrorLoc]
            if (mirrorLoc == null || mirror == null) {
                // If no mirrors in the way, energize the rest and then return nothing
                (loc..(grid.getEdge(loc, direction))).forEach {
                    grid[it] += 1
                    energized += it
                }
                listOf()
            } else {
                (loc..mirrorLoc).forEach {
                    grid[it] += 1
                    energized += it
                }
                mirror.traverse(direction)!!
                    .map { mirrorLoc + it.vector to it }
                    .toList()
                    .filter {
                        grid.isOnGrid(it.first) && !mirrorPaths.contains(it)
                    }.also { mirrorPaths += it }
            }
        }
        return energized
    }

    enum class Mirror(val traverse: (Direction) -> Sequence<Direction>?) {
        Vertical({ when (it) {
            Direction.Left, Direction.Right -> sequence {
                yield(Direction.Up)
                yield(Direction.Down)
            }
            else -> null
        } }),
        Horizontal({ when (it) {
            Direction.Up, Direction.Down -> sequence {
                yield(Direction.Left)
                yield(Direction.Right)
            }
            else -> null
        } }),
        LeftRight({ when (it) {
            Direction.Down -> sequenceOf(Direction.Right)
            Direction.Left -> sequenceOf(Direction.Up)
            Direction.Up -> sequenceOf(Direction.Left)
            Direction.Right -> sequenceOf(Direction.Down)
        } }),
        RightLeft({ when (it) {
            Direction.Down -> sequenceOf(Direction.Left)
            Direction.Right -> sequenceOf(Direction.Up)
            Direction.Up -> sequenceOf(Direction.Right)
            Direction.Left -> sequenceOf(Direction.Down)
        } }),

    }

    fun parseMirrors(input: List<String>): Map<Vector2, Mirror> =
        input.flatMapIndexed { y, line ->
            line.mapIndexedNotNull() { x, char ->
                val vector = Vector2(x, y)
                when (char) {
                    '\\' -> vector to Mirror.LeftRight
                    '/' -> vector to Mirror.RightLeft
                    '-' -> vector to Mirror.Horizontal
                    '|' -> vector to Mirror.Vertical
                    else -> null
                }
            }
        }.toMap()

}