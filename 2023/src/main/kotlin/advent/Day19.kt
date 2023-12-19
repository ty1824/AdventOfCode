package advent

import advent.Day13.split

object Day19 : AdventDay {
    override fun part1(input: List<String>): Any {
        val (workflows, parts) = parseInput(input)
        val engine = WorkflowEngine(workflows)
        return parts.filter { engine.check(it) }
            .sumOf { it.values.values.sum() }
    }

    override fun part2(input: List<String>): Any {
        val (workflows, parts) = parseInput(input)
        val engine = WorkflowEngine(workflows)
        return engine.runVectorized().sumOf { it.possibilities() }
    }

    fun IntRange.split(at: Int): Pair<IntRange, IntRange> = (this.first until at) to at..this.last

    data class RatingRange(val ranges: Map<String, IntRange>) {
        fun split(on: String, at: Int): Pair<RatingRange, RatingRange> {
            val (newLeft, newRight) = ranges[on]!!.split(at)
            val newRanges = ranges.toMutableMap()
            newRanges[on] = newLeft
            val left = RatingRange(newRanges.toMap())
            newRanges[on] = newRight
            val right = RatingRange(newRanges.toMap())
            return left to right
        }

        fun possibilities(): Long = ranges.values.fold(1L) { acc, range ->
            acc * (range.last.toLong() - range.first.toLong() + 1L)
        }
    }

    class WorkflowEngine(val workflows: Map<String, Workflow>) {
        fun check(input: Part): Boolean = executeWorkflow(workflows["in"]!!, input)

        fun executeWorkflow(workflow: Workflow, input: Part): Boolean {
            var current: Workflow? = workflow
            while (current != null) {
                val rule = current.rules.first { it.expression == null || evaluateExpression(it.expression, input) > 0 }
                when (rule.action) {
                    is GoToRule -> current = workflows[rule.action.targetRule]
                    is Accept -> return true
                    is Reject -> return false
                }
            }
            throw RuntimeException("What??")
        }

        fun evaluateExpression(expr: BinaryExpression, input: Part): Int =
            expr.op.evaluate(input.values[expr.ref]!!, expr.right).let { if (it) 1 else 0 }

        fun runVectorized(): List<RatingRange> {
            val startingRange = 1..4000
            val approvedRanges = mutableListOf<RatingRange>()
            val initial = RatingRange(
                mapOf(
                    "x" to startingRange,
                    "m" to startingRange,
                    "a" to startingRange,
                    "s" to startingRange
                )
            )
            var iterations = 0
            breadthFirstSearch(
                listOf("in" to initial),
            ) { (workflowRef, range) ->
                val result = executeWorkflowVectorized(workflows[workflowRef]!!, range)
                approvedRanges += result.completed
                result.toRun
            }

            return approvedRanges
        }

        class VectorizedWorkflowResult(
            val completed: List<RatingRange>,
            val toRun: List<Pair<String, RatingRange>>
        )

        var v = true
        fun executeWorkflowVectorized(workflow: Workflow, input: RatingRange): VectorizedWorkflowResult {
            val completed = mutableListOf<RatingRange>()
            val toRun = mutableListOf<Pair<String, RatingRange>>()
            var current = input
            for (rule in workflow.rules) {
                val expressionResult = rule.expression?.let {
                    executeExpressionVectorized(it, current)
                } ?: FullMatch(current)

                when (expressionResult) {
                    is FullMatch -> {
                        when (val action = rule.action) {
                            is GoToRule -> toRun += action.targetRule to expressionResult.range
                            Accept -> completed += expressionResult.range
                            Reject -> {} // no-op
                        }
                        break
                    }
                    is PartialMatch -> {
                        when (val action = rule.action) {
                            is GoToRule -> toRun += action.targetRule to expressionResult.match
                            Accept -> completed += expressionResult.match
                            Reject -> {} // no-op
                        }
                        current = expressionResult.notMatch
                    }
                    NoMatch -> {}
                }
            }

            return VectorizedWorkflowResult(completed, toRun)
        }

        sealed interface VectorizedExpressionResult
        object NoMatch : VectorizedExpressionResult
        data class FullMatch(val range: RatingRange) : VectorizedExpressionResult
        data class PartialMatch(val match: RatingRange, val notMatch: RatingRange): VectorizedExpressionResult

        fun executeExpressionVectorized(expr: BinaryExpression, input: RatingRange): VectorizedExpressionResult {
            val targetRange = expr.ref
            val rangeForRef = input.ranges[targetRange]!!
            val operand = expr.right
            return when (expr.op) {
                Operator.GT -> when {
                    operand < rangeForRef.first -> FullMatch(input)
                    operand > rangeForRef.last -> NoMatch
                    else -> input.split(targetRange, operand + 1).let {
                        (notMatch, match) -> PartialMatch(match, notMatch)
                    }
                }
                Operator.LT -> when {
                    operand > rangeForRef.last -> FullMatch(input)
                    operand < rangeForRef.first -> NoMatch
                    else -> input.split(targetRange, operand).let {
                        (match, notMatch) -> PartialMatch(match, notMatch)
                    }
                }
            }
        }
    }


    enum class Operator(val evaluate: (Int, Int) -> Boolean) {
        GT({ a, b -> a > b }),
        LT({ a, b -> a < b })
    }
    data class BinaryExpression(val ref: String, val op: Operator, val right: Int)

    sealed interface Action
    data class GoToRule(val targetRule: String) : Action
    object Accept : Action
    object Reject : Action
    data class Rule(val expression: BinaryExpression?, val action: Action)

    data class Workflow(val rules: List<Rule>)
    data class Part(val values: Map<String, Int>)

    fun parseInput(input: List<String>): Pair<Map<String, Workflow>, List<Part>> {
        val (workflowStrs, partStrs) = input.split { it.isEmpty() }
        val workflows = workflowStrs.associate { it.substringBefore("{") to parseWorkflow(it.substringAfter("{")) }
        val parts = partStrs.map { parsePart(it)}
        return workflows to parts
    }

    fun parseWorkflow(workflowStr : String): Workflow =
        Workflow(workflowStr.substringBefore("}").split(",").map { parseRule(it) })


    fun parseRule(ruleStr: String): Rule {
        val components = ruleStr.split(":")
        return if (components.size == 1) {
            Rule(null, parseAction(components[0]))
        } else {
            Rule(parseBinaryExpression(components[0]), parseAction(components[1]))
        }
    }

    val exprRegex = Regex("([^<>]+)([<>])([^<>]+)")
    fun parseBinaryExpression(exprStr: String): BinaryExpression {
        val match = exprRegex.matchEntire(exprStr) ?: throw RuntimeException("failed on $exprStr")
        val left = match.groupValues[1]
        val op = when (match.groupValues[2]) {
            "<" -> Operator.LT
            ">" -> Operator.GT
            else -> throw RuntimeException("Oops bad op: ${match.groupValues[2]}")
        }
        val right = match.groupValues[3]
        return BinaryExpression(left, op, right.toInt())
    }

    fun parseAction(actionStr: String) : Action = when (actionStr) {
        "A" -> Accept
        "R" -> Reject
        else -> GoToRule(actionStr)
    }

    fun parsePart(partStr: String): Part =
        Part(
            partStr.substring(1, partStr.length - 1).split(",")
                .associate {
                    it.split("=").let { components ->
                        components[0] to components[1].toInt()
                    }
                }
        )

}