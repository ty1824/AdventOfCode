package advent

object Day16 : AdventDay {
    override fun part1(input: List<String>): Any {
        val valves = ValveMap(parseInput(input))
        return valves.maxValueTagTeam(listOf(State("AA", 30)), valves.usefulValves - "AA", 0)
    }

    override fun part2(input: List<String>): Any {
        val valves = ValveMap(parseInput(input))
        return valves.maxValueTagTeam(listOf(State("AA", 26), State("AA", 26)), valves.usefulValves - "AA", 0)
    }

    data class State(val position: String, val timeRemaining: Int)
    class ValveMap(val map: Map<String, Valve>) {
        val usefulValves: Set<String> = map.filter { it.value.flow != 0 }.keys
        operator fun get(key: String): Valve = map[key]!!

        fun maxValueTagTeam(states: List<State>, toVisit: Set<String>, value: Int): Pair<Set<String>, Int> {
            if (states.isEmpty()) {
                return toVisit to value
            }
            val max = states.first()
            val remainder = states.drop(1)
            val possibleValves = timeToValve(max.position).filter { (valve, timeTo) ->
                toVisit.contains(valve) && timeTo + 1 < max.timeRemaining
            }
            return if (possibleValves.isNotEmpty()) {
                possibleValves.map { (valve, timeTo) ->
                    if (timeTo + 1 >= max.timeRemaining)
                        maxValueTagTeam(remainder, toVisit, value)
                    val newStateAfterOpening = State(valve, max.timeRemaining - (timeTo + 1))
                    val newStates = (remainder + newStateAfterOpening).sortedByDescending { it.timeRemaining }
                    val newValue = value + this[valve].flow * newStateAfterOpening.timeRemaining
                    maxValueTagTeam(newStates, toVisit - valve, newValue)
                }.maxBy { it.second }
            } else {
                return maxValueTagTeam(remainder, toVisit, value)
            }
        }

        private val timeToValveCache: MutableMap<String, Map<String, Int>> = mutableMapOf()
        private fun timeToValve(from: String): Map<String, Int> {
            return timeToValveCache.computeIfAbsent(from) {
                val visited = mutableMapOf(from to 0)
                var frontier = listOf(from)
                var time = 0
                while (frontier.any()) {
                    time++
                    val nextFrontier =
                        frontier.flatMap { this[it].leadsTo }
                            .distinct()
                            .filter { !visited.containsKey(it) }
                            .toList()
                    nextFrontier.forEach { visited[it] = time }
                    frontier = nextFrontier
                }
                visited.filter { usefulValves.contains(it.key) }.toMap()
            }
        }
    }

    data class Valve(val name: String, val flow: Int, val leadsTo: List<String>)

    private val lineRegex = Regex("Valve ([A-Z]*) has flow rate=(\\d+); (?:tunnels lead to valves|tunnel leads to valve) (.*)")
    private fun parseInput(input: List<String>): Map<String, Valve> =
        input.associate { line ->
            val match = lineRegex.matchEntire(line)!!.groupValues.drop(1)
            match[0] to Valve(match[0], match[1].toInt(), match[2].split(", "))
        }
}