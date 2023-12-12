package advent

object Day10 : AdventDay {
    override fun part1(input: List<String>): Any {
        val grid = parseInput(input)
        val start = grid.getStartLocation()
        var prev = start
        var current = grid.getNext(prev + grid[prev].first, prev)
        var length = 1
        while (current != start) {
            length++
            val newCurrent = grid.getNext(prev, current)
            prev = current
            current = newCurrent
        }
        return length / 2
    }

    /**
     *  0 = unvisited
     *  1 = outside
     *  2 = visiting
     *  3 = pipe
     *  4 = inside
     */
    override fun part2(input: List<String>): Any {
        val grid = parseInput(input)
        val visited = IntGrid(IntArray(grid.size), grid.width, grid.height)
        val start = grid.getStartLocation()
        visited[start] = 3
        var prev = start
        var current = grid.getNext(prev + grid[prev].first, prev)
        while (current != start) {
            visited[current] = 3
            val newCurrent = grid.getNext(prev, current)
            prev = current
            current = newCurrent
        }

        fun check(loc: Vector2) {
            if (visited[loc] != 0) return
            var count = 0
            val move = Vector2(1, 1)
            var current = loc
            while (!visited.isOnEdge(current)) {
                current = current + move
                if (visited[current] == 3 && grid[current].let { it != Pipe.NE && it != Pipe.SW }) {
                    count += 1
                }
            }
            if (count % 2 == 0) {
                visited[loc] = 1
            } else {
                visited[loc] = 4
            }
        }
        for (y in 0 until visited.height) {
            for (x in 0 until visited.width) {
                check(Vector2(x, y))
            }
        }

        return visited.elements.count { it == 4 }
    }

    fun GenericGrid<Pipe>.getNext(from: Vector2, pipeLoc: Vector2): Vector2 {
        val pipe = this[pipeLoc]
        val fromDirection = from - pipeLoc
        return pipeLoc + when (fromDirection) {
            pipe.first -> pipe.second
            pipe.second -> pipe.first
            else -> throw RuntimeException("Can't traverse $pipe from $from through $pipeLoc. Could come from ${pipeLoc + pipe.first} or ${pipeLoc + pipe.second}")
        }
    }

    fun GenericGrid<Pipe>.getStartLocation(): Vector2 =
        this.indexToLocation(this.elements.indexOf(Pipe.START))

    enum class Pipe(val first: Vector2, val second: Vector2) {
        NS(Vector2(0, -1), Vector2(0, 1)),
        EW(Vector2(-1, 0), Vector2(1, 0)),
        NE(Vector2(0, -1), Vector2(1, 0)),
        NW(Vector2(0, -1), Vector2(-1, 0)),
        SW(Vector2(0, 1), Vector2(-1, 0)),
        SE(Vector2(0, 1), Vector2(1, 0)),
        START(Vector2(-1, 0), Vector2(1, 0)),
        EMPTY(Vector2(0, 0), Vector2(0, 0))

    }

    fun charToPipe(char: Char): Pipe = when(char) {
        '|' -> Pipe.NS
        '-' -> Pipe.EW
        'L' -> Pipe.NE
        'J' -> Pipe.NW
        '7' -> Pipe.SW
        'F' -> Pipe.SE
        'S' -> Pipe.START
        '.' -> Pipe.EMPTY
        else -> throw RuntimeException("Weird pipe: $char")
    }

    fun parseInput(input: List<String>): GenericGrid<Pipe> {
        val width = input[0].length
        val height = input.size
        val grid = input.flatMap { line ->
            line.map(::charToPipe)
        }.toTypedArray()
        return GenericGrid(grid, width, height)
    }
}