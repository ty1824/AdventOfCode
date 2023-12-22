package advent

import advent.Util.rangeOf
import kotlin.math.sign

data class Vector3(val x: Int = 0, val y: Int = 0, val z: Int = 0)

operator fun Vector3.plus(other: Vector3): Vector3 = Vector3(this.x + other.x, this.y + other.y, this.z + other.z)
operator fun Vector3.minus(other: Vector3): Vector3 = Vector3(this.x - other.x, this.y - other.y, this.z - other.z)
operator fun Vector3.times(other: Vector3): Vector3 = Vector3(this.x * other.x, this.y * other.y, this.z * other.z)
operator fun Vector3.div(other: Vector3): Vector3 = Vector3(this.x / other.x, this.y / other.y, this.z / other.z)
fun Vector3.linearMagnitude(): Int = this.x + this.y + this.z
fun Vector3.abs(): Vector3 = Vector3(kotlin.math.abs(this.x), kotlin.math.abs(this.y), kotlin.math.abs(this.z))
fun Vector3.sign(): Vector3 = Vector3(this.x.sign, this.y.sign, this.z.sign)
fun Vector3.plusX(x: Int): Vector3 = Vector3(this.x + x, this.y, this.z)
fun Vector3.plusY(y: Int): Vector3 = Vector3(this.x, this.y + y, this.z)
fun Vector3.plusZ(z: Int): Vector3 = Vector3(this.x, this.y, this.z + z)
fun Vector3.minusX(x: Int): Vector3 = Vector3(this.x - x, this.y, this.z)
fun Vector3.minusY(y: Int): Vector3 = Vector3(this.x, this.y - y, this.z)
fun Vector3.minusZ(z: Int): Vector3 = Vector3(this.x, this.y, this.z - z)
fun Vector3.manhattanDistance(): Int = this.abs().linearMagnitude()
operator fun Vector3.plus(value: Int): Vector3 = Vector3(this.x + value, this.y + value, this.z + value)
operator fun Vector3.minus(value: Int): Vector3 = Vector3(this.x - value, this.y - value, this.z - value)
operator fun Vector3.times(value: Int): Vector3 = Vector3(this.x * value, this.y * value, this.z * value)
operator fun Vector3.div(value: Int): Vector3 = Vector3(this.x / value, this.y / value, this.z / value)

operator fun Vector3.rangeTo(other: Vector3): Sequence<Vector3> = sequence {
    for (x in rangeOf(x, other.x)) {
        for (y in rangeOf(y, other.y)) {
            for (z in rangeOf(z, other.z)) {
                yield(Vector3(x, y, z))
            }
        }
    }
}