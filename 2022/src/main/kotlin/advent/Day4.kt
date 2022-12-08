package advent

object Day4 : AdventDay {
    override fun part1(input: List<String>): Int =
        input.count {
            val (one, two) = parse(it)
            one.contains(two) || two.contains(one)
        }

    override fun part2(input: List<String>): Int =
        input.count {
            val (one, two) = parse(it)
            one.overlaps(two) || two.overlaps(one)
        }

    private fun parse(line: String): List<Pair<Int, Int>> =
        line.split(',')
            .map { str ->
                str.splitToSequence('-')
                    .map(String::toInt)
                    .zipWithNext()
                    .first()
            }

    private fun Pair<Int, Int>.contains(other: Pair<Int, Int>) =
        this.first <= other.first && this.second >= other.second

    private fun Pair<Int, Int>.overlaps(other: Pair<Int, Int>) =
        (this.first >= other.first && this.first <= other.second) ||
                (this.second <= other.second && this.second >= other.first)

}