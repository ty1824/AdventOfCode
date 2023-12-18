package advent

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

data class Vector2(val x: Int = 0, val y: Int = 0) {
    companion object {
        val zero = Vector2()
        operator fun invoke(xRange: IntRange, yRange: IntRange): Sequence<Vector2> = sequence {
            xRange.forEach { x -> yRange.forEach { y -> yield(Vector2(x, y)) } }
        }
    }

    override fun toString(): String = "($x, $y)"
}

operator fun Vector2.plus(other: Vector2): Vector2 = Vector2(this.x + other.x, this.y + other.y)
operator fun Vector2.minus(other: Vector2): Vector2 = Vector2(this.x - other.x, this.y - other.y)
operator fun Vector2.times(other: Vector2): Vector2 = Vector2(this.x * other.x, this.y * other.y)
operator fun Vector2.div(other: Vector2): Vector2 = Vector2(this.x / other.x, this.y / other.y)
operator fun Vector2.rangeTo(other: Vector2): Sequence<Vector2> =
    when {
        other.x == this.x -> yRange(other).asSequence().map { Vector2(x, it) }
        other.y == this.y -> xRange(other).asSequence().map { Vector2(it, y)}
        else -> sequence {
            for (seqY in (min(y, other.y)..max(y, other.y))) {
                for (seqX in (min(x, other.x)..max(x, other.x))) {
                    yield(Vector2(seqX, seqY))
                }
            }
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
fun Vector2.cross(other: Vector2): Long = this.x.toLong() * other.y.toLong() - other.x.toLong() * this.y.toLong()
operator fun Vector2.plus(value: Int): Vector2 = Vector2(this.x + value, this.y + value)
operator fun Vector2.minus(value: Int): Vector2 = Vector2(this.x - value, this.y - value)
operator fun Vector2.times(value: Int): Vector2 = Vector2(this.x * value, this.y * value)
operator fun Vector2.div(value: Int): Vector2 = Vector2(this.x / value, this.y / value)

data class Path(val points: List<Vector2>)