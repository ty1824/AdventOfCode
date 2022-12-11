@file:OptIn(ExperimentalTime::class)

package advent

import java.math.BigInteger
import java.util.LinkedList
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

object Day11 : AdventDay {

    override fun part1(input: List<String>): Any {
        val state = State1(parseInitialState1(input))
        repeat(20) {
            state.evaluateRound()
        }
        println(state.timesInspected.toList())
        return state.timesInspected.sorted().takeLast(2).reduce(Int::times)
    }

    private fun parseInitialState1(input: List<String>): Array<Monkey1> =
        input.windowed(6, 7, true).map { lines ->
            Monkey1(
                items = LinkedList(lines[1].substringAfter("Starting items: ").split(", ").map { it.toInt() }),
                operation = parseOperation1(lines[2].substringAfter("Operation: new = ")),
                divisible = lines[3].substringAfter("Test: divisible by ").toInt(),
                ifTrue = lines[4].substringAfter("If true: throw to monkey ").toInt(),
                ifFalse = lines[5].substringAfter("If false: throw to monkey ").toInt()
            )
        }.toTypedArray()

    private fun parseOperation1(input: String): (Int) -> Int {
        val tokens = input.split(' ')
        val operator: (Int, Int) -> Int = when (tokens[1]) {
            "+" -> Int::plus
            "*" -> Int::times
            else -> throw RuntimeException("Failed to parse operator: ${tokens[1]}")
        }
        val left = tokens[0].toIntOrNull()
        val right = tokens[2].toIntOrNull()
        return { operator(left ?: it, right ?: it) }
    }

    data class Monkey1(
        val items: LinkedList<Int>,
        val operation: (Int) -> Int,
        val divisible: Int,
        val ifTrue: Int,
        val ifFalse: Int
    ) {
        fun checkThrowTarget(worry: Int): Int =
            if (worry % divisible == 0)
                this.ifTrue
            else
                this.ifFalse
    }

    class State1(val monkeys: Array<Monkey1>) {
        val timesInspected: IntArray = IntArray(monkeys.size)

        fun evaluateRound() {
            (0..monkeys.lastIndex).forEach(this::evaluateTurn)
        }

        private fun evaluateTurn(index: Int) {
            val monkey = monkeys[index]
            while (monkey.items.isNotEmpty()) {
                val initialItemWorry = monkey.items.removeFirst()
                val inspectionWorry = monkey.operation(initialItemWorry)
                val postInspectionWorry = inspectionWorry / 3
                timesInspected[index]++
                val throwTarget = monkey.checkThrowTarget(postInspectionWorry)
                monkeys[throwTarget].items += postInspectionWorry
            }
        }
    }


    var totalInspectionTime = Duration.ZERO
    var totalThrowTime = Duration.ZERO
    override fun part2(input: List<String>): Any {
        val state = State2(parseInitialState2(input))
        repeat(100) { round ->
            state.evaluateRound()
            println("Round: $round, Inspection: $totalInspectionTime, Throw: $totalThrowTime")
//            totalInspectionTime = Duration.ZERO
//            totalThrowTime = Duration.ZERO
        }
        return state.timesInspected.sorted().takeLast(2).reduce(Long::times)
    }

    private fun parseInitialState2(input: List<String>): Array<Monkey2> =
        input.windowed(6, 7, true).map { lines ->
            Monkey2(
                items = LinkedList(lines[1].substringAfter("Starting items: ").split(", ").map { WorryNumber(listOf(it.toBigInteger())) }),
                operation = parseOperation2(lines[2].substringAfter("Operation: new = ")),
                divisible = lines[3].substringAfter("Test: divisible by ").toBigInteger(),
                ifTrue = lines[4].substringAfter("If true: throw to monkey ").toInt(),
                ifFalse = lines[5].substringAfter("If false: throw to monkey ").toInt()
            )
        }.toTypedArray()

    private fun parseOperation2(input: String): (WorryNumber) -> WorryNumber {
        val tokens = input.split(' ')
        val operator: (WorryNumber, WorryNumber) -> WorryNumber = when (tokens[1]) {
            "+" -> WorryNumber::plus
            "*" -> WorryNumber::times
            else -> throw RuntimeException("Failed to parse operator: ${tokens[1]}")
        }
        val left = tokens[0].toBigIntegerOrNull()?.let { WorryNumber(listOf(it)) }
        val right = tokens[2].toBigIntegerOrNull()?.let { WorryNumber(listOf(it)) }
        return { operator(left ?: it, right ?: it) }
    }

    class State2(val monkeys: Array<Monkey2>) {
        val timesInspected: LongArray = LongArray(monkeys.size)

        fun evaluateRound() {
            (0..monkeys.lastIndex).forEach(this::evaluateTurn)
        }

        private fun evaluateTurn(index: Int) {
            val monkey = monkeys[index]
            while (monkey.items.isNotEmpty()) {
                val initialItemWorry = monkey.items.removeFirst()
                val (postInspectionWorry, inspectionTime) = measureTimedValue { monkey.operation(initialItemWorry) }
                totalInspectionTime += inspectionTime
                timesInspected[index]++
                val (throwTarget, throwTime) = measureTimedValue { monkey.checkThrowTarget(postInspectionWorry) }
                totalThrowTime += throwTime
                monkeys[throwTarget].items += postInspectionWorry
            }
        }
    }

    class WorryNumber(val numbers: List<BigInteger>) {

        fun getRaw(): BigInteger = numbers.reduce(BigInteger::times)

        fun plus(other: WorryNumber): WorryNumber = WorryNumber(listOf(getRaw() + other.getRaw()))

        fun times(other: WorryNumber): WorryNumber = WorryNumber(this.numbers + other.numbers)

        fun isDivisibleBy(num: BigInteger): Boolean = numbers.any { it % num == BigInteger.ZERO}
    }

    data class Monkey2(
        val items: LinkedList<WorryNumber>,
        val operation: (WorryNumber) -> WorryNumber,
        val divisible: BigInteger,
        val ifTrue: Int,
        val ifFalse: Int
    ) {
        fun checkThrowTarget(worry: WorryNumber): Int =
            if (worry.isDivisibleBy(divisible))
                this.ifTrue
            else
                this.ifFalse
    }
}