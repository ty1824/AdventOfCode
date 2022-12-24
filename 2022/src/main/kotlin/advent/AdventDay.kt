package advent

sealed interface AdventDay {
    val debugLevel: Int
        get() = 0

    fun part1(input: List<String>): Any

    fun part2(input: List<String>): Any

    fun debug(any: Any, level: Int = 1) { if (debugLevel >= level) print(any) }
    fun debugln(any: Any, level: Int = 1) { if (debugLevel >= level) println(any) }
    fun debugln(level: Int = 1) { if (debugLevel >= level) println() }
}