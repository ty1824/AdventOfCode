package advent

import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.sqrt

object Day8 : AdventDay {
    override fun part1(input: List<String>): Any {
        return parseGrid(input).getVisibleLocations().count()
    }

    override fun part2(input: List<String>): Any {
        return parseGrid(input).getLargestScenicScore()
    }

    private const val TOP = 0
    private const val BOTTOM = 1
    private const val LEFT = 2
    private const val RIGHT = 3
    val DIRECTIONS = intArrayOf(TOP, BOTTOM, LEFT, RIGHT)

    class Grid(private val trees: IntArray) {
        private val dimension = sqrt(trees.size.toDouble()).toInt()

        private val largest : Array<IntArray> = Array(trees.size) { IntArray(4) { -1 } }
        private val larger: Array<IntArray> = Array(trees.size) { IntArray(4) { -1 } }

        private fun Pair<Int, Int>.toIndex(): Int = (dimension * this.second) + (this.first)

        private fun Int.toLocation(): Pair<Int, Int> = (this % dimension) to (this / dimension)

        private fun getSameOrLargerTreeInDirection(location: Pair<Int, Int>, direction: Int): Int {
            val index = location.toIndex()
            if (larger[index][direction] < 0) {
                val thisSize = trees[index]
                var neighbor = location.getNeighboringLocation(direction)
                var result = index
                while (!neighbor.isOffGrid() && result == index) {
                    val neighborIndex = neighbor.toIndex()
                    if (thisSize <= trees[neighborIndex])
                        result = neighborIndex
                    else {
                        val neighborLarger = getSameOrLargerTreeInDirection(neighbor, direction)
                        if (neighborLarger == neighborIndex) {
                            break
                        } else if (trees[neighborLarger] >= thisSize) {
                            result = neighborLarger
                            break
                        } else
                            neighbor = neighborLarger.toLocation()
                    }
                }
                larger[index][direction] = if (result < 0) { index } else { result }
            }
            return larger[index][direction]
        }

        private fun getLargestTreeInDirection(location: Pair<Int, Int>, direction: Int): Int {
            val index = location.toIndex()
            if (largest[index][direction] < 0) {
                val neighbor = location.getNeighboringLocation(direction)
                if (neighbor.isOffGrid() || trees[index] > trees[getLargestTreeInDirection(neighbor, direction)]) {
                    // If we are on the edge & our neighbor is off the board, we are the largest in the direction
                    // Alternately, if we are taller than the other largest tree, use our height
                    largest[index][direction] = index
                } else {
                    largest[index][direction] = getLargestTreeInDirection(neighbor, direction)
                }
            }
            return largest[index][direction]
        }

        private fun isLocationVisibleFrom(location: Pair<Int, Int>, direction: Int): Boolean {
            val index = location.toIndex()
            val neighbor = location.getNeighboringLocation(direction)
            return if (neighbor.isOffGrid()) { true } else {
                trees[index] > trees[getLargestTreeInDirection(neighbor, direction)]
            }
        }

        private fun isLocationVisible(location: Pair<Int, Int>): Boolean {
            return DIRECTIONS.any { direction -> isLocationVisibleFrom(location, direction) }
        }

        fun getVisibleLocations(): List<Pair<Int, Int>> =
            trees.mapIndexed { index, _ -> index.toLocation() }
                .filter { isLocationVisible(it) }

        private fun getLargerTrees(location: Pair<Int, Int>): List<Pair<Int, Int>> =
            DIRECTIONS.map { direction -> getSameOrLargerTreeInDirection(location, direction).toLocation() }

        private fun getTreesInDirection(location: Pair<Int, Int>, direction: Int): Int {
            return when (direction) {
                TOP -> location.second
                BOTTOM -> dimension - location.second - 1
                LEFT -> location.first
                RIGHT -> dimension - location.first - 1
                else -> throw RuntimeException("Bad direction: $direction")
            }
        }

        private fun getDirectionalDistance(location: Pair<Int, Int>, otherLocation: Pair<Int, Int>, direction: Int): Int {
            return when (direction) {
                TOP, BOTTOM -> abs(otherLocation.second - location.second)
                LEFT, RIGHT -> abs(otherLocation.first - location.first)
                else -> throw RuntimeException("Bad direction: $direction")
            }
        }

        private fun getScenicScore(location: Pair<Int, Int>): Int {
            val index = location.toIndex()
            return getLargerTrees(location).mapIndexed { direction, otherLocation ->
                if (otherLocation.toIndex() == index)
                    // We are the tallest tree and thus can see to the edge
                    getTreesInDirection(location, direction)
                else
                    // See until the next-largest tree
                    getDirectionalDistance(location, otherLocation, direction)
            }.reduce(Int::times)
        }

        private var debug = false
        fun getLargestScenicScore(): Int {
            val maxLocation = trees.mapIndexed { index, _ -> index.toLocation() }.maxBy { getScenicScore(it) }
            return getScenicScore(maxLocation)
        }


        private fun Pair<Int, Int>.isOffGrid() =
            this.first < 0 || this.first >= dimension || this.second < 0 || this.second >= dimension

        private fun Pair<Int, Int>.getNeighboringLocation(direction: Int) = when (direction) {
            TOP -> this.first to this.second - 1
            BOTTOM -> this.first to this.second + 1
            LEFT -> this.first - 1 to this.second
            RIGHT -> this.first + 1 to this.second
            else -> throw RuntimeException("Not a valid direction: $direction")
        }
    }

    fun Int.toDirection(): String = when (this) {
        TOP -> "TOP"
        BOTTOM -> "BOTTOM"
        LEFT -> "LEFT"
        RIGHT -> "RIGHT"
        else -> "error"
    }

    fun parseGrid(input: List<String>): Grid = Grid(input.flatMap { row -> row.map { it.digitToInt() } }.toIntArray())
}