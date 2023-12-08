package advent

import java.lang.Integer.max

object Day7 : AdventDay {
    override fun part1(input: List<String>): Any {
        val sorted = parseInput(input).sorted()
        println(sorted.joinToString("\n"))
        return sorted.foldIndexed(0) { index, accum, hand ->
            hand.bid * (index + 1) + accum
        }
    }

    override fun part2(input: List<String>): Any {
        val sorted = parseInput(input).map { JokerHand(it) }.sorted()
        println(sorted.joinToString("\n"))
        return sorted.foldIndexed(0) { index, accum, hand ->
            hand.bid * (index + 1) + accum
        }
    }

    interface Hand : Comparable<Hand> {
        val cards: String
        val bid: Int
        val type: HandType

        override fun compareTo(other: Hand): Int {
            val thisType = type
            val otherType = other.type
            return when {
                thisType.ordinal < otherType.ordinal -> 1
                thisType.ordinal > otherType.ordinal -> -1
                else -> {
                    cards.zip(other.cards).first { (left, right) ->
                        left != right
                    }.let { (left, right) ->
                        cardValue[left]!!.compareTo(cardValue[right]!!)
                    }
                }
            }
        }
    }

    class BasicHand(override val cards: String, override val bid: Int) : Hand {
        override val type: HandType by lazy { HandType.values().first { it.matchesHand(this) } }

        override fun toString(): String = "$cards $bid"
    }

    class JokerHand(from: BasicHand) : Hand {
        private val jokers = from.cards.count { it == 'J' }
        override val bid: Int = from.bid
        override val cards = from.cards.replace("J", "1")
        override val type by lazy {
            when (val count = jokers) {
                0 -> from.type
                1 -> when (from.type) {
                    HandType.FourOfAKind -> HandType.FiveOfAKind
                    HandType.ThreeOfAKind -> HandType.FourOfAKind
                    HandType.TwoPair -> HandType.FullHouse
                    HandType.OnePair -> HandType.ThreeOfAKind
                    HandType.HighCard -> HandType.OnePair
                    else -> throw RuntimeException("Can't promote ${from.type} with $count J")
                }
                2 -> when (from.type) {
                    HandType.FullHouse -> HandType.FiveOfAKind
                    HandType.TwoPair -> HandType.FourOfAKind
                    HandType.OnePair -> HandType.ThreeOfAKind // Two jacks are the one pair and become one other card
                    else -> throw RuntimeException("Can't promote ${from.type} with $count J")
                }
                3 -> when (from.type) {
                    HandType.FullHouse -> HandType.FiveOfAKind
                    HandType.ThreeOfAKind -> HandType.FourOfAKind
                    else -> throw RuntimeException("Can't promote ${from.type} with $count J")
                }
                4 -> when (from.type) {
                    HandType.FourOfAKind -> HandType.FiveOfAKind
                    else -> throw RuntimeException("Can't promote ${from.type} with $count J")
                }
                5 -> from.type
                else -> throw RuntimeException("Can't promote ${from.type} with $count J")
            }

        }

        override fun toString(): String = "$cards $bid"
    }

    enum class HandType {
        FiveOfAKind {
            override fun matchesHand(hand: BasicHand): Boolean =
                hand.cards.groupBy { it }.size == 1
        },
        FourOfAKind {
            override fun matchesHand(hand: BasicHand): Boolean {
                val groupBy = hand.cards.groupBy { it }.mapValues { it.value.size }
                return groupBy.any { it.value == 4}
            }
        },
        FullHouse {
            override fun matchesHand(hand: BasicHand): Boolean {
                val groupBy = hand.cards.groupBy { it }.mapValues { it.value.size }
                return groupBy.any { it.value == 3 } && groupBy.any { it.value == 2 }
            }
        },
        ThreeOfAKind {
            override fun matchesHand(hand: BasicHand): Boolean {
                val groupBy = hand.cards.groupBy { it }.mapValues { it.value.size }
                return groupBy.any { it.value == 3 }
            }
        },
        TwoPair {
            override fun matchesHand(hand: BasicHand): Boolean {
                val groupBy = hand.cards.groupBy { it }.mapValues { it.value.size }
                return groupBy.size == 3 && groupBy.any { it.value == 2 }
            }
        },
        OnePair {
            override fun matchesHand(hand: BasicHand): Boolean {
                val groupBy = hand.cards.groupBy { it }.mapValues { it.value.size }
                return groupBy.any { it.value == 2 }
            }
        },
        HighCard {
            override fun matchesHand(hand: BasicHand): Boolean = true
        };


        abstract fun matchesHand(hand: BasicHand): Boolean
    }

    val cardValue: Map<Char, Int> = mapOf(
        '1' to -1,
        '2' to 0,
        '3' to 1,
        '4' to 2,
        '5' to 3,
        '6' to 4,
        '7' to 5,
        '8' to 6,
        '9' to 7,
        'T' to 8,
        'J' to 9,
        'Q' to 10,
        'K' to 11,
        'A' to 12
    )

    fun parseInput(input: List<String>): List<BasicHand> =
        input.map {
            val (cards, bid) = it.split(" ")
            BasicHand(cards, bid.toInt())
        }
}