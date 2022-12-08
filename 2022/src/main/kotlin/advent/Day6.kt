package advent

object Day6 : AdventDay {
    override fun part1(input: List<String>): Any {
        val data = input[0]
        return (0..(data.length - 4)).first {
            allUnique(data.substring(it, it + 4))
        } + 4
    }

    override fun part2(input: List<String>): Any {
        val data = input[0]
        return (0..(data.length - 14)).first {
            allUnique(data.substring(it, it + 14))
        } + 14
    }

    private fun allUnique(input: String): Boolean = input.toSet().size == input.count()
}