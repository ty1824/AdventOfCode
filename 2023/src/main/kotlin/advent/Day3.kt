package advent

object Day3 : AdventDay {
    override fun part1(input: List<String>): Any {
        val grid = parseGrid(input)
        val numbers = input.flatMapIndexed(::lineToNumbers)
        return numbers.sumOf { num ->
            if (num.location.any { loc -> grid.anyAdjacent(loc) { it < 0 } }) {
                num.value
            } else {
                0
            }
        }
    }

    override fun part2(input: List<String>): Any {
        val grid = parseGrid(input)
        val numbers = input.flatMapIndexed(::lineToNumbers)
        val locToNumber = numbers.flatMap {
            it.location.map { loc -> loc to it}
        }.toMap()
        val gears = input.flatMapIndexed(::lineToGears)
        return gears.map { gear ->
            gear to grid.getAdjacent(gear).mapNotNull { locToNumber[it] }.distinct().toList()
        }.filter {
            it.second.size == 2
        }.sumOf {
            it.second[0].value * it.second[1].value
        }
    }


    data class Number(val value: Int, val location: List<Vector2>)

    fun parseGrid(input: List<String>): IntGrid {
        val data = input.flatMap { line ->
            line.map { char ->
                when {
                    char.isDigit() -> char.digitToInt()
                    char == '.' -> 0
                    char == '*' -> -2
                    else -> -1
                }
            }
        }
        return IntGrid(data.toIntArray(), input[0].length, input.size)
    }

    fun lineToNumbers(row: Int, line: String): List<Number> {
        val matches = Regex("\\d+").findAll(line, 0)
        return matches.map { match ->
            Number(match.value.toInt(), match.range.map { Vector2(it, row)})
        }.toList()
    }

    fun lineToGears(row: Int, line: String): List<Vector2> {
        val matches = Regex("\\*").findAll(line, 0)
        return line.mapIndexedNotNull { index, c ->
            if (c == '*') {
                Vector2(index, row)
            } else null
        }
    }
}