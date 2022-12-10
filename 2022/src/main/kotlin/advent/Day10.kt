package advent

object Day10 : AdventDay {
    val interestingCycles = listOf(20, 60, 100, 140, 180, 220)

    override fun part1(input: List<String>): Any {
        val states = computeStates(input)
        return interestingCycles.sumOf { cycle ->
            cycle * states[cycle - 1]
        }
    }

    override fun part2(input: List<String>): Any {
        val states = computeStates(input)
        // Pixels distinct from cycles distinct from pixel location
        return (1..240).chunked(40).joinToString("\n") { row ->
            row.joinToString("") { cycle ->
                val x = states[cycle - 1]
                when {
                    // Range = sprite location
                    ((x - 1)..(x + 1)).contains((cycle - 1) % 40) -> "#"
                    else -> " "
                }
            }
        }
    }

    private fun computeStates(input: List<String>): List<Int> =
        input.fold(listOf(1)) { acc, inst ->
            acc + acc.last().processInstruction(inst)
        }

    private fun Int.processInstruction(instruction: String): List<Int> {
        return when (instruction.substringBefore(' ')) {
            "noop" -> listOf(this)
            "addx" -> listOf(this, this + instruction.substringAfter(' ').toInt())
            else -> throw RuntimeException("Invalid instruction $instruction")
        }
    }
}