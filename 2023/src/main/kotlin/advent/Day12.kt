package advent

import java.lang.Integer.min

object Day12 : AdventDay {
    override val debugLevel: Int
        get() = 0

    override fun part1(input: List<String>): Any {
        val conditions = parseInput(input)
        var i = 0
        return conditions.sumOf {
            val count = newConfigurations(it)
//            println("Condition ${i++}: $count")
            count
        }
    }

    override fun part2(input: List<String>): Any {
        val conditions = parseInput(input).map { it.unfold() }
        var i = 0
        return conditions.sumOf {
            val count = newConfigurations(it)
//            println("Condition ${i++}: $count, (${it.cacheHits} / ${it.totalCalls})")
            count
        }
    }

    class Condition(val state: String, val groups: IntArray) {
        fun knownBroken(): Int = state.count { it == '#' }
        fun unknown(): Int = state.count { it == '?' }
        val maxGears: Int by lazy { groups.sum() }
        val maxStartPoint: IntArray
        val nextPound: IntArray = List(state.length) { index ->
            state.indexOf('#', index)
        }.toIntArray()
        val maxGroupLengthAt: IntArray = List(state.length) { index ->
            Regex("([#?]+)(?:[.?]|$)").matchAt(state, index)?.let { it.groupValues[1] }?.length ?: 0
        }.toIntArray()

        init {
            val reversed = state.reversed()
            val reversedGroups = groups.reversed()
            val startPoints = reversedGroups.foldIndexed(listOf<Int>()) { index, acc, group ->
                val minStartPoint = acc.lastOrNull()?.let { it + 1 } ?: 0
                val pattern = "(?:^|[?.])(${"[?#]".repeat(reversedGroups[index])})(?:$|[?.])"
                val match = Regex(pattern).find(reversed, minStartPoint)
                acc + match!!.groups[1]!!.range.last
            }
            maxStartPoint = startPoints.reversed().map { state.length - 1 - it}.toIntArray()
        }

        fun maxStartPoint(groupIndex: Int): Int = maxStartPoint[groupIndex]

        fun unfold(): Condition =
            Condition("$state?$state?$state?$state?$state", groups + groups + groups + groups + groups)

        fun validState(candidate: String): Boolean {
            if (candidate.count { it == '#'} > maxGears) {
//                println("$candidate ignored due to max gears $maxGears")
                return false
            }

            val largeGroups = candidate.split(Regex("[?#]+"))
            if (largeGroups.filter { it.indexOf('#') >= 0 }.size > maxGears) {
                return false
            }

            return true
        }

        fun validFinalState(finalCandidate: String): Boolean {
            val candidateGroups = finalCandidate.split(Regex("\\.+"))
                .filter { it.isNotEmpty() }
                .map { it.length }
                .toIntArray()
//            println(candidateGroups)
            return groups == candidateGroups
        }

        var totalCalls: Long = 0L
        var cacheHits: Long = 0L
        val cache: MutableMap<Pair<Int, Int>, Long> = mutableMapOf()
        fun groupConfigurations(start: Int, groupIndex: Int): Long {
            val key = start to groupIndex
            if (!cache.containsKey(key)) {
                cache[key] = if (groupIndex == groups.size) {
                    if (start >= state.length || !state.substring(start).contains('#')) {
                        1L
                    } else {
                        0L
                    }
                } else {
                    val validGroupLocations = validGroupLocations(this, start, groupIndex)
//                    debugln("Valid locations at $start: (${validGroupLocations.joinToString(", ")})")
                    var sum = 0L
                    for (i in validGroupLocations) {
                        val groupSize = groups[groupIndex]
                        sum += groupConfigurations(i + groupSize + 1, groupIndex + 1)
                    }
                    sum
                }
            } else {
                cacheHits++
            }
            totalCalls++
            return cache[key]!!
        }
    }

    private fun possibleConfigurations(condition: Condition): List<String> {
        println(condition.state)
        val configurations: MutableList<String> = mutableListOf()

        depthFirstSearch(condition.state) {
            when {
                it.indexOf('?') == -1 -> {
                    // No more missing data, submit string if it's valid
                    if (condition.validFinalState(it)) {
                        configurations += it
                    }
                    listOf()
                }
                it.count { char -> char == '#' } >= condition.maxGears -> listOf(it.replace('?', '.'))
                else -> {
                    val candidates = listOf(it.replaceFirst('?', '#'), it.replaceFirst('?', '.'))
                    val submitting = candidates.filter(condition::validState)
                    submitting
                }
            }
        }
        return configurations
    }

    fun newConfigurations(condition: Condition): Long {
        return condition.groupConfigurations(0, 0)
    }

    fun stringFromConfig(condition: Condition, config: List<Int>): String =
        config.foldIndexed(condition.state) { index, acc, groupIndex ->
            val groupLength = condition.groups[index]
            acc.replaceRange(groupIndex, groupIndex + groupLength, "#".repeat(groupLength)).replace('?', '.')
        }

    fun groupConfigurations(condition: Condition, start: Int, groupIndex: Int): Long {
        return if (groupIndex == condition.groups.size) {
            if (start >= condition.state.length || !condition.state.substring(start).contains('#')) {
                1L
            } else {
                0L
            }
        } else {
            val validGroupLocations = validGroupLocations(condition, start, groupIndex)
//            debugln("Valid locations at $start: (${validGroupLocations.joinToString(", ")})")
            var sum = 0L
            for (i in validGroupLocations) {
                val groupSize = condition.groups[groupIndex]
                sum += groupConfigurations(condition, i + groupSize + 1, groupIndex + 1)
            }
            sum
        }
    }

    fun validGroupLocations(condition: Condition, start: Int, groupIndex: Int): IntArray {
        val state = condition.state
        val length = state.length
        val nextPound = condition.nextPound[start]
        val groupLength = condition.groups[groupIndex]
        val end = if (nextPound < 0) {
            condition.maxStartPoint[groupIndex]
        } else {
            min(nextPound, condition.maxStartPoint[groupIndex])
        }
        val result = IntArray(end - start + 1)
        var index = 0
        for (current in start..end) {
            val next = current + groupLength
            val validState =
                condition.maxGroupLengthAt[current] >= groupLength
                    && (next == length || state[next] != '#')
            if (validState) {
                result[index++] = current
            }
        }
        return result.copyOf(index)
    }

    fun parseInput(input: List<String>): Sequence<Condition> = input.asSequence().map {
        val (state, groups) = it.split(" ")
        Condition(state, groups.split(",").map(String::toInt).toIntArray())
    }
}