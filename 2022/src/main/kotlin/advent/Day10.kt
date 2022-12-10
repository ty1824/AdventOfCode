package advent

object Day10 : AdventDay {
    val interestingCycles = listOf(20, 60, 100, 140, 180, 220)

    override fun part1(input: List<String>): Any {
        val states = computeStates(input)
        return interestingCycles.map {
            val state = states[it]
            state.cycle * state.startX
        }.sum()
    }

    override fun part2(input: List<String>): Any {
        val states = computeStates(input)
        // Pixels distinct from cycles distinct from pixel location
        return (0..239).chunked(40).joinToString("\n") { row ->
            row.joinToString("") { cycle ->
                val state = states[cycle + 1]
                when {
                    // Range = sprite location
                    ((state.startX - 1)..(state.startX + 1)).contains(cycle % 40) -> "#"
                    else -> " "
                }
            }
        }
    }

    private fun computeStates(input: List<String>): List<State> =
        input.fold(listOf(State(0, 1, 1))) { acc, inst ->
            acc + acc.last().processInstruction(inst)
        }

    data class State(val cycle: Int, val startX: Int, val endX: Int)

    private fun State.processInstruction(instruction: String): List<State> {
        return when (instruction.substringBefore(' ')) {
            "noop" -> listOf(
                State(this.cycle + 1, this.endX, this.endX)
            )
            "addx" -> listOf(
                State(this.cycle + 1, this.endX, this.endX),
                State(this.cycle + 2, this.endX, this.endX + instruction.substringAfter(' ').toInt())
            )
            else -> throw RuntimeException("Invalid instruction $instruction")
        }
    }
}