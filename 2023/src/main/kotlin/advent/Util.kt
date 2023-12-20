package advent

object Util {
    fun gcd(a: Long, b: Long): Long {
        return if (a == 0L) b else gcd(b % a, a)
    }

    // method to return LCM of two numbers
    fun lcm(a: Long, b: Long): Long {
        return a / gcd(a, b) * b
    }
}