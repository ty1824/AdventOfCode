package advent

import java.lang.Integer.max
import java.lang.Integer.min

object Day14 : AdventDay {
    val sandPosition = Vector2(500, 0)

    override fun part1(input: List<String>): Any {
        val (grid, dimensions) = parseGrid(input, false)
        var count = 0
        debugln(printGrid(grid))
        while (dropSand(grid, dimensions)) {
            count++
        }
        debugln("/".repeat(60))
        debugln(printGrid(grid))
        return count
    }

    override fun part2(input: List<String>): Any {
        val (grid, dimensions) = parseGrid(input, true)
        var count = 0
        println(printGrid(grid))
        while (dropSand(grid, dimensions)) {
            count++
        }
        debugln("/".repeat(60))
        debugln(printGrid(grid))
        return count
    }

    val possibleMovements = listOf(Vector2(0, 1), Vector2(-1, 1), Vector2(1, 1))
    fun dropSand(grid: IntGrid, dimensions: Dimensions): Boolean {
        var sandPosition: Vector2? = sandPosition - dimensions.min
        if (grid[sandPosition!!] == SAND) return false
        var atRest = false
        var count = 0
        while (sandPosition != null && !atRest) {
            val possiblePosition = possibleMovements.map { sandPosition!! + it }.firstOrNull {
                grid.isOffGrid(it) || grid[it] == EMPTY
            }
            if (possiblePosition == null) {
                atRest = true
            } else if (grid.isOffGrid(possiblePosition)) {
                sandPosition = null
            } else {
                sandPosition = possiblePosition
            }
            count++
        }
        val result =  if (sandPosition == null) {
            false
        } else {
            grid[sandPosition] = SAND
            true
        }
        return result
    }

    const val EMPTY = 0
    const val ROCK = 1
    const val SAND = 2
    fun parseGrid(input: List<String>, part2: Boolean): Pair<IntGrid, Dimensions> {
        val dimensions = Dimensions(500, 0, 500, 0)
        val paths = input.map { parsePath(it, dimensions) }
        val bottom = if (part2) {
            val bottomHeight = dimensions.maxY + 2
            ((500 - bottomHeight)..(500 + bottomHeight)).map { x ->
                val vec = Vector2(x, bottomHeight)
                dimensions.include(vec)
                vec
            }
        } else listOf()
        val size = dimensions.width * dimensions.height
        val grid = IntGrid(IntArray(size) { EMPTY }, dimensions.width, dimensions.height)

        // Scale the grid so it is minimum size for the points we care about
        val scaledPaths = paths.map { Path(it.points.map { oldPoint -> Vector2(oldPoint.x - dimensions.minX, oldPoint.y - dimensions.minY)})}
        scaledPaths.forEach { path ->
            path.points.windowed(2, 1) { line ->
                val linePoints = (line[0] to line[1]).pointsOnLine()
                linePoints.forEach {
                    grid[it] = ROCK
                }
            }
        }
        bottom.map { Vector2(it.x - dimensions.minX, it.y - dimensions.minY) }.forEach {
            grid[it] = ROCK
        }
        return grid to dimensions
    }

    data class Dimensions(var minX: Int, var minY: Int, var maxX: Int, var maxY: Int) {
        val width: Int
            get() = maxX - minX + 1
        val height: Int
            get() = maxY - minY + 1
        val min: Vector2
            get() = Vector2(minX, minY)
        val max: Vector2
            get() = Vector2(maxX, maxY)
    }
    fun Dimensions.combine(other: Dimensions): Dimensions =
        Dimensions(
            min(this.minX, other.minX),
            min(this.minY, other.minY),
            max(this.maxX, other.maxX),
            max(this.maxY, other.maxY)
        )

    fun Dimensions.include(point: Vector2) {
        if (point.x < minX) minX = point.x
        if (point.x > maxX) maxX = point.x
        if (point.y < minY) minY = point.y
        if (point.y > maxY) maxY = point.y
    }

    private fun Pair<Vector2, Vector2>.pointsOnLine(): List<Vector2> {
        val xRange = this.first.x toward this.second.x
        val yRange = this.first.y toward this.second.y

        val points = xRange.flatMap { x ->
            yRange.map { y ->
                Vector2(x, y)
            }
        }
        return points
    }

    private infix fun Int.toward(to: Int): IntProgression {
        val step = if (this > to) -1 else 1
        return IntProgression.fromClosedRange(this, to, step)
    }

    private fun parsePath(input: String, dimensions: Dimensions): Path {
        return Path(input.split(" -> ").map {
            val vec = Vector2(it.substringBefore(',').toInt(), it.substringAfter(',').toInt())
            dimensions.include(vec)
            vec
        })
    }

    private fun printGrid(grid: IntGrid): String {
        return (0 until grid.height).joinToString("\n") { y ->
            (0 until grid.width).joinToString("") { x ->
                when (grid[Vector2(x, y)]) {
                    EMPTY -> "."
                    ROCK -> "#"
                    SAND -> "0"
                    else -> "?"
                }
            }
        }

    }
}