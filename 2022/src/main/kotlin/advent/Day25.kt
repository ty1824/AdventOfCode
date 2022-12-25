package advent

import java.math.BigInteger
import kotlin.math.pow

object Day25 : AdventDay {
    override fun part1(input: List<String>): Any {
        val result = input.sumOf { snafuToLong(it) }
        println(result)
        return longToSnafu(result)
    }

    override fun part2(input: List<String>): Any {
        return "Congratulations, there is no part 2"
    }



    fun snafuToLong(snafu: String): Long =
        (0..snafu.lastIndex).fold(0) { acc, index ->
            val factor = BigInteger.valueOf(5).pow(snafu.lastIndex - index).toLong()
            val modified = toLong(snafu[index])
            acc + modified * factor
        }

    fun toLong(char: Char): Long = when (char) {
        '=' -> -2L
        '-' -> -1L
        '0' -> 0L
        '1' -> 1L
        '2' -> 2L
        else -> throw RuntimeException("Invalid character: $char")
    }

    fun longToSnafu(number: Long, current: String = ""): String {
        if (number < 3) return toSnafuDigit(number) + current
        val modified = number + 2
        val div = modified / 5
        val rem = modified % 5
        return longToSnafu(div, toSnafuDigit(rem - 2) + current)
    }

    fun toSnafuDigit(number: Long): Char = when (number) {
        -1L -> '-'
        -2L -> '='
        0L -> '0'
        1L -> '1'
        2L -> '2'
        else -> throw RuntimeException("Invalid number: $number")
    }
}