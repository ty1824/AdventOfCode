package advent

object Day14 : AdventDay {
    override fun part1(input: List<String>): Any {
        val grid = parseInput(input)
        moveRocksInDirection(grid, Direction.Up)
        return calclulateLoad(grid)
    }

    val cycles = 1000000000
    override fun part2(input: List<String>): Any {
        val grid = parseInput(input)
        val lastN = mutableListOf(grid.toString())
        var patternStart = -1
        while (patternStart < 0) {
            cycle(grid)
            val current = grid.toString()
            patternStart = lastN.lastIndexOf(current)
            lastN += current
        }
        val length = lastN.lastIndex - patternStart
        val remaining = cycles - lastN.lastIndex
        repeat(remaining % length) {
            cycle(grid)
        }
        return calclulateLoad(grid)
    }

    fun calclulateLoad(grid: CharGrid): Int {
        return grid.elements.mapIndexed { index, el -> el to grid.indexToLocation(index) }.sumOf { (tile, location) ->
            if (tile == 'O') {
                grid.height - location.y
            } else { 0 }
        }
    }

    val cycleDirections = listOf(Direction.Up, Direction.Left, Direction.Down, Direction.Right)
    fun cycle(grid: CharGrid) {
        cycleDirections.forEach {
            moveRocksInDirection(grid, it)
        }
    }

    fun roundRockLocationsNearSide(grid: CharGrid, direction: Direction): List<Vector2> {
        val locations = mutableListOf<Vector2>()
        val iterateOn = when (direction) {
            Direction.Right -> ((grid.width-1) downTo 0).flatMap { x ->
                (0 until grid.height).map { y ->
                    Vector2(x, y)
                }
            }
            Direction.Left -> (0 until grid.width).flatMap { x ->
                (0 until grid.height).map { y ->
                    Vector2(x, y)
                }
            }
            Direction.Down -> ((grid.height-1) downTo 0).flatMap { y ->
                (0 until grid.width).map { x ->
                    Vector2(x, y)
                }
            }
            Direction.Up -> (0 until grid.height).flatMap { y ->
                (0 until grid.width).map { x ->
                    Vector2(x, y)
                }
            }
        }
        iterateOn.forEach { location ->
            if (grid[location] == 'O') locations += location
        }
        return locations
    }

    fun moveRocksInDirection(grid: CharGrid, direction: Direction) {
        roundRockLocationsNearSide(grid, direction).forEach {
            moveRockInDirection(grid, direction, it)
        }
    }

    fun moveRockInDirection(grid: CharGrid, direction: Direction, rock: Vector2) {
        if (grid.isOffGrid(rock + direction.vector) ||
            grid[rock + direction.vector] == '#' ||
            grid[rock + direction.vector] == 'O') return
        val edge = when (direction) {
            Direction.Right -> Vector2(grid.width - 1, rock.y)
            Direction.Down -> Vector2(rock.x, grid.height - 1)
            Direction.Left -> Vector2(0, rock.y)
            Direction.Up -> Vector2(rock.x, 0)
        }
        val allInDirection = sequence {
            var current = rock
            yield(current)
            while (current != edge) {
                current += direction.vector
                yield(current)
            }
        }
        val dest = allInDirection.firstOrNull {
            val vector = it + direction.vector
            grid.isOffGrid(vector) || grid[vector] != '.'
        }
        if (dest != null && dest != rock) {
            grid[rock] = '.'
            grid[dest] = 'O'
        }
    }

    fun parseInput(input: List<String>): CharGrid =
        CharGrid(input.flatMap { it.asSequence() }.toCharArray(), input[0].length, input.size)

}