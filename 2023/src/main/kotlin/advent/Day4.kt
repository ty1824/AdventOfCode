package advent

import java.math.BigInteger

object Day4 : AdventDay {
    override fun part1(input: List<String>): Any {
        return parseInput(input).sumOf { card ->
            val winning = card.matchCount()
            if (winning == 0) {
                0
            } else {
                BigInteger.TWO.pow(winning - 1).toInt()
            }
        }
    }

    override fun part2(input: List<String>): Any {
        val cards = parseInput(input)
        val copies = mutableMapOf<Card, Int>()
        cards.forEachIndexed { index, card ->
            val cardCount = 1 + (copies[card] ?: 0)
            val matchCount = card.matchCount()
            for (i in 1 .. matchCount) {
                copies[cards[index + i]] = (copies[cards[index + i]] ?: 0) + cardCount
            }
        }
        return cards.size + copies.values.sum()
    }

    data class Card(val id: Int, val winnningNumbers: List<Int>, val myNumbers: List<Int>) {
        fun matchCount(): Int = myNumbers.filter { winnningNumbers.contains(it) }.size
    }

    data class ScratchEntry(val index: Int, var count: Int)

    fun parseInput(input: List<String>): List<Card> =
        input.map { line ->
            val (card, rest) = line.split(Regex(":\\s+"))
            val (winning, mine) = rest.split(Regex("\\s+\\|\\s+"))
            val whitespaceRegex = Regex("\\s+")
            Card(
                card.split(whitespaceRegex)[1].toInt(),
                winning.split(whitespaceRegex).map { it.toInt() },
                mine.split(whitespaceRegex).map { it.toInt() }
            )
        }
}