package advent

object Day2 : AdventDay {
    private val rps = mapOf('X' to 'A', 'Y' to 'B', 'Z' to 'C')
    private val beats = mapOf('A' to 'C', 'B' to 'A', 'C' to 'B')
    private val beatenBy = beats.entries.associateBy({ it.value } ) { it.key }

    private fun pointsForChoice(char: Char): Int = when (char) {
        'A' -> 1
        'B' -> 2
        'C' -> 3
        else -> throw RuntimeException("oops")
    }

    override fun part1(input: List<String>): Int {
        return input.sumOf {
            val other = it[0]
            val choice = rps[it[2]]!!
            val winPoints = when {
                beats[choice] == other -> 6
                beats[other] == choice -> 0
                else -> 3
            }
            pointsForChoice(choice) + winPoints
        }
    }

    override fun part2(input: List<String>): Int {
        return input.sumOf {
            val other = it[0]
            when (it[2]) {
                'X' -> pointsForChoice(beats[other]!!)
                'Y' -> 3 + pointsForChoice(other)
                'Z' -> 6 + pointsForChoice(beatenBy[other]!!)
                else -> throw RuntimeException("Bad input $it")
            }
        }
    }
}