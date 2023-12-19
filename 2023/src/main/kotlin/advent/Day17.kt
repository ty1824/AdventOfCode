package advent

import java.lang.Integer.min

object Day17 : AdventDay {
    override fun part1(input: List<String>): Any {
        val grid = parseInput(input)
        return moveTo(Vector2.zero, Vector2(grid.width - 1, grid.height - 1), grid, 1, 3)
    }

    override fun part2(input: List<String>): Any {
        val grid = parseInput(input)
        return moveTo(Vector2.zero, Vector2(grid.width - 1, grid.height - 1), grid, 4, 10)
    }

    data class SearchState(val location: Vector2, val lastDirection: Direction, val heatLoss: Int)

    fun distPrimary(a: SearchState, b: SearchState): Int {
        val dist = a.location.linearMagnitude().compareTo(b.location.linearMagnitude())
        val heat = b.heatLoss.compareTo(a.heatLoss)
        return if (dist == 0) {
            heat
        } else {
            dist
        }
    }

    fun heatPrimary(a: SearchState, b: SearchState): Int {
        val dist = a.location.linearMagnitude().compareTo(b.location.linearMagnitude())
        val heat = b.heatLoss.compareTo(a.heatLoss)
        return if (heat == 0) {
            dist
        } else {
            heat
        }
    }

    fun moveTo(start: Vector2, end: Vector2, grid: IntGrid, minDist: Int, maxDist: Int): Int {
        var minimum: Int = Integer.MAX_VALUE
        val initialSearchState = listOf(SearchState(start, Direction.Left, 0), SearchState(start, Direction.Up, 0))
        val comparator: (SearchState, SearchState) -> Int = ::heatPrimary
        val minAt: MutableMap<Pair<Vector2, Direction>, Int> = mutableMapOf()
        lowestFirstSearch(initialSearchState, comparator) { (loc, lastDirection, heatLoss) ->
            val dist = (end - loc).linearMagnitude()
            if (minimum <= heatLoss + dist) {
                listOf()
            } else if (loc == end) {
                minimum = min(minimum, heatLoss)
                listOf()
            } else {
                minAt[loc to lastDirection] = heatLoss
                (Directions - lastDirection - lastDirection.opposite).flatMap { newDirection ->
                    var newHeatLoss = heatLoss
                    (1 until minDist).map { loc + newDirection.vector * it }
                        .filter { grid.isOnGrid(it) }
                        .map {
                            newHeatLoss += grid[it]
                        }
                    (minDist..maxDist).map { loc + newDirection.vector * it }
                        .filter { grid.isOnGrid(it) }
                        .map {
                            newHeatLoss += grid[it]
                            SearchState(it, newDirection, newHeatLoss)
                        }.filter {
                            val prevMin = minAt[it.location to it.lastDirection] ?: Int.MAX_VALUE
                            if (prevMin <= it.heatLoss) {
                                false
                            } else {
                                minAt[it.location to it.lastDirection] = it.heatLoss
                                true
                            }
                        }
                }
            }
        }
        return minimum
    }

    fun parseInput(input: List<String>): IntGrid =
        IntGrid(input.flatMap { line -> line.map { it.digitToInt() } }.toIntArray(), input[0].length, input.size)

}