package advent

object Day18 : AdventDay {
    override fun part1(input: List<String>): Any {
        val instructions = parseInput(input)
        val digVertices = digToPoints(instructions)
        val crossProductArea = crossProductArea(digVertices)
        return crossProductArea + nonOverlappingSegmentLength(digVertices)
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