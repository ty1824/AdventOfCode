package advent

sealed interface AdventDay {
    fun part1(input: List<String>): Any

    fun part2(input: List<String>): Any
}