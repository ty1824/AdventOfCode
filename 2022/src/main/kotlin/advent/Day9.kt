package advent

object Day9 : AdventDay {
    override fun part1(input: List<String>): Any {
        val states = mutableListOf(GridState())
        input.forEach {
            val (direction, movesString) = it.split(' ')
            (1..movesString.toInt()).forEach {
                states += states.last().moveHead(directionToVector(direction))
            }
        }
        return states.map { it.tail }.distinct().count()
    }

    override fun part2(input: List<String>): Any {
        val states = mutableListOf(GridStateN(List(10) { Vector2() }))
        input.forEach {
            val (direction, movesString) = it.split(' ')
            (1..movesString.toInt()).forEach {
                states += states.last().moveHead(directionToVector(direction))
            }
        }
        return states.map { it.knots.last() }.distinct().count()
    }

    private fun directionToVector(direction: String): Vector2 = when (direction) {
        "U" -> Vector2(0, -1)
        "D" -> Vector2(0, 1)
        "L" -> Vector2(-1, 0)
        "R" -> Vector2(1, 0)
        else -> throw RuntimeException("Invalid direction $direction")
    }

    data class GridState(val head: Vector2 = Vector2(0, 0), val tail: Vector2 = Vector2(0, 0)) {
        fun moveHead(by: Vector2): GridState {
            val newHead = this.head + by
            val headTailDiff = newHead - this.tail
            val absDiff = headTailDiff.abs()
            val newTail = if (absDiff.x > 1 || absDiff.y > 1) {
                val diffDirection = headTailDiff.sign()
                val tailMove = if (absDiff.x > absDiff.y) {
                    absDiff.minusX(1)
                } else if (absDiff.y > absDiff.x) {
                    absDiff.minusY(1)
                } else {
                    // If we hit this, we're moving by too much.
                    throw RuntimeException("Failed to apply change. State: $this, change: $by, newHead $newHead, diff $headTailDiff ")
                }
                this.tail + (tailMove * diffDirection)
            } else this.tail
            return GridState(newHead, newTail)
        }
    }

    data class GridStateN(val knots: List<Vector2>) {
        fun moveHead(by: Vector2): GridStateN {
            return GridStateN(move(this.knots, by, 0))
        }

        private fun move(knots: List<Vector2>, by: Vector2, index: Int): List<Vector2> {
            val newHead = knots[index] + by
            if (index == knots.size - 1) return listOf(newHead)
            val headTailDiff = newHead - knots[index + 1]
            val absDiff = headTailDiff.abs()
            absDiff.linearMagnitude()
            val tailMove = if (absDiff.x > 1 || absDiff.y > 1) {
                val diffDirection = headTailDiff.sign()
                val tailMove = if (absDiff.x > absDiff.y) {
                    absDiff.minusX(1)
                } else if (absDiff.y > absDiff.x) {
                    absDiff.minusY(1)
                } else if (absDiff.x == 2) {
                    // New case in the world of N knots - movements can be N-dimensional.
                    absDiff - 1
                } else {
                    // If we hit this, we're moving by too much.
                    throw RuntimeException("Failed to apply change. State: $this, change: $by, newHead $newHead, diff $headTailDiff ")
                }
                (tailMove * diffDirection)
            } else Vector2()
            return listOf(newHead) + move(knots, tailMove, index + 1)
        }
    }
}