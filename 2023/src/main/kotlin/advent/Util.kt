package advent

import kotlin.math.sign

object Util {
    infix fun Long.pow(exponent: Int): Long {
        require(exponent >= 0) { "Exponent must be a non-negative integer" }

        var result: Long = 1

        repeat(exponent) {
            result *= this
        }

        return result
    }

    fun gcd(a: Long, b: Long): Long {
        return if (a == 0L) b else gcd(b % a, a)
    }

    // method to return LCM of two numbers
    fun lcm(a: Long, b: Long): Long {
        return a / gcd(a, b) * b
    }

    fun <T> Sequence<T>.step(stepSize: Int): Sequence<T> = sequence {
        var index = 0
        for (element in this@step) {
            if (index++ % stepSize == 0) {
                yield(element)
            }
        }
    }

    fun Sequence<Long>.derivatives(): Sequence<Long> = sequence {
        val seqs = mutableListOf(this@derivatives)
        fun getSeq(level: Int): Sequence<Long> {
            if (level >= seqs.size) {
                (seqs.size..level).forEach {
                    seqs += seqs[it - 1].windowed(2).map { (a, b) -> b - a }
                }
            }
            return seqs[level]
        }

        var level = 0
        while (true) {
            yield(getSeq(level++).first())
        }
    }

    fun Sequence<Long>.polynomialExpansion(): (Long) -> Long {
        val derivatives = this.derivatives().takeWhile { it != 0L }
        return { x ->
            derivatives.mapIndexed { index, derivative ->
                if (index > 0) {
                    (0 until index).fold(derivative) { acc, it -> acc * (x - it) } / index
                } else {
                    derivative
                }
            }.sum()
        }
    }



    fun rangeOf(
        from: Int,
        to: Int,
        step: Int = (to - from).sign,
        endInclusive: Boolean = true
    ): Sequence<Int> = sequence {
        var current = from
        do {
            yield(current)
            current += step
        } while (current < to)
        if (endInclusive && current == to && from != to) {
            yield(current)
        }
    }
}