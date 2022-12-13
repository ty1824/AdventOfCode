package advent

import kotlin.math.abs
import kotlin.math.sqrt

object Day8 : AdventDay {
    override fun part1(input: List<String>): Any {
        return parseGrid(input).getVisibleLocations().count()
    }

    override fun part2(input: List<String>): Any {
        return parseGrid(input).getLargestScenicScore()
    }

    class Trees(private val grid: IntGrid) {
        private val larger: Array<IntArray> = Array(grid.size) { IntArray(4) { -1 } }

        private fun getSameOrLargerTreeInDirection(location: Vector2, direction: Int): Int {
            val index = grid.locationToIndex(location)
            if (larger[index][direction] < 0) {
                val thisSize = grid[index]
                var neighbor = location + direction
                var result = index
                while (grid.isOnGrid(neighbor) && result == index) {
                    val neighborIndex = grid.locationToIndex(neighbor)
                    if (thisSize <= grid[neighborIndex])
                        result = neighborIndex
                    else {
                        val neighborLarger = getSameOrLargerTreeInDirection(neighbor, direction)
                        if (neighborLarger == neighborIndex) {
                            break
                        } else if (grid[neighborLarger] >= thisSize) {
                            result = neighborLarger
                            break
                        } else
                            neighbor = grid.indexToLocation(neighborLarger)
                    }
                }
                larger[index][direction] = result
            }
            return larger[index][direction]
        }

        private fun isLocationVisibleFrom(location: Vector2, direction: Int): Boolean {
            return getSameOrLargerTreeInDirection(location, direction) == grid.locationToIndex(location)
        }

        private fun isLocationVisible(location: Vector2): Boolean {
            return DIRECTIONS.any { direction -> isLocationVisibleFrom(location, direction) }
        }

        fun getVisibleLocations(): List<Vector2> =
            grid.elements.mapIndexed { index, _ -> grid.indexToLocation(index) }
                .filter { isLocationVisible(it) }

        private fun getLargerTrees(location: Vector2): List<Vector2> =
            DIRECTIONS.map { direction -> grid.indexToLocation(getSameOrLargerTreeInDirection(location, direction)) }

        private fun getTreesInDirection(location: Vector2, direction: Int): Int {
            return grid.locationToIndex(location + DIRECTION_VECTORS[direction])
        }

        private fun getDirectionalDistance(location: Vector2, otherLocation: Vector2, direction: Int): Int {
            return when (direction) {
                UP, DOWN -> abs(otherLocation.y - location.y)
                LEFT, RIGHT -> abs(otherLocation.x - location.x)
                else -> throw RuntimeException("Bad direction: $direction")
            }
        }

        private fun getScenicScore(location: Vector2): Int {
            val index = grid.locationToIndex(location)
            return getLargerTrees(location).mapIndexed { direction, otherLocation ->
                if (grid.locationToIndex(otherLocation) == index)
                    // We are the tallest tree and thus can see to the edge
                    getTreesInDirection(location, direction)
                else
                    // See until the next-largest tree
                    getDirectionalDistance(location, otherLocation, direction)
            }.reduce(Int::times)
        }

        fun getLargestScenicScore(): Int {
            val maxLocation = grid.elements.mapIndexed { index, _ -> grid.indexToLocation(index) }.maxBy { getScenicScore(it) }
            return getScenicScore(maxLocation)
        }

        private fun Vector2.getNeighboringLocation(direction: Int) = this + DIRECTION_VECTORS[direction]
    }

    private fun parseGrid(input: List<String>): Trees =
        Trees(IntGrid(input.flatMap { row -> row.map { it.digitToInt() } }.toIntArray(), input.first().length, input.size))
}