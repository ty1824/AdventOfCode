package advent

import advent.Direction.*

object Day24 : AdventDay {
    override fun part1(input: List<String>): Any {
        val map = parseInput(input)
        val start = State(map.entry, 0, listOf(map.exit))
        val path = optimalPath(map, start)
        return path.last().time
    }

    override fun part2(input: List<String>): Any {
        val map = parseInput(input)
        val start = State(map.entry, 0, listOf(map.exit, map.entry, map.exit))
        val path = optimalPath(map, start)
        return path.last().time
    }

    data class State(val position: Vector2, val time: Int, val destinations: List<Vector2>)
    data class ValleyMap(
        val blizzards: Map<Vector2, List<Direction>>,
        val entry: Vector2,
        val exit: Vector2,
        val dimensions: Vector2
    ) {
        fun next(): ValleyMap = ValleyMap(blizzards.flatMap { (location, blizzards) ->
            blizzards.map { blizzard: Direction ->
                val candidateLocation = location + blizzard.vector
                if (!isOnMap(candidateLocation)) {
                    when (blizzard) {
                        Left -> Vector2(dimensions.x - 1, candidateLocation.y)
                        Right -> Vector2(1, candidateLocation.y)
                        Up -> Vector2(candidateLocation.x, dimensions.y - 1)
                        Down -> Vector2(candidateLocation.x, 1)
                    } to blizzard
                } else {
                    candidateLocation to blizzard
                }
            }
        }.groupBy({ it.first }) { it.second }, entry, exit, dimensions)

        fun isOnMap(position: Vector2): Boolean =
                (position.x > 0 && position.y > 0 && position.x < dimensions.x && position.y < dimensions.y)

        fun toMapString(): String {
            val lines = mutableListOf<StringBuilder>()
            return (Vector2(0, 0)..dimensions).joinToString("\n") { row -> row.joinToString("") { point -> when {
                point == entry || point == exit -> "."
                point.x == 0 || point.y == 0 || point.x == dimensions.x || point.y == dimensions.y -> "#"
                blizzards[point] != null && blizzards[point]!!.size > 1 -> blizzards[point]!!.size.toString()
                blizzards[point] != null -> directionString(blizzards[point]!!.first()!!)
                else -> "."
            } } }
        }
    }

    fun directionString(direction: Direction): String = when(direction) {
        Up -> "^"
        Down -> "v"
        Left -> "<"
        Right -> ">"
    }

    private fun getMovePriority(position: Vector2, target: Vector2): List<Direction?> {
        val delta = target - position
        return (Direction.values().toList() + null).sortedByDescending { if (it != null) (delta * it.vector).linearMagnitude() else 0 }
    }

    private fun optimisticTime(state: State, targets: List<Vector2>): Int =
        targets.fold(state.position to state.time) { (last, total), next ->
            next to total + (next - last).manhattanDistance()
        }.second

    private fun optimalPath(map: ValleyMap, state: State): List<State> =
        timeToAllDestinationsRecursive(mutableMapOf(0 to map), mutableSetOf(state), listOf(state), Int.MAX_VALUE)!!

    private fun timeToAllDestinationsRecursive(mapAtTime: MutableMap<Int, ValleyMap>, visitedStates: MutableSet<State>, states: List<State>, minTime: Int): List<State>? {
        fun getMapAtTime(time: Int): ValleyMap = mapAtTime.computeIfAbsent(time) { getMapAtTime(time - 1).next() }
        val lastState = states.last()
        val destinations = lastState.destinations
        return if (destinations.isEmpty()) {
            // We've found a solution
            debugln("Candidate found: ${states.last().time}")
            states
        } else {
            val nextMap = getMapAtTime(lastState.time + 1)
            val moves = getMovePriority(lastState.position, destinations.first())
            val newStates = moves.mapNotNull {
                if (it != null) {
                    val position = lastState.position + it.vector
                    if (position == destinations.first()) {
                        State(position, lastState.time + 1, destinations.drop(1))
                    } else {
                        State(position, lastState.time + 1, destinations)
                    }
                } else {
                    State(lastState.position, lastState.time + 1, lastState.destinations)
                }
            }.filter {
                val notVisited = !visitedStates.contains(it)
                val possiblyOptimal = optimisticTime(it, destinations) < minTime
                val isSpecialTile = it.position == nextMap.exit || it.position == nextMap.entry
                val legalMove = nextMap.isOnMap(it.position) && !nextMap.blizzards.containsKey(it.position)
                notVisited && possiblyOptimal && (isSpecialTile || legalMove)
            }
            var newMinTime = minTime
            visitedStates += newStates
            newStates.mapNotNull { state ->
                val result = timeToAllDestinationsRecursive(mapAtTime, visitedStates, states + state, newMinTime)
                if (result != null) newMinTime = result!!.lastIndex
                result
            }.minByOrNull { it.lastIndex }
        }
    }

    fun parseInput(input: List<String>): ValleyMap =
        ValleyMap(input.flatMapIndexed { y, row -> row.mapIndexedNotNull { x, char ->
            val location = Vector2(x, y)
            when (char) {
                '<' -> location to listOf(Left)
                '>' -> location to listOf(Right)
                '^' -> location to listOf(Up)
                'v' -> location to listOf(Down)
                else -> null
            }
        }}.toMap(), Vector2(1, 0), Vector2(input[0].lastIndex - 1, input.lastIndex), Vector2(input[0].lastIndex, input.lastIndex))
}