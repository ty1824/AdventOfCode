package advent

object Day20 : AdventDay {
    override fun part1(input: List<String>): Any {
        val modules = parseInput(input)
        var lowPulses = 0L
        var highPulses = 0L
        repeat(1000) {
            val (low, high) = pressButton(modules)
            lowPulses += low
            highPulses += high
        }
        return lowPulses * highPulses
    }

    override fun part2(input: List<String>): Any {
        val modules = parseInput(input)
        val loops = pressButtonRx(modules)
        return loops.values.reduce(Util::lcm)
    }


    fun pressButtonRx(state: MachineModules): Map<String, Long> {
        val targetedBy = state.targetedBy["dd"]!!
        val loopStart = targetedBy.associateWith { -1L }.toMutableMap()
        val loopLen = targetedBy.associateWith { -1L }.toMutableMap()
        var buttons = 0L
        while (loopLen.values.any { it < 0 }) {
            buttons++
            breadthFirstSearch(listOf(Pulse(false, "button", listOf("broadcast")))) { pulse ->
                pulse.targets.mapNotNull { target ->
                    val targetModule = state.modules[target]
                    if (target == "dd" && targetModule is Conjunction && pulse.signal == true) {
                        if (targetModule.state[pulse.from] != pulse.signal) {
                            if (loopStart[pulse.from]!! < 0) {
                                loopStart[pulse.from] = buttons
                            } else if (loopLen[pulse.from]!! < 0) {
                                loopLen[pulse.from] = buttons - loopStart[pulse.from]!!
                            }
                        }
                    }
                    targetModule?.handleSignal(pulse.from, pulse.signal)
                }
            }
        }
        return loopLen
    }

    fun pressButton(state: MachineModules): Pair<Long, Long> {
        var lowPulses: Long = 0
        var highPulses: Long = 0
        breadthFirstSearch(listOf(Pulse(false, "button", listOf("broadcast")))) { pulse ->
            pulse.targets.mapNotNull { target ->
                if (pulse.signal) {
                    highPulses++
                } else {
                    lowPulses++
                }
                state.modules[target]?.handleSignal(pulse.from, pulse.signal)
            }
        }
        return lowPulses to highPulses
    }

    class MachineModules(val modules: Map<String, Module>, val targetedBy: Map<String, List<String>>)
    class Pulse(val signal: Boolean, val from: String, val targets: List<String>)
    interface Module {
        val label: String
        val targets: List<String>
        fun handleSignal(from: String, signal: Boolean): Pulse?
    }
    data class Broadcast(override val label: String, override val targets: List<String>): Module {
        override fun handleSignal(from: String, signal: Boolean): Pulse = Pulse(signal, label, targets)
    }

    data class FlipFlop(
        override val label: String,
        override val targets: List<String>,
        var state: Boolean = false
    ) : Module {
        override fun handleSignal(from: String, signal: Boolean): Pulse? {
            return if (!signal) {
                state = !state
                Pulse(state, label, targets)
            } else null
        }
    }
    data class Conjunction(
        override val label: String,
        override val targets: List<String>, val
        state: MutableMap<String, Boolean> = mutableMapOf()
    ) : Module {

        override fun handleSignal(from: String, signal: Boolean): Pulse? {
            state[from] = signal
            return if (state.all { it.value }) {
                Pulse(false, label, targets)
            } else {
                Pulse(true, label, targets)
            }
        }
    }

    fun parseInput(input: List<String>): MachineModules {
        val modules = mutableMapOf<String, Module>()
        val targetedBy = mutableMapOf<String, MutableList<String>>()
        input.forEach { line ->
            val (labelStr, targetStr) = line.split(" -> ")
            val targets = targetStr.split(", ")
            val label = labelStr.drop(1)
            when (labelStr[0]) {
                'b' -> {
                    targets.forEach { targetedBy.computeIfAbsent(it) { mutableListOf() } += "broadcast" }
                    modules["broadcast"] = Broadcast("broadcast", targets)
                }
                '%' -> {
                    targets.forEach { targetedBy.computeIfAbsent(it) { mutableListOf() } += label }
                    modules[label] = FlipFlop(label, targets)
                }
                '&' -> {
                    targets.forEach { targetedBy.computeIfAbsent(it) { mutableListOf() } += label }
                    modules[label] = Conjunction(label, targets)
                }
            }
        }
        modules.forEach { (key, mod) ->
            if (mod is Conjunction) {
                targetedBy[key]!!.forEach {
                    mod.state[it] = false
                }
            }
        }
        return MachineModules(modules, targetedBy)
    }
}