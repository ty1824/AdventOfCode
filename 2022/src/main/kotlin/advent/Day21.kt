package advent

object Day21 : AdventDay {
    override fun part1(input: List<String>): Any {
        val tree = parseInput(input)
        return tree["root"]
    }

    override fun part2(input: List<String>): Any {
        val tree = parseInput(input)
        return tree.findHumnValue()
    }

    sealed interface Expression {
        fun resolve(tree: ExpressionTree): Long
    }

    data class Constant(val value: Long) : Expression {
        override fun resolve(tree: ExpressionTree): Long = value
    }

    data class BinaryExpression(val operation: Char, val left: String, val right: String) : Expression {
        override fun resolve(tree: ExpressionTree): Long = when (operation) {
            '+' -> tree[left] + tree[right]
            '-' -> tree[left] - tree[right]
            '*' -> tree[left] * tree[right]
            '/' -> tree[left] / tree[right]
            else -> throw RuntimeException("Invalid operation $this")
        }

        fun inverseResolve(result: Long, tree: ExpressionTree): Long {
            val leftContainsHumn = tree.containsHumn(this.left, listOf()) != null
            return if (leftContainsHumn) {
                val leftExpr = tree.map[left]
                if (this.left == "humn") {
                    return inverseEvaluateLeft(operation, result, tree[right])
                } else if (leftExpr is Constant) leftExpr.value
                else {
                    (leftExpr as BinaryExpression).inverseResolve(inverseEvaluateLeft(operation, result, tree[right]), tree)
                }
            } else {
                val rightExpr = tree.map[right]
                if (this.right == "humn") {
                    inverseEvaluateRight(operation, result, tree[left])
                } else if (rightExpr is Constant) rightExpr.value
                else {
                    (rightExpr as BinaryExpression).inverseResolve(inverseEvaluateRight(operation, result, tree[left]), tree)
                }
            }

        }

    }

    fun inverseEvaluateLeft(operation: Char, result: Long, operand: Long) =
        when (operation) {
            '+' -> result - operand
            '-' -> result + operand
            '*' -> result / operand
            '/' -> result * operand
            else -> throw RuntimeException("blargh")
        }

    fun inverseEvaluateRight(operation: Char, result: Long, operand: Long) =
        when (operation) {
            '+' -> result - operand
            '-' -> operand - result
            '*' -> result / operand
            '/' -> operand / result
            else -> throw RuntimeException("blargh")
        }

    class ExpressionTree(val map: Map<String, Expression>) {
        operator fun get(id: String): Long = map[id]!!.resolve(this)

        fun findHumnValue(): Long {
            val root = map["root"]!! as BinaryExpression
            val toExplore = if (containsHumn(root.left, listOf()) != null) {
                root.left to this[root.right]
            } else {
                root.right to this[root.left]
            }
            return (map[toExplore.first] as BinaryExpression).inverseResolve(toExplore.second, this)
        }

        fun containsHumn(id: String, path: List<String>): List<String>? {
            val expression = map[id]!!
            val newPath = path + id
            return when {
                id == "humn" -> newPath
                expression is Constant -> null
                expression is BinaryExpression -> {
                    containsHumn(expression.left, newPath) ?: containsHumn(expression.right, newPath)
                }
                else -> throw RuntimeException("$id from $path")
            }
        }
    }

    val regex = Regex("([a-z]+): ((?:([a-z]+) ([+\\-*/]) ([a-z]+))|(\\d+))")
    fun parseInput(input: List<String>): ExpressionTree {
        return ExpressionTree(input.associate { line ->
            val matches = regex.matchEntire(line)!!.groupValues.drop(0)
            if (matches[4].isNotBlank()) {
                matches[1] to BinaryExpression(matches[4][0], matches[3], matches[5])
            } else {
                matches[1] to Constant(matches[6].toLong())
            }
        })
    }
}