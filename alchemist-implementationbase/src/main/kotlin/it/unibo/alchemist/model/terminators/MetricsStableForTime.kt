package it.unibo.alchemist.model.terminators

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.TerminationPredicate
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.Time.Companion.ZERO

/**
 * [stableForTime] for how much time the [metricsToCheck] should be stable.
 * [timeIntervalToCheck] every time-step it should check, if zero it checks at every invocation.
 * [equalTimes] how many times it should be stable.
 */
class MetricsStableForTime<T>
@JvmOverloads
constructor(
    private val stableForTime: Time,
    private val timeIntervalToCheck: Time = ZERO,
    private val equalTimes: Long,
    private val metricsToCheck: (Environment<T, Position<*>>) -> Map<String, T>,
) : TerminationPredicate<T, Position<*>> {
    private var timeStabilitySuccess: Time = ZERO
    private var lastChecked: Time = ZERO
    private var equalSuccess: Long = 0
    private var lastUpdatedMetrics: Map<String, T> = emptyMap()

    init {
        require(stableForTime > ZERO) {
            "The amount of time to check the stability should be more than zero."
        }
    }

    override fun invoke(environment: Environment<T, Position<*>>): Boolean {
        val simulationTime = environment.simulation.time
        val checkedInterval = simulationTime - lastChecked
        return when {
            checkedInterval >= timeIntervalToCheck -> {
                val metrics: Map<String, T> = metricsToCheck(environment).also {
                    require(it.isNotEmpty()) { "There should be at least one metric to check." }
                }
                lastChecked = simulationTime
                when {
                    lastUpdatedMetrics == metrics -> {
                        timeStabilitySuccess += checkedInterval
                        if (timeStabilitySuccess >= stableForTime) timeStabilitySuccess = ZERO
                        ++equalSuccess >= equalTimes
                    }
                    else -> {
                        reset(metrics)
                        false
                    }
                }
            }
            else -> false
        }
    }

    private fun reset(metrics: Map<String, T>) {
        timeStabilitySuccess = ZERO
        equalSuccess = 0
        lastUpdatedMetrics = metrics
    }
}
