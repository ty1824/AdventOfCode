package advent

object Day18 : AdventDay {
    override fun part1(input: List<String>): Any {
        val cubes = parseInput(input)
        return cubes.locations.sumOf { loc ->
            directions.count { !cubes.locations.contains(loc + it) }
        }
    }

    override fun part2(input: List<String>): Any {
        val cubes = parseInput(input)
        return cubes.locations.sumOf { cubes.findExteriorSides(it) }
    }

    val directions = sequenceOf(
        Vector3(-1, 0, 0), Vector3(1, 0, 0),
        Vector3(0, -1, 0), Vector3(0, 1, 0),
        Vector3(0, 0, -1), Vector3(0, 0, 1)
    )
    class Cubes(val locations: Set<Vector3>) {
        val boundingBox: Pair<Vector3, Vector3> by lazy {
            val maxX = locations.maxOf { it.x }
            val minX = locations.minOf { it.x }
            val maxY = locations.maxOf { it.y }
            val minY = locations.minOf { it.y }
            val maxZ = locations.maxOf { it.z }
            val minZ = locations.minOf { it.z }
            Vector3(minX, minY, minZ) to Vector3(maxX, maxY, maxZ)
        }

        fun isOutsideBoundingBox(vec: Vector3): Boolean {
            val (min, max) = boundingBox
            return vec.x < min.x || vec.y < min.y || vec.z < min.z
                || vec.x > max.x || vec.y > max.y || vec.z > max.z
        }

        val hasPathOutCache: MutableMap<Vector3, Boolean> = mutableMapOf()
        fun hasPathOut(from: Vector3): Boolean {
            // Cubes don't have a path out, they are blocked space!
            if (locations.contains(from)) return false
            // If we've already searched this node, return its result
            if (hasPathOutCache.containsKey(from)) return hasPathOutCache[from]!!

            // Otherwise, find a path out if it exists.
            val alreadySearchedOrInvalid = (locations + from).toMutableSet()
            val searchCandidates = mutableSetOf<Vector3>(from)
            var frontier = listOf(from)
            var hasPathOut = false
            while (frontier.any()) {
                alreadySearchedOrInvalid += frontier
                searchCandidates += frontier
                // If we've left the area containing cubes, this is a successful search
                if (frontier.any { hasPathOutCache[it] == true || isOutsideBoundingBox(it) }) {
                    hasPathOut = true
                    break
                } else {
                    // Get next locations to visit, filtering out any that we've already visited.
                    frontier = frontier.asSequence().flatMap { vec -> directions.map { vec + it } }
                        .distinct()
                        .filter { !alreadySearchedOrInvalid.contains(it) }
                        .toList()
                }
            }

            // All visited nodes have a path out if this search was successful.
            searchCandidates.forEach { hasPathOutCache[it] = hasPathOut }
            return hasPathOut
        }

        fun findExteriorSides(from: Vector3): Int =
            directions.count { hasPathOut(from + it) }
    }

    fun parseInput(input: List<String>): Cubes = Cubes(input.map {
        val (x, y, z) = it.split(',')
        Vector3(x.toInt(), y.toInt(), z.toInt())
    }.toSet())
}