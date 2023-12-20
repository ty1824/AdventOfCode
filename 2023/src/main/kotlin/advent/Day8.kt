package advent

import advent.Util.lcm

object Day8 : AdventDay {
    override fun part1(input: List<String>): Any {
        val directions = parseInputDirections(input)
        val map = parseInputMap(input)
        var current = "AAA"
        var steps = 0
        var currentDirection = -1
        fun nextDirection(): Int {
            currentDirection += 1
            currentDirection %= directions.size
            return currentDirection
        }
        while (current != "ZZZ") {
            val nextDirection = nextDirection()
            if (directions[nextDirection]) {
                current = map[current]!!.first
            } else {
                current = map[current]!!.second
            }
            steps++
        }
        return steps
    }

    override fun part2(input: List<String>): Any {
        val directions = parseInputDirections(input)
        val map = parseInputMap(input)
        var current = map.keys.filter { it.endsWith("A") }
        return current.map {
            findFirstZ(it, 0, directions, map)
        }.fold(1L) { acc, num ->
            lcm(acc, num)
        }
    }

    fun findFirstZ(start: String, startDirection: Int, directions: List<Boolean>, map: Map<String, Pair<String, String>>): Long {
        var current = start
        var currentDirection = startDirection - 1
        fun nextDirection(): Int {
            currentDirection += 1
            currentDirection %= directions.size
            return currentDirection
        }
        var steps = 0L
        while (!current.endsWith("Z")) {
            val nextDirection = nextDirection()
            if (directions[nextDirection]) {
                current = map[current]!!.first
            } else {
                current = map[current]!!.second
            }
            steps++
        }
        return steps
    }



    fun parseInputDirections(input: List<String>): List<Boolean> =
        input[0].map { it == 'L' }


    fun parseInputMap(input: List<String>): Map<String, Pair<String, String>> {
        return input.drop(2).associate { line ->
            val (key, rest) = line.split(" = ")
            val (left, right) = Regex("\\((\\w+), (\\w+)\\)").matchEntire(rest)!!.groupValues.drop(1)
            key to (left to right)
        }
    }
}