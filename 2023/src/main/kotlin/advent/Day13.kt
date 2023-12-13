package advent

object Day13 : AdventDay {
    override fun part1(input: List<String>): Any =
        parseInput(input).map {
            findMirror(it)
        }.sumOf { it.x + it.y * 100}

    override fun part2(input: List<String>): Any =
        parseInput(input).map {
            findMirrorSmudge(it)
        }.sumOf { it.x + it.y * 100}

    fun findMirror(grid: CharGrid): Vector2 {
        for (first in 0 until grid.height - 1) {
            val second = first + 1
            if (checkMirrorVert(grid, first, second)) return Vector2(0, second)
        }

        for (first in 0 until grid.width - 1) {
            val second = first + 1
            if (checkMirrorHoriz(grid, first, second)) return Vector2(second, 0)
        }

        println(grid)
        throw RuntimeException("Couldn't find one for this grid")
    }

    tailrec fun checkMirrorVert(grid: CharGrid, firstRow: Int, secondRow: Int): Boolean {
        return if (firstRow < 0 || secondRow >= grid.height) {
            true
        } else if (grid.getRow(firstRow).toList() == grid.getRow(secondRow).toList()) {
            checkMirrorVert(grid, firstRow - 1, secondRow + 1)
        } else {
            false
        }
    }

    tailrec fun checkMirrorHoriz(grid: CharGrid, firstCol: Int, secondCol: Int): Boolean {
        return if (firstCol < 0 || secondCol >= grid.width) {
            true
        } else if (grid.getColumn(firstCol).toList() == grid.getColumn(secondCol).toList()) {
            checkMirrorHoriz(grid, firstCol - 1, secondCol + 1)
        } else {
            false
        }
    }

    fun findMirrorSmudge(grid: CharGrid): Vector2 {
        for (first in 0 until grid.height - 1) {
            val second = first + 1
            if (checkMirrorVertSmudge(grid, first, second)) return Vector2(0, second)
        }

        for (first in 0 until grid.width - 1) {
            val second = first + 1
            if (checkMirrorHorizSmudge(grid, first, second)) return Vector2(second, 0)
        }

        println(grid)
        throw RuntimeException("Couldn't find one for this grid")
    }

    tailrec fun checkMirrorVertSmudge(grid: CharGrid, firstRowIndex: Int, secondRowIndex: Int): Boolean {
        return if (firstRowIndex < 0 || secondRowIndex >= grid.height) {
            false // must fall back to non-smudge or we fail
        } else {
            val firstRow = grid.getRow(firstRowIndex).joinToString("")
            val secondRow = grid.getRow(secondRowIndex).joinToString("")
            if (firstRow == secondRow) {
                checkMirrorVertSmudge(grid, firstRowIndex - 1, secondRowIndex + 1)
            } else if (checkIfEqualWithSmudge(firstRow, secondRow)) {
                checkMirrorVert(grid, firstRowIndex - 1, secondRowIndex + 1)
            } else {
                false
            }
        }
    }

    tailrec fun checkMirrorHorizSmudge(grid: CharGrid, firstColIndex: Int, secondColIndex: Int): Boolean {
        return if (firstColIndex < 0 || secondColIndex >= grid.width) {
            false // must fall back to non-smudge or we fail
        } else {
            val firstCol = grid.getColumn(firstColIndex).joinToString("")
            val secondCol = grid.getColumn(secondColIndex).joinToString("")
            if (firstCol == secondCol) {
                checkMirrorHorizSmudge(grid, firstColIndex - 1, secondColIndex + 1)
            } else if (checkIfEqualWithSmudge(firstCol, secondCol)) {
                checkMirrorHoriz(grid, firstColIndex - 1, secondColIndex + 1)
            } else {
                false
            }
        }
    }

    /**
     * Checks if two strings differ by only a single character
     */
    fun checkIfEqualWithSmudge(first: String, second: String): Boolean =
        first.zip(second).filter { it.first != it.second}.size == 1


    fun parseInput(input: List<String>): List<CharGrid> =
        input.split { it.isEmpty() }.map { subgrid ->
            CharGrid(subgrid.flatMap { it.toList() }.toCharArray(), subgrid[0].length, subgrid.size)
        }

    fun <T> List<T>.split(predicate: (T) -> Boolean): List<List<T>> {
        var previous = 0
        var current = 0
        val result = mutableListOf<List<T>>()
        while (current < size) {
            if (predicate(get(current))) {
                result.add(this.subList(previous, current))
                previous = current + 1
            }
            current++
        }
        if (previous != current) {
            result.add(this.subList(previous, current))
        }
        return result
    }
}