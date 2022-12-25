package advent

import advent.Day19.Resource.*
import kotlin.math.max

object Day19 : AdventDay {
    override fun part1(input: List<String>): Any {
        val blueprints = parseInput(input)
        val results = blueprints.map {
            it.simulateOptimalGeodes(24)
        }
        println(results)
        return results.mapIndexed { index, value -> (index + 1) * value }.sum()
    }

    override fun part2(input: List<String>): Any {
        val blueprints = parseInput(input.take(3))
        val results = blueprints.map {
            it.simulateOptimalGeodes(32)
        }
        println(results)
        return results.reduce(Int::times)
    }

    enum class Resource {
        Ore,
        Clay,
        Obsidian,
        Geode
    }

    val resourcesToBuy: List<Resource> = Resource.values().toList()

    data class Robot(val produces: Resource, val cost: Map<Resource, Int>)
    data class Blueprint(val robots: Map<Resource, Robot>) {

        val maxCost: Map<Resource, Int> =
            Resource.values().associateWith { res -> (robots.values.mapNotNull { it.cost[res] }).maxOrNull() ?: 0 }

        fun simulateOptimalGeodes(duration: Int): Int {
            var currentState = sequenceOf(
                SimulationState(
                    duration,
                    this,
                    Resource.values().associateWith { 0 },
                    mapOf(Ore to 1, Clay to 0, Obsidian to 0, Geode to 0),
                    Ore
                ),
                SimulationState(
                    duration,
                    this,
                    Resource.values().associateWith { 0 },
                    mapOf(Ore to 1, Clay to 0, Obsidian to 0, Geode to 0),
                    Clay
                )
            )
            val resolvedStates = mutableListOf<SimulationState>()
            while (currentState.any()) {
                val maxUnresolvedGeodes = currentState.maxOfOrNull { it.resources[Geode]!! } ?: 0
                val maxResolvedGeodes = resolvedStates.maxOfOrNull { it.resources[Geode]!! } ?: 0
//                val averageGeodes = unresolved.sumOf { it.resources[Geode]!! } / unresolved.count()
                debugln("Time remaining: ${currentState.first().timeRemaining}, Possible states: ${resolvedStates.count()} resolved / ${currentState.count()} active, Maximum geodes: ${currentState.maxBy { it.resources[Geode]!! }.resources[Geode]!!}")
                val nextState = currentState.filter {
                    val maxPossibleGeodes = it.maxPossibleGeodes()
                    maxPossibleGeodes > max(maxResolvedGeodes, maxUnresolvedGeodes)
                }.flatMap { it.action() }.distinct().toList().asSequence()

                resolvedStates += nextState.filter { it.timeRemaining == 0}
                currentState = nextState.filter { it.timeRemaining != 0 }.toList().asSequence()
                // Beware Ye Hacky Optimization
                val maxBotsPerType = Resource.values().associateWith { res -> currentState.maxOfOrNull { it.robots[res]!! } ?: 0 }
                currentState = currentState.filter {
                    when {
                        maxBotsPerType[Geode]!! > 0 -> it.robots[Geode]!! >= maxBotsPerType[Geode]!! - 1
                        maxBotsPerType[Obsidian]!! > 0 -> it.robots[Obsidian]!! >= maxBotsPerType[Obsidian]!! - 2
                        maxBotsPerType[Clay]!! > 0 -> it.robots[Clay]!! >= maxBotsPerType[Clay]!! - 3
                        else -> true
                    }
                }.toList().asSequence()
            }
            return resolvedStates.maxOf { it.resources[Geode]!! }
        }
    }

    private data class SimulationState(
        val timeRemaining: Int,
        val blueprint: Blueprint,
        val resources: Map<Resource, Int>,
        val robots: Map<Resource, Int>,
        val toBuy: Resource,
    ) {
        fun canAfford(type: Resource): Boolean =
            blueprint.robots[type]!!.cost.all { (resource, cost) -> resources[resource]!! >= cost }

        fun timeToAffordNextRobot(type: Resource): Int =
            blueprint.robots[type]!!.cost.minOf { (resource, cost) ->
                if (robots[resource]!! > 0) {
                    max(cost - resources[resource]!!, 0) / robots[resource]!!
                } else {
                    -1
                }
            }

        fun fastForward(): SimulationState = this.copy(
            timeRemaining = 0,
            blueprint = blueprint,
            resources = resources.mapValues { (res, value) -> value + robots[res]!! * timeRemaining },
            robots = robots,
            toBuy = toBuy
        )

        fun maxPossibleGeodes(): Int {
            val base = this.resources[Geode]!! + (this.robots[Geode]!! * timeRemaining)
            var n = timeRemaining - timeToAffordNextRobot(toBuy)
            var extraBots = 0
            var extraGeodes = 0
            while (n > 0) {
                extraGeodes += extraBots
                extraBots++
                n--
            }
            return base + extraGeodes
        }

        fun action(): List<SimulationState> {
            if (timeRemaining == 0) return listOf(this)
            val newResources = resources.mapValues { (res, value) -> value + robots[res]!! }
            if (timeRemaining == 1) return listOf(this.copy(
                timeRemaining = 0,
                resources = newResources
            ))
            return if (canAfford(toBuy)) {
                val cost = blueprint.robots[toBuy]!!.cost
                val resourcesAfterPurchase = newResources.mapValues { (res, value) ->
                    value - (cost[res] ?: 0)
                }
                val robotsAfterPurchase =
                    robots.mapValues { (type, count) -> if (type == toBuy) count + 1 else count }
                resourcesToBuy.filter {
                    // Only add if we need more to maximize throughput
                    it == Geode || blueprint.maxCost[it]!! > robotsAfterPurchase[it]!!
                }.map { newToBuy ->
                    val temp = this.copy(
                        timeRemaining = timeRemaining - 1,
                        resources = resourcesAfterPurchase,
                        robots = robotsAfterPurchase,
                        toBuy = newToBuy,
                    )

                    val timeToAffordNextRobot = temp.timeToAffordNextRobot(temp.toBuy)
                    when {
                        timeToAffordNextRobot < 0 -> temp.fastForward()
                        temp.toBuy == Geode && timeToAffordNextRobot < timeRemaining -> temp
                        temp.toBuy == Clay && timeToAffordNextRobot < timeRemaining - 2 -> temp
                        timeToAffordNextRobot in 0 until timeRemaining - 2 -> temp
                        else -> {
                            temp.fastForward()
                        }
                    }
                }
            } else {
                listOf(this.copy(
                    timeRemaining = timeRemaining - 1,
                    resources = newResources,
                ))
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