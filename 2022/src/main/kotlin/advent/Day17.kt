package advent

object Day17 : AdventDay {
    override fun part1(input: List<String>): Any {
        val jetPattern = input[0]
        val tower = RockTower(jetPattern)
        repeat(2022) { tower.dropShape() }
        return tower.height
    }

    private const val iterations: Long = 1000000000000L
    override fun part2(input: List<String>): Any {
        val jetPattern = input[0]
        val tower = RockTower(jetPattern)
        val (length, height) = tower.findPattern()
        val remainingIterations = iterations - tower.drops.size
        val patternItr = remainingIterations / length
        val patternRemainder = (remainingIterations % length).toInt()
        // Finish the last few iterations not covered by the pattern
        repeat(patternRemainder) { tower.dropShape() }
        return tower.height + (patternItr * height)
    }

    /**
     * A shape like
     * .#.
     * ###
     *
     * Will become
     * listOf((0, 0), (1, 0), (2, 0), (1, 1))
     */
    private class Shape(template: Array<String>) {
        // Starting from the bottom-left of the shape
        val occupiedTiles: List<Vector2> = template.flatMapIndexed { y, row ->
            row.mapIndexedNotNull { x, char ->
                if (char == '#') Vector2(x, template.lastIndex - y) else null
            }
        }

        fun at(location: Vector2): List<Vector2> = occupiedTiles.map { location + it }

        override fun toString(): String {
            return occupiedTiles.toString()
        }
    }

    private val LEFT = Vector2(-1, 0)
    private val RIGHT = Vector2(1, 0)
    private val DOWN = Vector2(0, -1)
    private class RockTower(val pattern: String) {
        var filledLocations: MutableList<BooleanArray> = mutableListOf(BooleanArray(7) { true })
        val drops: MutableList<Pair<Pair<Int, Int>, Int>> = mutableListOf()
        val height: Int
            get() = filledLocations.lastIndex

        private fun checkPattern(): Pair<Int, Int>? {
            val drop = (nextShapeIndex to nextPatternIndex)
            val indexOfLast = drops.indexOfLast { it.first == drop }
            if (indexOfLast < 0) return null
            val indexOfPrior = drops.indexOfLast { it.first == drop && it != drops[indexOfLast] }
            if (indexOfPrior < 0) return null
            val lastHeightDiff = this.height - drops[indexOfLast].second
            val priorHeightDiff = drops[indexOfLast].second - drops[indexOfPrior].second
            val lastLength = drops.size - indexOfLast
            val priorLength = indexOfLast - indexOfPrior
            // If the previous two repetitions had the same length and same height, we have a reliable pattern.
            return if (lastLength == priorLength && lastHeightDiff == priorHeightDiff) {
                lastLength to lastHeightDiff
            } else null
        }

        /**
         * If the next drop would be the beginning of a pattern that has already repeated twice, return the
         * number of iterations in the pattern and the height it produces.
         */
        fun findPattern(): Pair<Int, Int> {
            var pattern: Pair<Int, Int>?
            do {
                dropShape()
                pattern = checkPattern()
            } while (pattern == null)
            return pattern
        }

        fun dropShape() {
            drops += nextShapeIndex to nextPatternIndex to height
            val (shape, startLocation) = getNextShape()
            var currentLocation = startLocation
            do {
                val (newLocation, atRest) = tick(shape, currentLocation)
                currentLocation = newLocation
            } while (!atRest)
            shape.at(currentLocation).forEach {
                // Add rows to match current location
                repeat(it.y - filledLocations.lastIndex) { filledLocations.add(BooleanArray(7)) }
                filledLocations[it.y][it.x] = true
            }
        }

        fun tick(shape: Shape, currentLocation: Vector2): Pair<Vector2, Boolean> {
            // Jet Movement
            val moveBy = getNextJet()
            val afterJet = moveShapeIfPossible(shape, currentLocation, moveBy)

            // Downward Movement
            val afterDown = moveShapeIfPossible(shape, afterJet, DOWN)

            return afterDown to (afterJet == afterDown)
        }

        fun moveShapeIfPossible(shape: Shape, current: Vector2, moveBy: Vector2): Vector2 =
            (current + moveBy).let { newLoc ->
                if (shape.at(newLoc).all { it.isValidLocation() }) {
                    newLoc
                } else {
                    current
                }
            }

        fun Vector2.isValidLocation() =
            // We are within the walls (0 through 6 = width 7
            this.x >= 0 && this.x <= 6
                    // We have not filled to this point yet
                    && (filledLocations.lastIndex < this.y
                    // The tile is not already filled
                    || !filledLocations[this.y][this.x])

        private var nextShapeIndex = 0
        fun getNextShape(): Pair<Shape, Vector2> {
            val shape = shapes[nextShapeIndex]
            nextShapeIndex = (nextShapeIndex + 1) % shapes.size
            return shape to Vector2(2, filledLocations.size + 3)
        }

        private var nextPatternIndex = 0
        fun getNextJet(): Vector2 {
            val jet = when (pattern[nextPatternIndex]) {
                '<' -> LEFT
                '>' -> RIGHT
                else -> throw RuntimeException("Invalid pattern ${pattern[nextPatternIndex]}")
            }
            nextPatternIndex = (nextPatternIndex + 1) % pattern.length
            return jet
        }

        override fun toString(): String =
            filledLocations.reversed().joinToString("\n") { row ->
                row.joinToString("") { if (it) "#" else "." }
            }
    }

    private val shapes = arrayOf(
        Shape(arrayOf(
            "####"
        )),
        Shape(arrayOf(
            ".#.",
            "###",
            ".#."
        )),
        Shape(arrayOf(
            "..#",
            "..#",
            "###"
        )),
        Shape(arrayOf(
            "#",
            "#",
            "#",
            "#"
        )),
        Shape(arrayOf(
            "##",
            "##"
        ))
    )
}