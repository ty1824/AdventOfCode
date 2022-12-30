package advent

import advent.Day19.Resource.*
import kotlin.math.ceil
import kotlin.math.max

object Day19 : AdventDay {

    override fun part1(input: List<String>): Any {
        val blueprints = parseInput(input)
        val results = blueprints.map { it.simulateOptimalGeodesDfs(24) }
        debugln(results)
        return results.mapIndexed { index, value -> (index + 1) * value }.sum()
    }

    override fun part2(input: List<String>): Any {
        val blueprints = parseInput(input.take(3))
        val results = blueprints.map { it.simulateOptimalGeodesDfs(32) }
        debugln(results)
        return results.reduce(Int::times)
    }

    enum class Resource {
        Ore,
        Clay,
        Obsidian,
        Geode;

        companion object {
            val none: Map<Resource, Int> = Resource.values().associateWith { 0 }
        }
    }

    val resourcesToBuy: List<Resource> = Resource.values().toList().reversed()

    data class Robot(val produces: Resource, val cost: Map<Resource, Int>)
    data class Blueprint(val robots: Map<Resource, Robot>) {

        val maxCost: Map<Resource, Int> =
            Resource.values().associateWith { res -> (robots.values.mapNotNull { it.cost[res] }).maxOrNull() ?: 0 }

        fun costOf(robotType: Resource): Map<Resource, Int> = robots[robotType]!!.cost

        fun simulateOptimalGeodesDfs(duration: Int): Int {
            val initialState = SimState(
                duration,
                this,
                Resource.none,
                mapOf(Ore to 1, Clay to 0, Obsidian to 0, Geode to 0),
                null
            )
            var maxGeodes = SimState(0, this, Resource.none, mapOf(), null)
            depthFirstSearch(initialState) { state ->
                state.action().filter {
                    if (it.timeRemaining == 0) {
                        if (it.resources[Geode]!! > maxGeodes.resources[Geode]!!) {
                            maxGeodes = it
                            debugln("Candidate found: ${maxGeodes.resources[Geode]}")
                        }
                        false
                    } else it.maxPossibleGeodes() > maxGeodes.resources[Geode]!!
                }
            }

            debugln(maxGeodes.toString(true), 2)
            return maxGeodes.resources[Geode]!!
        }
    }

    private data class SimState(
        val timeRemaining: Int,
        val blueprint: Blueprint,
        val resources: Map<Resource, Int>,
        val robots: Map<Resource, Int>,
        val priorState: SimState?
    ) {
        fun toString(deep: Boolean): String {
            val builder = StringBuilder(if (deep) priorState?.toString(true) + "\n" else "")
            builder.appendLine("===============")
            builder.appendLine("$timeRemaining time left")
            Resource.values().forEach {
                builder.appendLine("    $it -> ${this.resources[it]} (+${this.robots[it]}) next: ${timeToAffordNextRobot(it)}")
            }
            return builder.toString()
        }

        fun timeToAffordNextRobot(type: Resource): Int =
            blueprint.robots[type]!!.cost.maxOf { (resource, cost) ->
                if ((robots[resource] ?: 0) > 0) {
                    ceil(max(cost - resources[resource]!!, 0) / robots[resource]!!.toFloat()).toInt() + 1
                } else {
                    Int.MAX_VALUE
                }
            }

        fun fastForward(): SimState = this.copy(
            timeRemaining = 0,
            blueprint = blueprint,
            resources = resources.mapValues { (res, value) -> value + robots[res]!! * timeRemaining },
            robots = robots,
        )

        fun maxPossibleGeodes(): Int {
            val base = this.resources[Geode]!! + (this.robots[Geode]!! * timeRemaining)
            var n = timeRemaining
            var extraBots = 0
            var extraGeodes = 0
            while (n > 0) {
                extraGeodes += extraBots
                extraBots++
                n--
            }
            return base + extraGeodes
        }

        fun action(): List<SimState> {
            return resourcesToBuy.map { it to timeToAffordNextRobot(it) }
                .filter { (type, timeToNext) ->
                    // Only add if we will be able to afford and we need more to maximize throughput
                    timeToNext < this.timeRemaining && (type == Geode || blueprint.maxCost[type]!! > robots[type]!!)
                }
                .map { (type, timeToNext) ->
                    val newResources = resources.mapValues { (res, value) -> value + robots[res]!! * timeToNext }
                    val cost = blueprint.costOf(type)
                    val resourcesAfterPurchase = newResources.mapValues { (res, value) -> value - (cost[res] ?: 0) }
                    val robotsAfterPurchase = robots.mapValues { (it, count) -> if (it == type) count + 1 else count }
                    this.copy(
                        timeRemaining = timeRemaining - timeToNext,
                        resources = resourcesAfterPurchase,
                        robots = robotsAfterPurchase,
                        priorState = this
                    )
                }.ifEmpty {
                    // Can't afford any more robots before time runs out. Skip to the end.
                    listOf(this.fastForward())
                }
        }
    }

    private val lineRegex = Regex("Blueprint \\d+: Each ore robot costs (\\d+) ore. Each clay robot costs (\\d+) ore. Each obsidian robot costs (\\d+) ore and (\\d+) clay. Each geode robot costs (\\d+) ore and (\\d+) obsidian.")
    private fun parseInput(input: List<String>): List<Blueprint> =
        input.map { line ->
            val match = lineRegex.matchEntire(line)!!.groupValues
            Blueprint(mapOf(
                Ore to Robot(Ore, mapOf(Ore to match[1].toInt())),
                Clay to Robot(Clay, mapOf(Ore to match[2].toInt())),
                Obsidian to Robot(Obsidian, mapOf(Ore to match[3].toInt(), Clay to match[4].toInt())),
                Geode to Robot(Geode, mapOf(Ore to match[5].toInt(), Obsidian to match[6].toInt()))
            ))
        }
}