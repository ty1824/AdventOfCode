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

    val neighborVectors =
        listOf(
                    Vector2(0, -1),
             Vector2(-1, 0), Vector2(1, 0),
                    Vector2(0, 1),
        )

    class HeightMap(private val tiles: CharArray, private val width: Int, private val height: Int) {
        val startLocation = indexToLocation(tiles.indexOf('S'))
        val endLocation = indexToLocation(tiles.indexOf('E'))

        fun heightAt(location: Vector2): Char =
            when (location) {
                startLocation -> 'a'
                endLocation -> 'z'
                else -> tiles[locationToIndex(location)]
            }
        
        fun getTraversableLocations(location: Vector2): Iterable<Vector2> {
            val from = heightAt(location)
            return neighborVectors.map(location::plus)
                .filter {
                    isOnMap(it) && from.canTraverseToHeight(heightAt(it))
                }
        }

        fun getLocationsTraversableTo(location: Vector2): Iterable<Vector2> {
            val from = heightAt(location)
            return neighborVectors.map(location::plus)
                .filter {
                    isOnMap(it) && heightAt(it).canTraverseToHeight(from)
                }
        }

        private fun Char.canTraverseToHeight(other: Char): Boolean =
            ((other - this) <= 1) || other == 'E'

        private fun isOnMap(vector: Vector2): Boolean = !isOffMap(vector)
        private fun isOffMap(vector: Vector2): Boolean =
            vector.x < 0 || vector.x > width-1 || vector.y < 0 || vector.y > height-1
        private fun locationToIndex(vector: Vector2): Int = width * vector.y + vector.x
        private fun indexToLocation(index: Int): Vector2 = Vector2((index % width), (index / width))

    }

    private fun parseMap(input: List<String>): HeightMap {
        val width = input.first().length
        val height = input.size
        val tiles = CharArray(width * height)
        input.forEachIndexed { index, line -> line.toCharArray(tiles, width * index)}
        return HeightMap(tiles, width, height)
    }
}