package advent

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

data class Vector2(val x: Int = 0, val y: Int = 0) {
    companion object {
        val zero = Vector2()
        operator fun invoke(xRange: IntRange, yRange: IntRange): List<Vector2> =
            xRange.flatMap { x -> yRange.map { y -> Vector2(x, y) } }
    }

    override fun toString(): String = "($x, $y)"
}

operator fun Vector2.plus(other: Vector2): Vector2 = Vector2(this.x + other.x, this.y + other.y)
operator fun Vector2.minus(other: Vector2): Vector2 = Vector2(this.x - other.x, this.y - other.y)
operator fun Vector2.times(other: Vector2): Vector2 = Vector2(this.x * other.x, this.y * other.y)
operator fun Vector2.div(other: Vector2): Vector2 = Vector2(this.x / other.x, this.y / other.y)
operator fun Vector2.rangeTo(other: Vector2): Sequence<Sequence<Vector2>> =
    (min(this.y, other.y)..max(this.y, other.y)).asSequence().map { y ->
        (min(this.x, other.x)..max(this.x, other.x)).asSequence().map { x ->
            Vector2(x, y)
        }
    }

fun Vector2.xRange(other: Vector2): IntRange = min(x, other.x) .. max(x, other.x)
fun Vector2.yRange(other: Vector2): IntRange = min(y, other.y) .. max(y, other.y)

fun Vector2.linearMagnitude(): Int = this.x + this.y
fun Vector2.abs(): Vector2 = Vector2(abs(this.x), abs(this.y))
fun Vector2.sign(): Vector2 = Vector2(this.x.sign, this.y.sign)
fun Vector2.minusX(x: Int): Vector2 = Vector2(this.x - x, this.y)
fun Vector2.minusY(y: Int): Vector2 = Vector2(this.x, this.y - y)
fun Vector2.manhattanDistance(): Int = this.abs().linearMagnitude()
operator fun Vector2.plus(value: Int): Vector2 = Vector2(this.x + value, this.y + value)
operator fun Vector2.minus(value: Int): Vector2 = Vector2(this.x - value, this.y - value)
operator fun Vector2.times(value: Int): Vector2 = Vector2(this.x * value, this.y * value)
operator fun Vector2.div(value: Int): Vector2 = Vector2(this.x / value, this.y / value)

data class Path(val points: List<Vector2>)