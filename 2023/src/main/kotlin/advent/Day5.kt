package advent

class AlmanacRange(val first: Long, val last: Long)

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
        return getMinFinalDestination(seedRanges, maps)
    }

    fun oldPart2(input: List<String>): Any {
        val seedRanges = parseSeedRanges(input)
        val maps = parseMaps(input)
        var total = seedRanges.sumOf { it.last - it.first }
        var i = 0L
        var min = Long.MAX_VALUE
        seedRanges.forEach {
            for (v in it.first .. it.last) {
                i++
                if (i % 1000000 == 0L) println((i.toDouble() / total) * 100)
                val candidate = getFinalDestination(v, maps)
                if (candidate < min) min = candidate
            }
        }
        return min
    }

    fun getMinFinalDestination(ranges: List<AlmanacRange>, maps: List<SeedMap>): Long {
        var onMap = 0
        return maps.fold(ranges) { accum, map ->
            compressRanges(accum).flatMap {
                map.getDestinationRanges(it)
            }
        }.minBy {
            it.first
        }.first
    }

    fun compressRanges(ranges: List<AlmanacRange>): List<AlmanacRange> {
        return ranges.sortedBy { it.first }.fold(listOf<AlmanacRange>()) { accum, next ->
            if (accum.size == 0) {
                listOf(next)
            } else {
                val last = accum.last()
                if (last.last > next.first) {
                    accum.dropLast(1) + AlmanacRange(last.first, next.last)
                } else {
                    accum + listOf(next)
                }
            }
        }
    }

    fun getMinFinalDestination(range: AlmanacRange, maps: List<SeedMap>): Long =
        maps.fold(listOf(range)) { accum, map ->
            accum.flatMap {
                map.getDestinationRanges(it)
            }
        }.minBy {
            it.first
        }.first


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

        fun getDestinationRanges(input: AlmanacRange): List<AlmanacRange> {
            val results = mutableListOf<AlmanacRange>()
            var current = input.first
            var currentRangeIndex = 0
            fun nextValidRange(value: Long): Int {
                var range: MapRange? = null
                while (currentRangeIndex < ranges.size && ranges[currentRangeIndex].sourceEnd < value) {
                    currentRangeIndex++
                }
                return currentRangeIndex
            }
            while (current <= input.last) {
                val nextValidRange = nextValidRange(current)
                if (nextValidRange >= ranges.size) {
                    // No more valid ranges, direct translate til end of input range
                    results += AlmanacRange(current, input.last)
                    current = input.last + 1
                } else {
                    val range = ranges[nextValidRange]
                    if (!range.contains(current)) {
                        // skip forward to range by generating direct translation range
                        results += AlmanacRange(current, range.start - 1)
                        current = range.start
                    }

                    if (range.sourceEnd > input.last) {
                        // trim output range if it extends beyond the input range
                        results += AlmanacRange(range.targetValue(current), range.targetValue(input.last))
                        current = input.last + 1
                    } else {
                        // Otherwise use the entire output of this range
                        results += range.destRange
                        current = range.sourceEnd + 1
                    }
                }
            }
            return results
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
        val sourceEnd = start + length - 1
        val destEnd = dest + length - 1
        val sourceRange: AlmanacRange by lazy { AlmanacRange(start, sourceEnd) }
        val destRange: AlmanacRange by lazy { AlmanacRange(dest, destEnd) }
        val sourceToDestOffset = dest - start

        fun contains(value: Long): Boolean = value in (start .. sourceEnd)
        operator fun compareTo(value: Long): Int = when {
            value < start -> 1
            value >= sourceEnd -> -1
            else -> 0
        }
        fun targetValue(givenValue: Long): Long = (givenValue - start) + dest


    }

    fun parseSeeds(input: List<String>): List<Long> =
        input[0].split(": ")[1].split(" ").map { it.toLong() }

    fun parseSeedRanges(input: List<String>): List<AlmanacRange> =
        parseSeeds(input)
            .chunked(2)
            .map { AlmanacRange(it[0], it[0] + it[1]) }

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