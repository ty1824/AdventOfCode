package advent

import kotlin.math.abs
import kotlin.math.sign

val UP = 0
val DOWN = 1
val LEFT = 2
val RIGHT = 3
val DIRECTIONS = intArrayOf(UP, DOWN, LEFT, RIGHT)
val DIRECTION_VECTORS = listOf(Vector2(0, -1), Vector2(0, 1), Vector2(-1, 0), Vector2(1, 0))

data class Vector2(val x: Int = 0, val y: Int = 0)

operator fun Vector2.plus(other: Vector2): Vector2 = Vector2(this.x + other.x, this.y + other.y)
operator fun Vector2.minus(other: Vector2): Vector2 = Vector2(this.x - other.x, this.y - other.y)
operator fun Vector2.times(other: Vector2): Vector2 = Vector2(this.x * other.x, this.y * other.y)
operator fun Vector2.div(other: Vector2): Vector2 = Vector2(this.x / other.x, this.y / other.y)
fun Vector2.linearMagnitude(): Int = this.x + this.y
fun Vector2.abs(): Vector2 = Vector2(abs(this.x), abs(this.y))
fun Vector2.sign(): Vector2 = Vector2(this.x.sign, this.y.sign)
fun Vector2.minusX(x: Int): Vector2 = Vector2(this.x - x, this.y)
fun Vector2.minusY(y: Int): Vector2 = Vector2(this.x, this.y - y)
operator fun Vector2.plus(value: Int): Vector2 = Vector2(this.x + value, this.y + value)
operator fun Vector2.minus(value: Int): Vector2 = Vector2(this.x - value, this.y - value)
operator fun Vector2.times(value: Int): Vector2 = Vector2(this.x * value, this.y * value)
operator fun Vector2.div(value: Int): Vector2 = Vector2(this.x / value, this.y / value)

data class Path(val points: List<Vector2>)