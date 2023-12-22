package advent

import advent.Util.polynomialExpansion
import advent.Util.step

object Day21 : AdventDay {
    override fun part1(input: List<String>): Any {
        val grid = parseInput(input)
        val startLocation = grid.indexToLocation(grid.elements.indexOf(1))
        val result = walkSteps(grid, startLocation, 64).toList()
        return result.last()
    }

    val steps = 26501365L
    override fun part2(input: List<String>): Any {
        val grid = parseInput(input)
        val startLocation = grid.indexToLocation(grid.elements.indexOf(1))
        val tilesFull = (steps / grid.width)
        val stepsRemaining: Int = steps.toInt() % grid.width
        return walkSteps(grid, startLocation, limitToGrid = false)
            .drop(stepsRemaining) // Start at the "remainder" so we have clean steps
            .step(grid.width) // Step a full grid width at a time
            .map { it.toLong() } // We need longs for the expansion
            .polynomialExpansion() // Compute derivatives up front and then expand based on input values.
            .invoke(tilesFull)
    }

    fun walkSteps(
        grid: IntGrid,
        startLocation: Vector2,
        totalSteps: Int = Integer.MAX_VALUE,
        limitToGrid: Boolean = true
    ): Sequence<Int> = sequence {
        val visited = mutableSetOf<Vector2>()
        val odd = mutableSetOf<Vector2>()
        val even = mutableSetOf<Vector2>()
        var frontier = listOf(startLocation)
        var stepsTaken = 0
        do {
            if (stepsTaken % 2 == 0) {
                even += frontier
            } else {
                odd += frontier
            }

            if (stepsTaken % 2 == 0) {
                yield(even.size)
            } else {
                yield(odd.size)
            }

            frontier = frontier.flatMap { loc ->
                Directions.vectors.map { loc + it }
                    .filter {
                        if (limitToGrid) {
                            grid.isOnGrid(it) && grid[it] >= 0
                        } else {
                            val onGridLoc = ((it % grid.dimensions) + grid.dimensions) % grid.dimensions
                            grid[onGridLoc] >= 0
                        }
                    }
                    .filter { !visited.contains(it)}
                    .also { visited += it }
            }
        } while (stepsTaken++ < totalSteps)
    }

    fun parseInput(input: List<String>): IntGrid {
        return IntGrid(input.flatMap { line ->
            line.map { when (it) {
                '#' -> -1
                '.' -> 0
                'S' -> 1
                else -> throw RuntimeException("barf on: $it")
            } }
        }.toIntArray(), input[0].length, input.size)
    }
}