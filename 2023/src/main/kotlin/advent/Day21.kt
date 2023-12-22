package advent

import advent.Util.derivatives
import advent.Util.polynomialExpansion
import advent.Util.pow
import advent.Util.step

object Day21 : AdventDay {
    override fun part1(input: List<String>): Any {
        val grid = parseInput(input)
        val startLocation = grid.indexToLocation(grid.elements.indexOf(1))
        val result = walkSteps(grid, startLocation, 64).toList()
        return result.last()
    }

    fun totalTiles(tileSteps: Long): Long = 2 * (tileSteps * tileSteps - tileSteps) + 1

    val steps = 26501365L
    override fun part2(input: List<String>): Any {
        val grid = parseInput(input)
        val startLocation = grid.indexToLocation(grid.elements.indexOf(1))
        val tilesFull = (steps / grid.width)
        val stepsRemaining: Int = steps.toInt() % grid.width
        return walkSteps(grid, startLocation, limitToGrid = false)
            .drop(stepsRemaining)
            .step(grid.width)
            .map { it.toLong() }
            .polynomialExpansion()
            .invoke(tilesFull)


        println("Total tile steps: $tilesFull, Remaining plot steps: $stepsRemaining")
        val full = totalTiles(tilesFull + 1)
        val innerEven = tilesFull * tilesFull
        val innerOdd = (tilesFull - 1) * (tilesFull - 1)
        val inner = innerOdd + innerEven
        println("Inner odd: $innerOdd")
        println("Inner even: $innerEven")
        val edge = full - inner - 4
        val fullWalk = walkSteps(grid, startLocation, grid.width).last()
        val fullWalkEven = walkSteps(grid, startLocation, grid.width + 1).last()
//        val fullDest = walkStepsDest(grid, startLocation, grid.width + 1)
//        println(CharGrid(grid.elements.mapIndexed { index, el ->
//            if (el < 0) {
//                '#'
//            } else if (el > 0) {
//                'S'
//            } else if (fullDest.contains(grid.indexToLocation(index))) {
//                'O'
//            } else {
//                '.'
//            }
//
//        }.toCharArray(), grid.width, grid.height))
        val cornerResults = listOf(
            Vector2(0, 0),
            Vector2(grid.maxX, 0),
            Vector2(0, grid.maxY),
            Vector2(grid.maxX, grid.maxY)
        ).map {
            walkSteps(grid, it, stepsRemaining - 1).last()
        }
        val sideResults = listOf(
            Vector2((grid.maxX) / 2, 0),
            Vector2((grid.maxX) / 2, grid.maxY),
            Vector2(0, (grid.maxY) / 2),
            Vector2(grid.maxX, (grid.maxY) / 2)
        ).map {
            walkSteps(grid, it, stepsRemaining * 2).last()
        }
//        val middleEndResults = walkSteps(grid, startLocation, stepsRemaining)
        println("Full: $fullWalk")
        println("Full Even: $fullWalkEven")
        println("Corners: $cornerResults")
        println("Side: $sideResults")
        println("Edge size: $edge")
        val main = fullWalk * innerOdd + fullWalkEven * innerEven
        val corners = cornerResults.sum().toLong() * (edge / 4)
        val sides = sideResults.sum().toLong()
        println("$main, $corners, $sides")
        val result = main + corners + sides
        val diff = 609708004316870 - result
        println("Diff from result $diff")
        println(diff.toDouble() / (innerEven - innerOdd))

        return result
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

    fun walkStepsDest(grid: IntGrid, startLocation: Vector2, totalSteps: Long): Set<Vector2> {
//        val startLocation = grid.indexToLocation(grid.elements.indexOf(1))
        val visited = mutableSetOf<Pair<Vector2, Int>>()
        val destinations = mutableSetOf<Vector2>()
        depthFirstSearch(listOf(startLocation to 0)) { pair ->
            if (visited.contains(pair)) {
                listOf()
            } else {
                val (loc, stepsTaken) = pair
                visited += pair
                val nextLocations = Directions.vectors.map { loc + it }
                    .filter { grid.isOnGrid(it) && grid[it] >= 0 }
                    .toList()
                val nextSteps = stepsTaken + 1
                if (stepsTaken + 1 < totalSteps) {
                    nextLocations.filter {
                        !visited.contains(it to (nextSteps % 2))
                    }.map { it to nextSteps }
                } else {
                    destinations += nextLocations
                    listOf()
                }
            }
        }

        return destinations
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