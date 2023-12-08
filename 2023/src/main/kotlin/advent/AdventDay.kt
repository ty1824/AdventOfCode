package advent

sealed interface AdventDay {
    companion object {
        var globalDebugLevel = 0
    }

    val debugLevel: Int
        get() = globalDebugLevel

    fun part1(input: List<String>): Any

    fun part2(input: List<String>): Any

    fun debug(any: Any, level: Int = 1) { if (debugLevel >= level) print(any) }
    fun debugln(any: Any, level: Int = 1) { if (debugLevel >= level) println(any) }
    fun debugln(level: Int = 1) { if (debugLevel >= level) println() }
}