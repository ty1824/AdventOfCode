package advent

object Day5 : AdventDay {
    override fun part1(input: List<String>): String {
        val (state, instructions) = parseInput(input)
        instructions.forEach { move ->
            (1..move.quantity).forEach {
                state[move.to] += state[move.from].removeLast()
            }
        }
        return state.map { it.last() }.joinToString("")
    }

    override fun part2(input: List<String>): String {
        val (state, instructions) = parseInput(input)
        instructions.forEach { move ->
            state[move.to] +=
                (1..move.quantity).map {
                    state[move.from].removeLast()
                }.reversed()
        }
        return state.map { it.last() }.joinToString("")
    }

    fun parseInput(input: List<String>): Pair<List<MutableList<Char>>, List<Move>> {
        val splitIndex = input.indexOf("")
        val unparsedState = input.take(splitIndex - 1)
        val unparsedInstructions = input.drop(splitIndex + 1)

        val state = List(9) { mutableListOf<Char>() }
        unparsedState.reversed().forEach { row ->
            row.chunked(4).forEachIndexed { index, box ->
                if (box[1].isLetter()) state[index] += box[1]
            }
        }
        val instructions = unparsedInstructions.map {
            val (quantity, from, to) = it.split("move ", " from ", " to ").drop(1)
            // Subtract 1 from columns to map to indices
            Move(quantity.toInt(), from.toInt()-1, to.toInt()-1)
        }
        return state to instructions
    }

    data class Move(val quantity: Int, val from: Int, val to: Int)

}