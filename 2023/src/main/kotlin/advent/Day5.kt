package advent

object Day5 : AdventDay {
    override fun part1(input: List<String>): Any {
        val seeds = parseSeeds(input)
        val maps = parseMaps(input)
        val finalDestinations = seeds.map { getFinalDestination(it, maps) }
        return finalDestinations.min()
    }

    override fun part2(input: List<String>): Any {
        val seedRanges = parseSeedRanges(input)
        val maps = parseMaps(input)
        var total = seedRanges.sumOf { it.last - it.first }
        var i = 0L
        var min = Long.MAX_VALUE
        seedRanges.forEach {
            it.forEach {
                i++
                if (i % 1000000 == 0L) println((i.toDouble() / total) * 100)
                val candidate = getFinalDestination(it, maps)
                if (candidate < min) min = candidate
            }
        }
        return min
    }


    fun getFinalDestination(seed: Long, maps: List<SeedMap>): Long {
        return maps.fold(seed) { acc, map ->
            map.getDestination(acc)//.also { print("$it, ")}
        }
    }

    class SeedMap(val name: String, givenRanges: List<MapRange>) {
        val ranges = givenRanges.sortedBy { it.start }
        fun getDestination(input: Long): Long =
            searchRanges(input)?.let {
//                println("$input maps with range (${it.start}-${it.start+it.length}) to dest ${it.dest}")
                it.targetValue(input)
            } ?: run {
//                println("$input maps with no range")
                input
            }

        fun searchRanges(value: Long): MapRange? = binarySearchHelper(value, 0, ranges.size)

        fun binarySearchHelper(value: Long, low: Int, high: Int): MapRange? {
//            println("    SEARCHING $low to $high")
            return when (val searchRange = high - low) {
                -1, 0 -> null
                1 -> if (ranges[low].contains(value)) ranges[low] else null
                2 -> when {
                    ranges[low].contains(value) -> ranges[low]
                    ranges[low + 1].contains(value) -> ranges[low + 1]
                    else -> null
                }
                else -> {
                    val peekAt = low + (searchRange / 2)
                    val checkRange = ranges[peekAt]
                    when {
                        checkRange > value -> binarySearchHelper(value, low, peekAt)
                        checkRange < value -> binarySearchHelper(value, peekAt + 1, high)
                        else -> checkRange
                    }
                }
            }
        }
    }

    data class MapRange(val start: Long, val dest: Long, val length: Long) {
        fun contains(value: Long): Boolean = value in (start until start + length)
        operator fun compareTo(value: Long): Int = when {
            value < start -> 1
            value >= start + length -> -1
            else -> 0
        }
        fun targetValue(givenValue: Long): Long = (givenValue - start) + dest
    }

    fun parseSeeds(input: List<String>): List<Long> =
        input[0].split(": ")[1].split(" ").map { it.toLong() }

    fun parseSeedRanges(input: List<String>): List<LongRange> =
        parseSeeds(input)
            .chunked(2)
            .map { it[0].rangeTo(it[0] + it[1]) }

    fun parseMaps(input: List<String>): List<SeedMap> {
        val mapLines = mutableListOf<MutableList<String>>()
        input.drop(1).forEach { line ->
            if (line.isBlank()) {

            } else if (line.first().isLetter()) {
                mapLines += mutableListOf<String>(line)
            } else if (line.isNotBlank()) {
                mapLines.last() += line
            }
        }
        val maps = mapLines.map { linesForMap ->
            val name = linesForMap.first()
            val entries = linesForMap.drop(1).map { entryLine ->
                val (dest, start, length) = entryLine.split(" ")
                MapRange(start.toLong(), dest.toLong(), length.toLong())
            }
            SeedMap(name, entries)
        }
        return maps
    }
}