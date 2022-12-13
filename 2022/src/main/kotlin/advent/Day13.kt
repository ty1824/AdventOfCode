package advent

object Day13 : AdventDay {
    val dividerPacketInput = listOf("[[2]]", "[[6]]")

    override fun part1(input: List<String>): Any {
        val packetPairs = parseInput(input)
        return packetPairs.mapIndexed { index, packets ->
            if (packets.first.compareTo(packets.second) <= 0) { index + 1 } else 0
        }.sum()
    }

    override fun part2(input: List<String>): Any {
        val dividerPackets = dividerPacketInput.map { parseContent(it).first }
        val packets: List<Content> = parseInput(input).flatMap { listOf(it.first, it.second) } + dividerPackets
        val sortedPackets = packets.sorted()
        return dividerPackets.map { sortedPackets.indexOf(it) + 1 }.reduce(Int::times)
    }

    sealed interface Content : Comparable<Content> {
       override fun compareTo(other: Content): Int =
            when {
                this is Num && other is Num -> this.compareTo(other)
                this is Num && other is Ls -> Ls(listOf(this)).compareTo(other)
                this is Ls && other is Num -> this.compareTo(Ls(listOf(other)))
                this is Ls && other is Ls -> this.compareTo(other)
                else -> throw RuntimeException("Invalid content comparison $this to $other")
            }
    }
    data class Num(val value: Int) : Content {
        fun compareTo(other: Num): Int = value.compareTo(other.value)
        override fun toString(): String = value.toString()
    }
    data class Ls(val values: List<Content>) : Content {
        fun compareTo(other: Ls): Int {
            val leftItr = values.iterator()
            val rightItr = other.values.iterator()
            while (leftItr.hasNext()) {
                if (!rightItr.hasNext()) return 1
                val nextLeft = leftItr.next()
                val nextRight = rightItr.next()
                val compareTo = nextLeft.compareTo(nextRight)
                if (compareTo != 0) return compareTo
            }
            // If the right container still has contents, we are in order, otherwise continue.
            return if (rightItr.hasNext()) -1 else 0
        }

        override fun toString(): String = "[${values.joinToString()}]"
    }

    fun parseInput(input: List<String>): List<Pair<Content, Content>> =
        input.windowed(2, 3).map { parseContent(it[0]).first to parseContent(it[1]).first }

    fun parseContent(raw: String): Pair<Content, Int> =
        when (raw.first()) {
            '[' -> parseList(raw)
            else -> {
                val toParse = raw.takeWhile { it.isDigit() }
                Num(toParse.toInt()) to toParse.length - 1
            }
        }

    fun parseList(raw: String): Pair<Ls, Int> {
        var current = 1
        val result = mutableListOf<Content>()
        while (raw[current] != ']') {
            when (raw[current]) {
                ',', ' ' -> { /* no-op */ }
                else -> {
                    val parseResult = parseContent(raw.substring(current))
                    result += parseResult.first
                    current += parseResult.second
                }

            }
            current++
        }
        return Ls(result.toList()) to current
    }
}