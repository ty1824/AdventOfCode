package advent

import java.lang.Integer.min

object Day22 : AdventDay {
    override fun part1(input: List<String>): Any {
        val original = parseInput(input)
        val bricksAtRest = brickfall(original)
        val supportedBy = findSupportedBy(bricksAtRest)
        val supporting = findSupporting(bricksAtRest)
        return bricksAtRest.count { brick ->
            val supportedBricks = supporting[brick]
            supportedBricks.isNullOrEmpty() || supportedBricks.all {(supportedBy[it] ?: listOf()).size > 1 }
        }
    }

    override fun part2(input: List<String>): Any {
        val original = parseInput(input)
        val bricksAtRest = brickfall(original)
        return bricksAtRest.map { brick ->
            brick to findAllAffectedBricks(bricksAtRest, brick).size
        }.sumOf { it.second }
    }

    fun findAllAffectedBricks(bricks: List<Brick>, brick: Brick): Set<Brick> {
        val affected = mutableSetOf<Brick>()
        val supportedBy: Map<Brick, MutableSet<Brick>> = findSupportedBy(bricks).map { (k, v) -> k to v.toMutableSet() }.toMap()
        val supporting = findSupporting(bricks)

        fun getSupportedBy(brick: Brick) = supportedBy[brick] ?: mutableSetOf()

        var iters = 0
        var current = setOf(brick)
        while (current.isNotEmpty()) {
            // Find all bricks supported by the current frontier
            val allSupporting = current.flatMap {
                supporting[it] ?: setOf()
            }.toSet()

            // Remove current frontier from the supports for the new frontier
            allSupporting.forEach {
                getSupportedBy(it).removeAll(current)
            }

            // Filter new frontier by bricks that are no longer supported
            current = allSupporting.filter {
                getSupportedBy(it).size == 0
            }.toSet()
            affected += current
        }
        return affected
    }

    fun findSupportedBy(bricks: List<Brick>): Map<Brick, Set<Brick>> {
        val brickLocs = locToBrick(bricks)
        val supportedBy = mutableMapOf<Brick, Set<Brick>>()
        bricks.forEach { brick ->
            supportedBy[brick] = brick.squares.map { it.minusZ(1) }.mapNotNull { square ->
                val supportingBrick = brickLocs[square]
                if (supportingBrick != null && supportingBrick != brick) {
                    supportingBrick
                } else {
                    null
                }
            }.toSet()
        }
        return supportedBy
    }

    fun findSupporting(bricks: List<Brick>): Map<Brick, Set<Brick>> {
        val brickLocs = locToBrick(bricks)
        val supporting = mutableMapOf<Brick, Set<Brick>>()
        bricks.forEach { brick ->
            supporting[brick] = brick.squares.map { it.plusZ(1) }.mapNotNull { square ->
                val supportedBrick = brickLocs[square]
                if (supportedBrick != null && supportedBrick != brick) {
                    supportedBrick
                } else {
                    null
                }
            }.toSet()
        }
        return supporting
    }

    fun locToBrick(bricks: List<Brick>): Map<Vector3, Brick> {
        val brickLocs = mutableMapOf<Vector3, Brick>()
        bricks.forEach { brick ->
            brick.squares.forEach { square ->
                brickLocs[square] = brick
            }
        }
        return brickLocs
    }

    fun brickfall(bricks: List<Brick>): List<Brick> {
        val restState = mutableMapOf<Vector2, Int>()
        val newBricks = mutableListOf<Brick>()
        bricks.sortedBy { min(it.start.z, it.end.z) }.forEach { brick ->
            val squares = brick.squares.toList()
            val lowSquares = squares.groupBy { square -> square.x to square.y }
                .map { group -> group.value.minBy { it.z } }
            val fallDist = lowSquares.minOf { it.z - (restState[Vector2(it.x, it.y)] ?: 0) } - 1
            val newBrick = Brick(brick.start.minusZ(fallDist), brick.end.minusZ(fallDist))
            newBrick.squares.forEach {
                restState[Vector2(it.x, it.y)] = it.z
            }
            newBricks += newBrick
        }
        return newBricks
    }

    data class Brick(val start: Vector3, val end: Vector3) {
        val squares: List<Vector3> by lazy { (start..end).toList() }
    }

    fun parseInput(input: List<String>): List<Brick> {
        return input.map {
            val (start, end) = it.split("~")
            Brick(parseVector(start), parseVector(end))
        }
    }

    fun parseVector(vecString: String): Vector3 {
        val (x, y, z) = vecString.split(",").map { it.toInt() }
        return Vector3(x, y, z)
    }
}