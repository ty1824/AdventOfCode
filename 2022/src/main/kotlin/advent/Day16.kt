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

        private val timeToValveCache: MutableMap<String, Map<String, Int>> = mutableMapOf()

        @Deprecated("Doesn't actually give the max value")
        fun greedyMax(at: String, timeLeft: Int, visited: List<String>): Int {
            println("Starting iteration at $at with $timeLeft time left.")
            if (timeLeft <= 1) return 0
            val newVisited = visited + at
            val timeToValves = timeToValve(at)
            if (timeToValves.values.min() > timeLeft) return 0
            val valueAtTime = valueAtTime(at, timeLeft).filter { !newVisited.contains(it.key) }
            // Get the highest value valve
            val (valve, value) = valueAtTime.entries.maxByOrNull { it.value }!!
            println("Moving from $at to $valve takes ${timeToValves[valve]!! + 1} minutes. Will release a total of $value pressure")
            return value + greedyMax(valve, timeLeft - (timeToValves[valve]!! + 1), newVisited)
        }

        /**
         * Only works for a single actor. Used to solve part 1.
         */
        fun maxValueGivenTime(position: String, time: Int, visited: List<String>): Pair<List<String>, Int> =
            timeToValve(position).filter { it.key != position && !visited.contains(it.key) }
                .map { (valve, timeTo) ->
                    if (timeTo + 1 >= time) visited to 0
                    else {
                        val timeLeftAfterOpening = time - (timeTo + 1)
                        val (nextVisited, nextMaxValue) = maxValueGivenTime(valve, timeLeftAfterOpening, visited + position)
                        nextVisited to this[valve].flow * timeLeftAfterOpening + nextMaxValue
                    }
                }.maxBy { it.second }

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

        private fun valueAtTime(position: String, time: Int): Map<String, Int> =
            timeToValve(position).mapValues { (valve, timeTo) ->
                if (timeTo + 1 >= time) 0
                else {
                    this[valve].flow * (time - (timeTo + 1))
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