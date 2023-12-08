package advent

object Day2 : AdventDay {
    override fun part1(input: List<String>): Any =
        input.map {
            parseLine(it)
        }.filter {
            part1IsPossible(it)
        }.sumOf {
            it.id
        }

    override fun part2(input: List<String>): Any =
        input.map {
            parseLine(it)
        }.map {
            val red = it.revealedCubes.maxOf {
                it.cubes["red"] ?: 0
            }
            val green = it.revealedCubes.maxOf {
                it.cubes["green"] ?: 0
            }
            val blue = it.revealedCubes.maxOf {
                it.cubes["blue"] ?: 0
            }
            red * green * blue
        }.sum()

    data class Game(val id: Int, val revealedCubes: List<Reveal>)
    data class Reveal(val cubes: Map<String, Int>)

    fun parseLine(input: String): Game {
        val (game, rest) = input.split(": ")
        val gameId = game.split(" ")[1].toInt()

        val reveals = rest.split("; ").map { reveal ->
            Reveal(reveal.split(", ").map {
                val (count, color) = it.split(" ")
                color to count.toInt()
            }.toMap())
        }
        return Game(gameId, reveals)
    }

    fun part1IsPossible(game: Game): Boolean =
        game.revealedCubes.none {
            (it.cubes["red"] ?: 0) > 12
                || (it.cubes["green"] ?: 0) > 13
                || (it.cubes["blue"] ?: 0) > 14
        }
}