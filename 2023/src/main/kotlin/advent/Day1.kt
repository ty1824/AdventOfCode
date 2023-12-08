package advent

object Day1 : AdventDay {
    override fun part1(input: List<String>): Any =
        input.sumOf { line ->
            line.first { it.isDigit() }.digitToInt() * 10 + line.last { it.isDigit() }.digitToInt()
        }

    override fun part2(input: List<String>): Any =
        input.sumOf { line ->
            val transformed = transform(line)
            transformed.first() * 10 + transformed.last()
        }

    val digits = arrayOf("one", "two", "three", "four", "five", "six", "seven", "eight", "nine")
    val digitMap = digits.mapIndexed { index, it ->
        it to (index+1)
    }.toMap()
    val digitRegex = Regex(digits.joinToString("|"))

    fun lineValue(line: String): Int =
        line.first { it.isDigit() }.digitToInt() * 10 + line.last { it.isDigit() }.digitToInt()

    fun transform(line: String): List<Int> {
        return line.mapIndexedNotNull { index, _ ->
            digitAt(line, index)
        }
    }

    fun digitAt(line: String, index: Int): Int? {
        return if (line[index].isDigit()) {
            line[index].digitToInt()
        } else {
            val match = digitRegex.matchAt(line, index)
            match?.value?.let { digitMap[it] }
        }
    }


}