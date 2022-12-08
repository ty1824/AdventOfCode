package advent

object Day1 : AdventDay {
    override fun part1(input: List<String>): Int {
        val calories = getCalories(input)
        return calories.max()
    }

    override fun part2(input: List<String>): Int {
        val calories = getCalories(input)
        return calories.sorted().takeLast(3).sum()
    }

    private fun getCalories(input: Iterable<String>): List<Int> {
        val calories = mutableListOf(0)
        input.forEach { element ->
            if (element.isBlank())
                calories += 0
            else {
                calories[calories.lastIndex] = calories[calories.lastIndex] + element.toInt()
            }
        }
        return calories.toList()
    }
}

