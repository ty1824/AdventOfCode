package advent

interface Grid<T> {
    val width: Int

    val height: Int

    operator fun set(index: Int, value: T)
    operator fun set(location: Vector2, value: T) = set(locationToIndex(location), value)
    operator fun get(index: Int): T
    operator fun get(location: Vector2): T = get(locationToIndex(location))

    val size: Int
        get() = width * height

    fun isOnGrid(vector: Vector2): Boolean = !isOffGrid(vector)

    fun isOffGrid(vector: Vector2): Boolean =
        vector.x < 0 || vector.x > width-1 || vector.y < 0 || vector.y > height-1

    fun locationToIndex(vector: Vector2): Int = width * vector.y + vector.x

    fun indexToLocation(index: Int): Vector2 = Vector2((index % width), (index / width))

    fun getRows(): Sequence<Sequence<T>> = sequence {
        var el = 0
        yield(sequence {
            for (i in (0 until width)) {
                yield(get(el++))
            }
        })

    }

    fun anyAdjacent(location: Vector2, predicate: (T) -> Boolean): Boolean {
        val ret = ((location - Vector2(1, 1))..(location + Vector2(1, 1))).flatten().any {
            isOnGrid(it) && predicate(get(it))
        }
        return ret
    }

    fun getAdjacent(location: Vector2): Sequence<Vector2> =
        ((location - Vector2(1, 1))..(location + Vector2(1, 1))).flatten().filter {
            isOnGrid(it)
        }
}

class IntGrid(
    val elements: IntArray,
    override val width: Int,
    override val height: Int
) : Grid<Int> {
    override fun set(index: Int, value: Int) { elements[index] = value }
    override fun get(index: Int): Int = elements[index]

    /**
     * Type-specific method returning primitive value.
     */
    fun getInt(location: Vector2): Int = elements[locationToIndex(location)]
    fun getInt(index: Int): Int = elements[index]
}

class CharGrid(
    val elements: CharArray,
    override val width: Int,
    override val height: Int
) : Grid<Char> {
    override fun set(index: Int, value: Char) { elements[index] = value }
    override fun get(index: Int): Char = elements[index]

    /**
     * Type-specific method returning primitive value.
     */
    fun getChar(location: Vector2): Char = elements[locationToIndex(location)]
    fun getChar(index: Int): Char = elements[index]
}

class GenericGrid<T> (
    val elements: Array<T>,
    override val width: Int,
    override val height: Int
) : Grid<T> {
    override fun set(index: Int, value: T) { elements[index] = value }
    override fun get(index: Int): T = elements[index]
}