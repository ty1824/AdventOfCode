package advent

object Day12 : AdventDay {
    override fun part1(input: List<String>): Any {
        val heightMap = parseMap(input)
        val startLocation = heightMap.startLocation
        val endLocation = heightMap.endLocation

        val visitedLocations: MutableSet<Vector2> = mutableSetOf(startLocation)
        var frontier: Set<Vector2> = setOf(startLocation)
        var step = 0
        while (!visitedLocations.contains(endLocation) && step < 1000) {
            val newLocations = frontier.flatMap { heightMap.getTraversableLocations(it) }
                .filter { !visitedLocations.contains(it) }
                .toSet()
            visitedLocations += newLocations
            frontier = newLocations
            step++
        }
        return step
    }

    override fun part2(input: List<String>): Any {
        val heightMap = parseMap(input)
        val startLocation = heightMap.endLocation

        val visitedLocations: MutableSet<Vector2> = mutableSetOf(startLocation)
        var frontier: Set<Vector2> = setOf(startLocation)
        var step = 0
        do {
            val newLocations = frontier.flatMap { heightMap.getLocationsTraversableTo(it) }
                .filter { !visitedLocations.contains(it)}
                .toSet()
            visitedLocations += newLocations
            frontier = newLocations
            step++
        } while (!frontier.any { heightMap.heightAt(it) == 'a' })
        return step
    }

    class HeightMap(private val grid: CharGrid) {
        val startLocation = grid.indexToLocation(grid.elements.indexOf('S'))
        val endLocation = grid.indexToLocation(grid.elements.indexOf('E'))

        fun heightAt(location: Vector2): Char =
            when (location) {
                startLocation -> 'a'
                endLocation -> 'z'
                else -> grid.getChar(location)
            }

        fun getTraversableLocations(location: Vector2): Iterable<Vector2> {
            val from = heightAt(location)
            return DIRECTION_VECTORS.map(location::plus)
                .filter {
                    grid.isOnGrid(it) && from.canTraverseToHeight(heightAt(it))
                }
        }

        fun getLocationsTraversableTo(location: Vector2): Iterable<Vector2> {
            val from = heightAt(location)
            return DIRECTION_VECTORS.map(location::plus)
                .filter {
                    grid.isOnGrid(it) && heightAt(it).canTraverseToHeight(from)
                }
        }

        private fun Char.canTraverseToHeight(other: Char): Boolean =
            ((other - this) <= 1) || other == 'E'
    }

    private fun parseMap(input: List<String>): HeightMap {
        val width = input.first().length
        val height = input.size
        val tiles = CharArray(width * height)
        input.forEachIndexed { index, line -> line.toCharArray(tiles, width * index)}
        return HeightMap(CharGrid(tiles, width, height))
    }
}