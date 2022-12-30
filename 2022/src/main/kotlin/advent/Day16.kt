package advent

import kotlin.math.max

object Day16 : AdventDay {
    override fun part1(input: List<String>): Any {
        val valves = ValveMap(parseInput(input))
        return valves.maxValueDfs(listOf(ValvePosition("AA", 30)))
    }

    override fun part2(input: List<String>): Any {
        val valves = ValveMap(parseInput(input))
        return valves.maxValueDfs(listOf(ValvePosition("AA", 26), ValvePosition("AA", 26)))
    }

    data class ValvePosition(val valve: String, val timeRemaining: Int)
    class ValveMap(val map: Map<String, Valve>) {
        val usefulValves: Set<String> = map.filter { it.value.flow != 0 }.keys
        operator fun get(key: String): Valve = map[key]!!

        fun maxValueAt(position: ValvePosition, toVisit: Iterable<String>): Int {
            val timeTo = timeToValve(position.valve)
            return toVisit.sumOf { max(get(it).flow * (position.timeRemaining - timeTo[it]!!), 0) }
        }

        data class DfsSearchState(val currentValves: List<ValvePosition>, val totalPressure: Int, val toVisit: Set<String>, val previous: DfsSearchState?)
        fun maxValueDfs(startingFrom: List<ValvePosition>): Int {
            var currentMaxCandidate = DfsSearchState(listOf(), 0, setOf(), null)
            depthFirstSearch(DfsSearchState(startingFrom, 0, usefulValves, null)) { state ->
                if (state.currentValves.isEmpty()) {
                    if (state.totalPressure > currentMaxCandidate.totalPressure) {
                        debugln("Candidate found: ${state.totalPressure}")
                        currentMaxCandidate = state
                    }
                    listOf()
                } else {
                    val max = state.currentValves.first()
                    val remainder = state.currentValves.drop(1)
                    timeToValve(max.valve).asSequence()
                        .filter { (valve, timeTo) -> // Only keep valves that we can reach in time
                            state.toVisit.contains(valve) && timeTo < max.timeRemaining
                        }.map { (valve, timeTo) -> // Convert to next state
                            val newPosition = ValvePosition(valve, max.timeRemaining - timeTo)
                            val newPositionsSorted = (remainder + newPosition).sortedByDescending { it.timeRemaining }
                            val newTotalPressure = state.totalPressure + this[valve].flow * newPosition.timeRemaining
                            val newToVisit = state.toVisit - newPosition.valve
                            val maxVals: Int = newPositionsSorted.sumOf {
                                maxValueAt(it, newToVisit)
                            }
                            DfsSearchState(newPositionsSorted, newTotalPressure, newToVisit, state) to maxVals
                        }.filter { (newState, maxPressure) -> // Filter out states that can not beat the current best
                            newState.totalPressure + maxPressure > currentMaxCandidate.totalPressure
                        }.map { it.first }
                        .ifEmpty { sequenceOf(DfsSearchState(remainder, state.totalPressure, state.toVisit, state)) }
                        .toList()
                }
            }
            return currentMaxCandidate.totalPressure
        }

        private val timeToValveCache: MutableMap<String, Map<String, Int>> = mutableMapOf()
        private fun timeToValve(from: String): Map<String, Int> {
            return timeToValveCache.computeIfAbsent(from) {
                val visited = mutableMapOf(from to 0)
                var time = 1
                breadthFirstSearch(listOf(from), beforeIteration = { time++ }) { valve ->
                    val nextValves = this[valve].leadsTo.filter { nextValve -> !visited.containsKey(nextValve) }
                    nextValves.forEach { nextValve ->
                        visited[nextValve] = time
                    }
                    nextValves
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