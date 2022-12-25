@file:OptIn(ExperimentalTime::class)

package advent

import java.io.File
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        val totalTime = measureTime {
            (1..25).forEach {
                if (it != 8) runDay(it)
                println()
            }
        }
        println("Total time to run all days: ${totalTime.inWholeMilliseconds}ms")
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

fun runDay(day: Int, inputPath: String = "day$day.txt") {
    println("Running day $day on input $inputPath")
    val inputFile = File(AdventDay::class.java.getResource(inputPath)!!.toURI())
    val input = inputFile.readLines()
    val solutionRunner = AdventDay::class.sealedSubclasses.first {
        it.simpleName == "Day${day}"
    }.objectInstance!!

    println("Part 1:")
    val part1Time = measureTime {
        println(solutionRunner.part1(input))
    }
    println("time: ${part1Time.inWholeMilliseconds}ms")
    println()
    println("Part 2:")
    val part2Time = measureTime {
        println(solutionRunner.part2(input))
    }
    println("time: ${part2Time.inWholeMilliseconds}ms")

}