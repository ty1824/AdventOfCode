package advent

import advent.Direction.*

object Directions : List<Direction> by listOf(Right, Down, Left, Up) {
    val vectors: List<Vector2> = this.map { it.vector }
}

enum class Direction(private val oppositeOrdinal: Int, val vector: Vector2) {
    Right(2, Vector2(1, 0)),
    Down(3, Vector2(0, 1)),
    Left(0, Vector2(-1, 0)),
    Up(1, Vector2(0, -1));

    val opposite: Direction by lazy { values()[oppositeOrdinal] }
}