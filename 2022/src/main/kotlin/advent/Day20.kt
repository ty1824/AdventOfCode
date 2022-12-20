package advent

import java.util.LinkedList

object Day20 : AdventDay {
    override fun part1(input: List<String>): Any {
        val data = parseInput(input)
        val mixed = data.mix(1)
        return getCoordinates(mixed).sum()
    }

    val decryptionKey = 811589153
    override fun part2(input: List<String>): Any {
        val data = parseInput(input).map { it * decryptionKey }
        val mixed = data.mix(10)
        return getCoordinates(mixed).sum()
    }

    fun getCoordinates(list: List<Long>): List<Long> {
        val zeroIndex = list.indexOf(0)
        return listOf(
            list[(zeroIndex + 1000) % list.size],
            list[(zeroIndex + 2000) % list.size],
            list[(zeroIndex + 3000) % list.size]
        )
    }

    fun List<Long>.mix(times: Int): List<Long> {
        val mixed = LinkedList(List(this.size) { index -> index.toLong() })
        repeat(times) {
            this.forEachIndexed { index, element ->
                val indexInMixed = mixed.indexOf(index.toLong())
                val valueInMixed = mixed.removeAt(indexInMixed)
                val newLocation = (((indexInMixed + element) % this.lastIndex) + this.lastIndex) % this.lastIndex
                mixed.add(newLocation.toInt(), valueInMixed)
            }
        }
        return mixed.map { this[it.toInt()] }
    }

    fun parseInput(input: List<String>): List<Long> = input.map { it.toLong() }
}