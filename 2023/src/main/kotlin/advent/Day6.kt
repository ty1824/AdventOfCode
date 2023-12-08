package advent

object Day6 : AdventDay {
    override fun part1(input: List<String>): Any {
        val races = parseInput1(input)
        return races.map {
            waysToRecord(it)
        }.fold(1) { acc, ways -> acc * ways}
    }

    override fun part2(input: List<String>): Any {
        val race = parseInput2(input)
        return waysToRecord(race)
    }

    class RaceDetails(val time: Long, val distance: Long)

    fun waysToRecord(race: RaceDetails): Int {
        return (1 .. race.time).filter { chargeFor ->
            val moveFor = race.time - chargeFor
            chargeFor * moveFor > race.distance
        }.size
    }

    fun parseInput1(input: List<String>): List<RaceDetails> {
        val times = input[0].split(Regex("\\s+")).drop(1).map { it.toLong() }
        val distances = input[1].split(Regex("\\s+")).drop(1).map { it.toLong() }

        return times.zip(distances).map { RaceDetails(it.first, it.second) }
    }

    fun parseInput2(input: List<String>): RaceDetails {
        val time = input[0].split(Regex("\\s+")).drop(1).joinToString("").toLong()
        val distance = input[1].split(Regex("\\s+")).drop(1).joinToString("").toLong()

        return RaceDetails(time, distance)
    }
}