package advent

import java.util.LinkedList

object Day11 : AdventDay {

    override fun part1(input: List<String>): Any {
        val state = State1(parseInitialState1(input))
        repeat(20) {
            state.evaluateRound()
        }
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

    override fun part2(input: List<String>): Any {
        val state = State2(parseInitialState2(input))
        repeat(10000) { state.evaluateRound() }
        return state.timesInspected.sorted().takeLast(2).reduce(Long::times)
    }

    private fun parseInitialState2(input: List<String>): Array<Monkey2> {
        val worryValues: MutableList<WorryValue> = mutableListOf()
        fun createWorryValue(value: Int): WorryValue {
            val worryValue = WorryValue(value)
            worryValues += worryValue
            return worryValue
        }
        val monkeys = input.windowed(6, 7, true).map { lines ->
            Monkey2(
                items = LinkedList(lines[1].substringAfter("Starting items: ").split(", ").map { createWorryValue(it.toInt()) }),
                operation = parseOperation2(lines[2].substringAfter("Operation: new = ")),
                divisible = lines[3].substringAfter("Test: divisible by ").toInt(),
                ifTrue = lines[4].substringAfter("If true: throw to monkey ").toInt(),
                ifFalse = lines[5].substringAfter("If false: throw to monkey ").toInt()
            )
        }.toTypedArray()
        val divisibles = monkeys.map { it.divisible }
        worryValues.forEach { it.initModulusMap(divisibles) }
        return monkeys
    }

    private fun parseOperation2(input: String): (WorryValue) -> Unit {
        val tokens = input.split(' ')
        return if (tokens[2] == "old") {
            when (tokens[1]) {
                "+" -> { it -> it * 2 }
                "*" -> { it -> it.square() }
                else -> throw RuntimeException("Invalid operator: ${tokens[1]}")
            }
        } else {
            val constant = tokens[2].toInt()
            when (tokens[1]) {
                "+" -> { it -> it + constant }
                "*" -> { it -> it * constant }
                else -> throw RuntimeException("Invalid operator: ${tokens[1]}")
            }
        }
    }

    class State2(val monkeys: Array<Monkey2>) {
        val timesInspected: LongArray = LongArray(monkeys.size)

        fun evaluateRound() {
            (0..monkeys.lastIndex).forEach(this::evaluateTurn)
        }

        private fun evaluateTurn(index: Int) {
            val monkey = monkeys[index]
            while (monkey.items.isNotEmpty()) {
                val item = monkey.items.removeFirst()
                monkey.operation(item)
                timesInspected[index]++
                val throwTarget = monkey.checkThrowTarget(item)
                monkeys[throwTarget].items += item
            }
        }
    }

    /**
     * When adding, a number becomes divisible by some other if its modulus becomes 0
     * Addition may remove divisible status. If the addend modulo the other number is 0, the number retains divisible status.
     * When multiplying, a number becomes divisible by some other if the multiplicand is divisible by the other
     * Multiplying does not change divisible status.
     *
     * e.g. 13 is not divisible by 3.
     * 13 + 2 is divisible by 3 (15 % 3 is 0)
     * 13 * 2 is not divisible by 3 (2 is not divisible by 3)
     * 15 * 342 is divisible by 3 (5130 % 3 is 0)
     * 5130 + 2 is not divisible by 3.
     *
     * Ok, so what happens when multiplying something that is not divisible (mod is nonzero)
     *
     * 13 is not divisible by 11 (modulus = 2)
     * 13 * 19 = 247 (modulus = 5, which is 2 * 19 % 11)
     *
     * https://en.wikipedia.org/wiki/Modular_arithmetic
     */
    class WorryValue(private val initial: Int) {
        private lateinit var divisors: List<Int>
        private lateinit var modulusMap: MutableMap<Int, Int>

        fun initModulusMap(divisors: List<Int>) {
            this.divisors = divisors
            this.modulusMap = divisors.associateWith { initial % it }.toMutableMap()
        }

        fun square() {
            this.modulusMap.replaceAll { key, value -> (value * value) % key }
        }

        operator fun plus(other: Int) {
            this.modulusMap.replaceAll { key, value -> (value + other) % key }
        }

        operator fun times(other: Int) {
            this.modulusMap.replaceAll { key, value -> (value * other) % key }
        }

        fun isDivisibleBy(num: Int): Boolean = modulusMap[num] == 0
    }

    data class Monkey2(
        val items: LinkedList<WorryValue>,
        val operation: (WorryValue) -> Unit,
        val divisible: Int,
        val ifTrue: Int,
        val ifFalse: Int
    ) {
        fun checkThrowTarget(worry: WorryValue): Int =
            if (worry.isDivisibleBy(divisible))
                this.ifTrue
            else
                this.ifFalse
    }
}