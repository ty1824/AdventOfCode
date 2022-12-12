package advent

import java.io.File
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@OptIn(ExperimentalTime::class)
fun main(args: Array<String>) {
    val day = if (args.size > 0) args[0] else {
        println("Enter the day you want to run")
        readLine()!!.toInt()
    }
    val filepath = if (args.size > 1) { args[1] } else { "day${day}.txt" }
    val inputFile = File(AdventDay::class.java.getResource(filepath)!!.toURI())
    val input = inputFile.readLines()
    val solutionRunner = AdventDay::class.sealedSubclasses.first {
        it.simpleName == "Day${day}"
    }.objectInstance!!

    println("Running day $day on input $filepath")

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