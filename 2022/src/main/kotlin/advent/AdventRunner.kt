package advent

import java.io.File

sealed interface AdventDay {
    fun part1(input: List<String>): Any

    fun part2(input: List<String>): Any
}

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
    println(solutionRunner.part1(input))

    println("Part 2:")
    println(solutionRunner.part2(input))
}