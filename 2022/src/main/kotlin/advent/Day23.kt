package advent

object Day23 : AdventDay {
    override fun part1(input: List<String>): Any {
        val elves = parseInput(input)
        var map = ElfMap(elves.toSet(), 0)
        repeat(10) {
            map = map.stepRound().first
        }
        debugln(map.mapString())
        val dimensions = map.dimensions() + 1
        return dimensions.x * dimensions.y - map.elfLocations.size
    }

    override fun part2(input: List<String>): Any {
        val elves = parseInput(input)
        var map = ElfMap(elves.toSet(), 0)
        var iteration = 0
        do {
            val (newMap, anyMoved) = map.stepRound()
            iteration++
            map = newMap
        } while (anyMoved)
        return iteration
    }

    private val allDirections = (-1..1).flatMap { x -> (-1..1).map { y -> Vector2(x, y)}} - Vector2(0, 0)
    private enum class MoveDirection(val movement: Vector2, val check: List<Vector2>) {
        North(Vector2(0, 1), (-1..1).map { Vector2(it, 1) }),
        South(Vector2(0, -1), (-1..1).map { Vector2(it, -1) }),
        West(Vector2(-1, 0), (-1..1).map { Vector2(-1, it) }),
        East(Vector2(1, 0), (-1..1).map { Vector2(1, it) })
    }

    private fun movementsFrom(index: Int): List<MoveDirection> =
        (index until (index + 4)).map { MoveDirection.values()[it % 4] }

    private data class ElfMap(val elfLocations: Set<Vector2>, val moveStart: Int) {
        fun stepRound(): Pair<ElfMap, Boolean> {
            val (newLocations, anyMoved) = move(proposeLocations())
            return ElfMap(newLocations, (moveStart + 1) % 4) to anyMoved
        }

        fun proposeLocations(): Map<Vector2, List<Vector2>> {
            val turnMoves = movementsFrom(moveStart)
            return elfLocations.mapNotNull { location ->
                if (allDirections.none { elfLocations.contains(location + it) }) {
                    null
                } else {
                    val move = turnMoves.firstOrNull { moves ->
                        moves.check.none { move -> elfLocations.contains(location + move) }
                    }
                    move?.let {
                        location + it.movement to location
                    }
                }
            }.groupBy({ it.first }) { it.second }.toMap()
        }

        fun move(proposals: Map<Vector2, List<Vector2>>): Pair<Set<Vector2>, Boolean> {
            val movements: Map<Vector2, Vector2> = proposals.mapNotNull { (key, value) ->
                if (value.size == 1) value.first() to key else null
            }.toMap()
            return elfLocations.map {
                movements[it] ?: it
            }.toSet() to movements.isNotEmpty()
        }

        fun bottomLeft(): Vector2 = Vector2(elfLocations.minOf { it.x }, elfLocations.minOf { it.y })
        fun topRight(): Vector2 = Vector2(elfLocations.maxOf { it.x }, elfLocations.maxOf { it.y })

        fun dimensions(): Vector2 = topRight() - bottomLeft()

        fun mapString(): String {
            val bottomLeft = bottomLeft()
            val topRight = topRight()
            return (topRight.y downTo bottomLeft.y).joinToString("\n") { y ->
                (bottomLeft.x..topRight.x).joinToString("") { x -> if (elfLocations.contains(Vector2(x, y))) "#" else "." }
            }
        }
    }

    private fun parseInput(input: List<String>): List<Vector2> =
        input.flatMapIndexed { inverseY, row ->
            val y = input.lastIndex - inverseY
            row.mapIndexedNotNull() { x, char ->
                when (char) {
                    '#' -> Vector2(x, y)
                    else -> null
                }
            }
        }
}