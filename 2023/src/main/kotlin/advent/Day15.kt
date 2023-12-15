package advent

object Day15 : AdventDay {
    override fun part1(input: List<String>): Any {
        val str = input[0]
        return str.splitToSequence(',').sumOf {
            hash(it)
        }
    }

    override fun part2(input: List<String>): Any {
        val map = HashMap()
        input[0].splitToSequence(',').map { parseOp(it) }.forEach { op ->
            when (op) {
                is SetOp -> map.set(op.label, op.lens)
                is RemoveOp -> map.remove(op.label)
            }
        }
        return map.focusingPower()
    }

    fun hash(str: String): Int =
        str.fold(0) { acc, c ->
            ((acc + c.code) * 17) % 256
        }

    val opRegex = Regex("([^-=]*)(-|=)(\\d?)")
    fun parseOp(op: String): Operation {
        val match = opRegex.matchAt(op, 0) ?: throw RuntimeException("No match for op: $op")
        val label = match.groups[1]!!.value
        val operator = match.groups[2]!!.value
        val lens = match.groups[3]?.value
        return if (operator == "=") {
            SetOp(label, lens!!.toInt())
        } else {
            RemoveOp(label)
        }
    }

    sealed interface Operation
    data class SetOp(val label: String, val lens: Int) : Operation
    data class RemoveOp(val label: String) : Operation

    class Entry(val label: String, val value: Int) {
        override fun hashCode(): Int = hash(label)
        override fun equals(other: Any?): Boolean = other is Entry && label == other.label
    }

    class HashMap {
        val buckets: Array<MutableList<Entry>> = Array(256) { mutableListOf() }

        fun set(label: String, value: Int) {
            val entry = Entry(label, value)
            val bucket = buckets[entry.hashCode()]
            val index = bucket.indexOf(entry)
            if (index >= 0) {
                bucket[index] = entry
            } else {
                bucket += entry
            }
        }

        fun remove(label: String) {
            val entry = Entry(label, -1)
            buckets[entry.hashCode()].remove(entry)
        }

        fun focusingPower(): Int =
            buckets.foldIndexed(0) { bucketIndex, sum, bucket ->
                sum + (bucketIndex + 1) * bucket.foldIndexed(0) { lensIndex, sum, lens ->
                    sum + (lensIndex + 1) * lens.value
                }
            }

        override fun toString() =
            buckets.joinToString("\n") {
                it.joinToString("|") { it.value.toString() }
            }.trim()
    }
}