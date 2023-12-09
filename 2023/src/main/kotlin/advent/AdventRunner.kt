@file:OptIn(ExperimentalTime::class)

package advent

import java.io.File
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

/**
 * Accepts 0, 1 or 2 arguments.
 *
 * 0 arguments -> Runs all 25 days
 * 1 argument -> Must be an integer from 1 to 25. Runs the specified day against the default input file.
 * 2 arguments -> First must be an integer from 1 to 25, the second must be a filepath within resources.
 *                Runs the specified day against the specified input file.
 */
fun main(args: Array<String>) {
    if (args.isEmpty()) {
        val times = mutableListOf<Duration>()
        val totalTime = measureTime {
            (1..25).forEach {
                runDay(it)?.let { time ->
                    times += time
                    println()
                }
            }
        }
        times.forEachIndexed { index, time ->
            println("  Day ${index + 1}: ${time.toString(DurationUnit.SECONDS, 3)}")
        }
        println("Total time to run all days: ${totalTime.toString(DurationUnit.SECONDS, 3)}")
    } else {
        val day = args[0].toInt()
        val filepath = if (args.size > 1) {
            args[1]
        } else {
            "day${day}.txt"
        }
        runDay(day, filepath)
    }
}

fun runDay(day: Int, inputPath: String = "day$day.txt"): Duration? {
    val solutionRunner = AdventDay::class.sealedSubclasses.firstOrNull {
        it.simpleName == "Day${day}"
    }?.objectInstance
    if (solutionRunner != null) {
        println("Running day $day on input $inputPath")
        val inputFile = File(AdventDay::class.java.getResource(inputPath)!!.toURI())
        val input = inputFile.readLines()


        println("Part 1:")
        val part1Time = measureTime {
            println(solutionRunner.part1(input))
        }
        println("time: ${part1Time.toString(DurationUnit.MILLISECONDS, 1)}")
        println()
        println("Part 2:")
        val part2Time = measureTime {
            println(solutionRunner.part2(input))
        }
        println("time: ${part2Time.toString(DurationUnit.MILLISECONDS, 1)}")
        return part1Time + part2Time
    } else {
        return null
    }
}