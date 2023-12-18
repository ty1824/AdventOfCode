package advent

import java.lang.Math.abs

object Day18 : AdventDay {
    override fun part1(input: List<String>): Any {
        val instructions = parseInput(input)
        val digSite = digAll(instructions)
        val digVertices = digToPoints(instructions)
        val crossProductArea = crossProductArea(digVertices)
        val digInterior = digSite.digInterior()
        return digInterior.digMap.size to (crossProductArea + nonOverlappingSegmentLength(digVertices))
    }

    override fun part2(input: List<String>): Any {
        val instructions = parseInput(input).map { it.realInstruction() }
        val digVertices = digToPoints(instructions)
        return crossProductArea(digVertices) + nonOverlappingSegmentLength(digVertices)
    }

    fun nonOverlappingSegmentLength(vertices: List<Vector2>): Long {
        var last: Vector2 = vertices.last()
        return vertices.fold(0L) { acc, vec ->
            val result = (vec - last).abs().linearMagnitude()
            last = vec
            acc + result.toLong()
        } / 2 + 1
    }

    fun crossProductArea(vertices: List<Vector2>): Long {
        var last: Vector2 = vertices.last()
        return vertices.fold(0L) { acc, vec ->
            val result = last.cross(vec)
            last = vec
            acc + result
        } / 2
    }

    class DigSite(val digMap: Set<Vector2>) {
        val left = digMap.minOf { it.x }
        val right = digMap.maxOf { it.x }
        val top = digMap.minOf { it.y }
        val bottom = digMap.maxOf { it.y }

        val dimensions = Vector2(right, bottom) - Vector2(left, top)

        fun offSite(vec: Vector2) = vec.x < left || vec.x > right || vec.y < top || vec.y > bottom

        fun onSite(vec: Vector2) = !offSite(vec)

        val searchVectors = (Vector2(-1, -1)..Vector2(1, 1)) - Vector2.zero
        fun searchAll(vec: Vector2): Boolean =
            searchVectors.all {
                search(vec, it)
            }

        fun search(vec: Vector2, searchDir: Vector2): Boolean {
            var search = vec + searchDir
            var hits = 0
            while (!offSite(search)) {
                if (digMap.contains(search)) hits++
                search += searchDir
            }
            return hits % 2 == 1
        }

        val one = Vector2(1, 1)
        fun digInterior(): DigSite {
            val visited = mutableSetOf<Vector2>()
            visited += digMap
            val inside: MutableSet<Vector2> = mutableSetOf()
            inside += digMap

            fun allReachableFrom(vec: Vector2): Pair<Set<Vector2>, Boolean> {
                val reached = mutableSetOf<Vector2>()
                var offSite = false
                reached += vec
                breadthFirstSearch(listOf(vec)) {
                    ((it - one)..(it + one)).toList().filter { search ->
                        if (!offSite && offSite(search)) {
                            offSite = true
                        }
                        onSite(search)
                    }.filter { search ->
                        !digMap.contains(search) && !reached.contains(search)
                    }.also { search -> reached += search }
                }
                return reached to offSite
            }
            val topLeft = Vector2(left, top)
            val bottomRight = Vector2(right, bottom)
            val sequence = topLeft..bottomRight
            for (vec in sequence) {
                if (!visited.contains(vec)) {
                    val (touched, outside) = allReachableFrom(vec)
                    visited += touched
                    if (!outside) {
                        inside += touched
                    }
                }
            }
            return DigSite(inside)
        }

        override fun toString(): String {
            var output = mutableListOf<String>()
            for (y in top..bottom) {
                output += ""
                for (x in left..right) {
                    output[y - top] = output[y - top] + if (digMap.contains(Vector2(x, y))) { '#' } else { '.' }
                }
            }
            return output.joinToString("\n")
        }

    }

    fun digAll(instructions: List<DigInstruction>): DigSite {
        val digLocations = mutableSetOf<Vector2>()
        var current = Vector2.zero
        instructions.forEach { (direction, length) ->
            val dest = current + (direction.vector * length)
            (current..dest).forEach {
                digLocations += it
            }
            current = dest
        }
        return DigSite(digLocations)
    }

    fun digToPoints(instructions: List<DigInstruction>): List<Vector2> {
        var current = Vector2.zero
        return instructions.map { (direction, length) ->
            current += (direction.vector * length)
            current
        }
    }

    data class DigInstruction(val direction: Direction, val length: Int, val code: String) {
        fun realInstruction(): DigInstruction =
            DigInstruction(
                when (val dirChar = code.substring(6, 7)) {
                    "0" -> Direction.Right
                    "1" -> Direction.Down
                    "2" -> Direction.Left
                    "3" -> Direction.Up
                    else -> throw RuntimeException("Bad dirchar $dirChar")
                },
                code.substring(1, 6).toInt(16),
                ""
            )
    }

    fun parseInput(input: List<String>): List<DigInstruction> = input.map { line ->
        val (dirChar, length, color) = line.split(" ")
        val direction = when (dirChar) {
            "R" -> Direction.Right
            "L" -> Direction.Left
            "U" -> Direction.Up
            "D" -> Direction.Down
            else -> throw RuntimeException("Bad dirchar $dirChar")
        }
        DigInstruction(direction, length.toInt(), color.replace("(", "").replace(")", ""))
    }
}