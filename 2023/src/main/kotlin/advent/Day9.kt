package advent

object Day9 : AdventDay {
    override fun part1(input: List<String>): Any {
        val lists = parseInput(input)
        return lists.sumOf {
            nextInSequence(it)
        }
    }

    override fun part2(input: List<String>): Any {
        val lists = parseInput(input)
        return lists.sumOf {
            prevInSequence(it)
        }
    }

    fun parseInput(input: List<String>): List<List<Long>> =
        input.map { line ->
            line.split(" ").map { it.toLong() }
        }

    fun nextInSequence(list: List<Long>, print: Boolean = false): Long {
        val cache = mutableMapOf<Pair<Int, Int>, Long>()
        val base = list.reversed()
        fun getValue(column: Int, row: Int): Long {
            return if (row > 0) {
                val coord = column to row
                val value = cache[coord]
                if (value != null) {
                    value
                } else {
                    val newValue = getValue(column, row - 1) - getValue(column + 1, row - 1)
                    cache[coord] = newValue
                    newValue
                }
            } else {
                if (column < base.size) {
                    base[column]
                } else 0

            }
        }
        if (print) println("${getValue(2, 0)} ${getValue(1, 0)} ${getValue(0, 0)}")
        var current = 1
        while (getValue(0, current) != 0L || getValue(1, current) != 0L) {
            if (print) println("${getValue(2, current)} ${getValue(1, current)} ${getValue(0, current)}")
            current++
        }
        if (print) println("${getValue(2, current)} ${getValue(1, current)} ${getValue(0, current)}")
        var next = 0L
        while (current >= 0) {
            next += getValue(0, current--)
        }
        if (print) println(next)
        return next
    }

    fun prevInSequence2(list: List<Long>, print: Boolean = false): Long {
        val cache = mutableMapOf<Pair<Int, Int>, Long>()
        val base = list.reversed()
        val max = list.size - 1
        fun getValue(column: Int, row: Int): Long {
            return if (row > 0) {
                val coord = column to row
                val value = cache[coord]
                if (value != null) {
                    value
                } else {
                    val newValue = getValue(column, row - 1) - getValue(column + 1, row - 1)
                    cache[coord] = newValue
                    newValue
                }
            } else {
                if (column < base.size) {
                    base[column]
                } else 0

            }
        }
        if (print) println("${getValue(max, 0)} ${getValue(max - 1, 0)} ${getValue(max - 2, 0)}")
        var current = 1
        while (getValue(max - current, current) != 0L || getValue(max - current - 1, current) != 0L) {
            if (print) println("${getValue(max - current, current)} ${getValue(max - current - 1, current)} ${getValue(max - current - 2, current)}")
            current++
        }
//        if (print) println("${getValue(max - current, current)} ${getValue(max - current - 1, current)} ${getValue(max - current - 2, current)}")
        var next = 0L
        while (current >= 0) {
            next = getValue(max - current, current--) - next
        }
        if (print) println(next)
        return next
    }

    fun prevInSequence(list: List<Long>, print: Boolean = false): Long {
        val cache = mutableMapOf<Pair<Int, Int>, Long>()
        fun getValue(column: Int, row: Int): Long {
            return if (row > 0) {
                val coord = column to row
                val value = cache[coord]
                if (value != null) {
                    value
                } else {
                    val left = getValue(column, row - 1)
                    val right = getValue(column + 1, row - 1)
                    val newValue = right - left
                    cache[coord] = newValue
                    newValue
                }
            } else {
                if (column < list.size && column >= 0) {
                    list[column]
                } else 0

            }
        }
        if (print) println("${getValue(0, 0)} ${getValue(1, 0)} ${getValue(2, 0)}")
        var current = 1
        while (current < list.size - 2) {
            if (print) println("${getValue(0, current)} ${getValue(1, current)} ${getValue(2, current)}")
            current++
        }
        if (print) println("${getValue(0, current)} ${getValue(1, current)} ${getValue(2, current)}")
        var next = 0L
        while (current >= 0) {
            next = getValue(0, current--) - next
        }
        if (print) println(next)
        return next
    }
}