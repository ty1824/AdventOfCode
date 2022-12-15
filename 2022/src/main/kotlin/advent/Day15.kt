package advent

import kotlin.math.max
import kotlin.math.min

object Day15 : AdventDay {
    private const val resultY: Int =
        2000000
//        10 //sample

    override fun part1(input: List<String>): Any {
        val sensors = parseInput(input)
        val excludedRanges = sensors.mapNotNull { (sensor, range) ->
            val closestPointOnLine = Vector2(sensor.x, resultY)
            val distanceToLine = (closestPointOnLine - sensor).manhattanDistance()
            if (distanceToLine < range) {
                val extraRange = range - distanceToLine
                (sensor.x - extraRange)..(sensor.x + extraRange)
            } else {
                null
            }
        }
        return excludedRanges.countIncluded()
    }

    private const val maxSearch: Int =
        4000000 // realData
//        20 //sample

    override fun part2(input: List<String>): Any {
        val sensors = parseInput(input)
        val location = (0..maxSearch).asSequence().flatMap { y ->
            val excludedRanges = sensors.mapNotNull { (sensor, range) ->
                val closestPointOnLine = Vector2(sensor.x, y)
                val distanceToLine = (closestPointOnLine - sensor).manhattanDistance()

                if (distanceToLine < range) {
                    val extraRange = range - distanceToLine
                    val left = max(sensor.x - extraRange, 0)
                    val right = min(sensor.x + extraRange, maxSearch)
                    left..right
                } else {
                    null
                }
            }
            excludedRanges.getExcluded().map { Vector2(it, y) }
        }.first()

        return location.x * maxSearch.toLong() + location.y
    }

    private fun List<IntRange>.countIncluded(): Int =
        // First value in pair is the last visited value. Second value is the result
        this.toList()
            .sortedBy { it.first }
            .fold(Pair(Int.MIN_VALUE, 0)) { acc: Pair<Int, Int>, range: IntRange ->
                val (last, result) = acc
                if (last >= range.last) {
                    acc
                } else {
                    range.last to (result + range.last - max(last, range.first))
                }
            }.second

    private fun List<IntRange>.getExcluded(): List<Int> {
        // First value in pair is the last visited value. Second value is the result
        return this.toList()
            .sortedBy { it.first }
            .fold(Pair(0, listOf())) { acc: Pair<Int, List<Int>>, range: IntRange ->
                val (last, result) = acc
                if (last >= range.last) {
                    // We've already covered this range, ignore it
                    acc
                } else {
                    // Add the gap to the result if it exists, while skipping forward to the end of this range.
                    range.last to result + ((last + 1) until range.first).toList()
                }
            }.second
    }


    private fun parseInput(input: List<String>): List<Pair<Vector2, Int>> = input.map { parseLine(it) }

    private val lineRegex = Regex("Sensor at x=(-?\\d+), y=(-?\\d+): closest beacon is at x=(-?\\d+), y=(-?\\d+)")
    private fun parseLine(line: String): Pair<Vector2, Int>  {
        val (sensorX, sensorY, beaconX, beaconY) = lineRegex.matchEntire(line)!!.groupValues.drop(1)
        val beacon = Vector2(sensorX.toInt(), sensorY.toInt())
        val sensor = Vector2(beaconX.toInt(), beaconY.toInt())
        return beacon to (beacon - sensor).manhattanDistance()
    }
}