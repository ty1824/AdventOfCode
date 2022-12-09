package advent

import java.io.File
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

sealed interface AdventDay {
    fun part1(input: List<String>): Any

    fun part2(input: List<String>): Any
}

@OptIn(ExperimentalTime::class)
fun main(args: Array<String>) {
    val day = if (args.size > 0) args[0] else {
        println("Enter the day you want to run")
        readLine()!!.toInt()
    }
    val inputFile = if (args.size > 1) File(args[1]) else File(AdventDay::class.java.getResource("day${day}.txt")!!.toURI())
    val input = inputFile.readLines()
    val solutionRunner = AdventDay::class.sealedSubclasses.first {
        it.simpleName == "Day${day}"
    }.objectInstance!!

    println("Part 1:")
    val part1Time = measureTime {
        println(solutionRunner.part1(input))
    }
    println("time: ${part1Time.inWholeMilliseconds}ms")

    println("Part 2:")
    val part2Time = measureTime {
        println(solutionRunner.part2(input))
    }
    println("time: ${part2Time.inWholeMilliseconds}ms")
}