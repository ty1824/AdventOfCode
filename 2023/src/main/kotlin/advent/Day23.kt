package advent

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

object Day23 : AdventDay {
    override fun part1(input: List<String>): Any {
        val grid = parseInput(input)
        return longestPath(
            grid,
            grid.indexToLocation(grid.elements.indexOf('.')),
            grid.indexToLocation(grid.elements.lastIndexOf('.'))
        ).size - 1
    }

    override fun part2(input: List<String>): Any {
        val grid = parseInput(input)
        return longestPathWithHallways(
            grid,
            grid.indexToLocation(grid.elements.indexOf('.')),
            grid.indexToLocation(grid.elements.lastIndexOf('.')),
            true
        ).length
    }

    fun gridWithCovered(grid: CharGrid, covered: Iterable<Vector2>): CharGrid {
        val newGrid = CharGrid(grid.elements.copyOf(), grid.width, grid.height)
        covered.forEach {
            newGrid[it] = 'O'
        }
        return newGrid
    }

    class Hallway(path: List<Vector2>) {
        val forwardExit = path.last()
        val reverseExit = path.first()
        val innerPath = path.drop(1).dropLast(1)
        val forwardStart = innerPath.first()
        val reverseStart = innerPath.last()
        val length = innerPath.size

        fun path(entryLoc: Vector2): List<Vector2> =
            if (forwardStart == entryLoc) {
                listOf(reverseStart, forwardExit)
            } else {
                listOf(forwardStart, reverseExit)
            }

        override fun toString(): String = length.toString()
    }

    fun calculateAllHallways(grid: CharGrid): Map<Vector2, Hallway> {
        val possibleIndices = grid.elements.indices
            .filter { grid[it] != '#'}
            .map { grid.indexToLocation(it) }
            .filter { moveFrom(grid, it, true).count() == 2 }
            .toMutableSet()
        val allHallways = mutableListOf<Hallway>()
        while (possibleIndices.size > 0) {
            val first = possibleIndices.first()
            possibleIndices.remove(first)
            val hallway = findHallwayFrom(grid, first)
            if (hallway != null) {
                allHallways += hallway
                possibleIndices.removeAll(hallway.innerPath.toSet())
            }
        }
        allHallways += findEdgeHallways(grid)
        return allHallways.associateBy { it.forwardStart } + allHallways.associateBy { it.reverseStart }
    }

    fun findEdgeHallways(grid: CharGrid): List<Hallway> {
        val startLoc = grid.indexToLocation(grid.elements.indexOf('.'))
        val start = walkHallwayFrom(grid, persistentListOf(startLoc, moveFrom(grid, startLoc, true).first()))
        val endLoc = grid.indexToLocation(grid.elements.lastIndexOf('.'))
        val end = walkHallwayFrom(grid, persistentListOf(endLoc, moveFrom(grid, endLoc, true).first()))
        return listOfNotNull(start, end).map {
            Hallway(it)
        }
    }

    tailrec fun walkEdgeHallwayFrom(grid: CharGrid, path: PersistentList<Vector2>): List<Vector2>? {
        val possibleDestinations = moveFrom(grid, path.last(), true).toList()
        return if (possibleDestinations.size == 2) {
            val next = possibleDestinations.first { path.lastIndexOf(it) < 0 }
            walkHallwayFrom(grid, path.add(next))
        } else if (possibleDestinations.size > 2) {
            path
        } else {
            null
        }
    }

    fun findHallwayFrom(grid: CharGrid, loc: Vector2): Hallway? {
        val possibleDestinations = moveFrom(grid, loc, true).toList()
        return if (possibleDestinations.size == 2) {
            val (left, right) = possibleDestinations
            val leftPath = walkHallwayFrom(grid, persistentListOf(loc, left)) ?: return null
            val rightPath = walkHallwayFrom(grid, persistentListOf(loc, right)) ?: return null
            Hallway(leftPath.drop(1).reversed() + rightPath)
        } else {
            null
        }
    }

    tailrec fun walkHallwayFrom(grid: CharGrid, path: PersistentList<Vector2>): List<Vector2>? {
        val possibleDestinations = moveFrom(grid, path.last(), true).toList()
        return if (possibleDestinations.size == 2) {
            val next = possibleDestinations.first { path.lastIndexOf(it) < 0 }
            walkHallwayFrom(grid, path.add(next))
        } else if (possibleDestinations.size > 2) {
            path
        } else {
            null
        }
    }

    fun longestPath(grid: CharGrid, startLocation: Vector2, endLocation: Vector2, ignoreSlopes: Boolean = false): List<Vector2> {
        var maxPath: List<Vector2> = listOf()
        depthFirstSearch(listOf(persistentListOf(startLocation))) { currentPath ->
            val currentLoc = currentPath.last()
            if (currentLoc == endLocation) {
                if (currentPath.size > maxPath.size) {
                    maxPath = currentPath
                }
                listOf()
            } else {
                moveFrom(grid, currentLoc, ignoreSlopes)
                    .filter { currentPath.lastIndexOf(it) < 0 }
                    .sortedBy { it.manhattanDistance() }
                    .map { currentPath.add(it) }
                    .toList()

            }
        }
        return maxPath
    }

    interface PathElement
    data class Tile(val loc: Vector2)
    data class HallwayTraversal(val hallway: Hallway, val start: Vector2, val end: Vector2)


    data class PathState(val path: PersistentList<Vector2>, val length: Int)
    fun longestPathWithHallways(grid: CharGrid, startLocation: Vector2, endLocation: Vector2, ignoreSlopes: Boolean = false): PathState  {
        val hallways = calculateAllHallways(grid)
        var maxPath: PathState = PathState(persistentListOf(), 0)
        depthFirstSearch(listOf(PathState(persistentListOf(startLocation), 0))) { state ->
            val (currentPath, length) = state
            val currentLoc = currentPath.last()
            val hallway = hallways[currentLoc]
            if (currentLoc == endLocation) {
                if (length > maxPath.length) {
                    maxPath = state
                }
                listOf()
            } else if (hallway != null) {
                val newElements = hallway.path(currentLoc)
                if (newElements.none { currentPath.lastIndexOf(it) >= 0 }) {
                    val newPath = currentPath.addAll(newElements)
                    listOf(PathState(newPath, length + hallway.length))
                } else {
                    listOf()
                }
            } else {
                moveFrom(grid, currentLoc, ignoreSlopes)
                    .filter { !currentPath.contains(it) }
                    .sortedBy { it.manhattanDistance() }
                    .map { PathState(currentPath.add(it), length + 1) }
                    .toList()
            }
        }
        return maxPath
    }

    fun moveFrom(grid: CharGrid, from: Vector2, ignoreSlopes: Boolean): Sequence<Vector2> {
        val slopeDirection = grid[from].slopeDirection()
        return if (!ignoreSlopes && slopeDirection != null) {
            sequenceOf(from + slopeDirection.vector)
        } else {
            Directions.vectors.asSequence().map {
                from + it
            }
        }.filter { candidate ->
            grid.isOnGrid(candidate) && grid[candidate] != '#'
        }
    }

    fun Char.slopeDirection(): Direction? = when (this) {
        '^' -> Direction.Up
        '>' -> Direction.Right
        'v' -> Direction.Down
        '<' -> Direction.Left
        else -> null
    }

    fun parseInput(input: List<String>): CharGrid =
        CharGrid(input.flatMap { line ->
            line.toList()
        }.toCharArray(), input[0].length, input.size)
}