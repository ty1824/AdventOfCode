package advent

import org.junit.Test
import kotlin.test.assertTrue


class Day22Test {
    @Test
    fun cubeTest() {
        val cubeInputs = listOf(
            """
            |..
            |..
            |........
            |........
            |      ..
            |      ..
            """.trimMargin(),
            """
            |      ..
            |      ..
            |........
            |........
            |..
            |..
            """.trimMargin(),
            """
            |    ..
            |    ..
            |........
            |........
            |    ..
            |    ..
            """.trimMargin(),
            """
            |  ....
            |  ....
            |  ..
            |  ..
            |....
            |....
            |..
            |..
            """.trimMargin()
        )
        val failures = cubeInputs.flatMapIndexed { index, cubeInput ->
            println("Testing cube ${index+1}")
            (0 until 6).flatMap { face ->
                Vector2(0..1, 0..1).mapNotNull { startPos ->
                    val init = Triple(face, startPos, 0)
                    val result = Day22.runSample(cubeInput.lines(), "8L8L8L8L", init)
                    println("  $init == $result ${if (init == result) "Success!" else "Failed :("}")
                    if (init != result) {
                        "Cube $index: Expected $init but was $result"
                    } else null
                }
            }
        }
        assertTrue(failures.isEmpty(), failures.joinToString("\n"))

    }
}