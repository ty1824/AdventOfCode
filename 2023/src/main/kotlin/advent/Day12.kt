package advent

object Day12 : AdventDay {
    override fun part1(input: List<String>): Any {
        val conditions = parseInput(input)
        return conditions.sumOf {
            possibleConfigurations(it).size
        }
    }

    override fun part2(input: List<String>): Any {
        val conditions = parseInput(input).map { it.unfold() }
        return conditions.sumOf {
            possibleConfigurations(it).size
        }
    }

    class Condition(val state: String, val groups: List<Int>) {
        fun knownBroken(): Int = state.count { it == '#' }
        fun unknown(): Int = state.count { it == '?' }
        val maxGears: Int by lazy { groups.sum() }

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
            val candidateGroups = finalCandidate.split(Regex("\\.+")).filter { it.isNotEmpty() }.map { it.length }
//            println(candidateGroups)
            return groups == candidateGroups
        }
    }

    private fun possibleConfigurations(condition: Condition): List<String> {
        println(condition.state)
        val configurations: MutableList<String> = mutableListOf()
        breadthFirstSearch<String>(listOf(condition.state)) {
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

    fun parseInput(input: List<String>): List<Condition> = input.map {
        val (state, groups) = it.split(" ")
        Condition(state, groups.split(",").map(String::toInt))
    }
}