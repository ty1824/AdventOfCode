package advent

object Day11 : AdventDay {
    override fun part1(input: List<String>): Any {
        val grid2 = parseInput(input)
        val expandedRows = grid2.expandedRows()
        val expandedColumns = grid2.expandedColumns()
        val galaxies2 = grid2.galaxyLocations()
        val sums = mutableListOf<Int>()
        for (i1 in 0 until (galaxies2.size - 1)) {
            for (i2 in i1 + 1 until galaxies2.size) {
                val from = galaxies2[i1]
                val target = galaxies2[i2]
                val expandedBetween = expandedRows.count {
                    from.yRange(target).contains(it) } +
                        expandedColumns.count { from.xRange(target).contains(it) }
                val stepVector = target - from
                val distance = stepVector.manhattanDistance() + expandedBetween
                sums += distance
            }
        }
        return sums.sum()
    }

    override fun part2(input: List<String>): Any {
        val grid2 = parseInput(input)
        val expandedRows = grid2.expandedRows()
        val expandedColumns = grid2.expandedColumns()
        val galaxies2 = grid2.galaxyLocations()
        val sums = mutableListOf<Long>()
        for (i1 in 0 until (galaxies2.size - 1)) {
            for (i2 in i1 + 1 until galaxies2.size) {
                val from = galaxies2[i1]
                val target = galaxies2[i2]
                val expandedBetween = expandedRows.count {
                    from.yRange(target).contains(it) } +
                        expandedColumns.count { from.xRange(target).contains(it) }
                val stepVector = target - from
                val distance = stepVector.manhattanDistance() + expandedBetween * (1000000L - 1)
                sums += distance
            }
        }
        return sums.sum()
    }

    fun CharGrid.galaxyLocations(): List<Vector2> =
        elements.asSequence().mapIndexedNotNull { index, element ->
            if (element == '#') {
                indexToLocation(index)
            } else {
                null
            }
        }.toList()

    fun CharGrid.expandedRows(): List<Int> =
        getRows().mapIndexedNotNull { index, row ->
            val r = row.toList()
            if (r.none { it == '#' }) {
                index
            } else {
                null
            }
        }.toList()

    fun CharGrid.expandedColumns(): List<Int> =
        getColumns().mapIndexedNotNull { index, col ->
            val c = col.toList()
            if (c.none { it == '#' }) {
                index
            } else {
                null
            }
        }.toList()

    fun parseInput(input: List<String>): CharGrid {
        val width = input[0].length
        val height = input.size
        val grid = input.flatMap { line ->
            line.toList()
        }.toCharArray()
        return CharGrid(grid, width, height)
    }
}