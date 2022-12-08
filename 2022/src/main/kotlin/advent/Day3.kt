package advent

object Day3 : AdventDay {
    override fun part1(input: List<String>): Int =
        input.sumOf { it.asCompartments().findDuplicate().priority() }

    override fun part2(input: List<String>): Int =
        input.asSequence().chunked(3).sumOf {
            it.findDuplicate().priority()
        }

    private fun String.asCompartments(): Pair<String, String> =
        (this.length / 2).let { this.substring(0, it) to this.substring(it)}

    private fun Pair<String, String>.findDuplicate(): Char =
        this.first.first { this.second.contains(it) }

    private fun List<String>.findDuplicate(): Char {
        val (one, two, three) = this
        return one.first { two.contains(it) && three.contains(it) }
    }

    private fun Char.priority(): Int =
        if (this.isLowerCase()) {
            this.code - 97 + 1
        } else {
            this.code - 65 + 27
        }
}