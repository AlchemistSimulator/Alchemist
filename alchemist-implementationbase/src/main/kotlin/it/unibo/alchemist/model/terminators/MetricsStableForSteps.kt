package it.unibo.alchemist.model.terminators

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.TerminationPredicate

/**
 * [stepInterval] for how many steps the [metricsToCheck] should be stable.
 * [equalInterval] how many times it should be stable.
 */
class MetricsStableForSteps<T>(
    private val stepInterval: Long,
    private val equalInterval: Long,
    private val metricsToCheck: (Environment<T, Position<*>>) -> Map<String, T>,
) : TerminationPredicate<T, Position<*>> {
    private var stepsChecked: Long = 0
    private var equalSuccess: Long = 0
    private var lastUpdatedMetrics: Map<String, T> = emptyMap()

    init {
        require(stepInterval > 0 && equalInterval > 0) { "check and equal interval must be positive" }
    }

    override fun invoke(environment: Environment<T, Position<*>>): Boolean {
        val metrics: Map<String, T> = metricsToCheck(environment)
        require(metrics.isNotEmpty()) {
            "There should be at least one metric to check."
        }
        return when {
            lastUpdatedMetrics == metrics -> {
                if (++stepsChecked >= stepInterval) stepsChecked = 0
                ++equalSuccess >= equalInterval
            }
            else -> {
                reset(metrics)
                false
            }
        }
    }

    private fun reset(metrics: Map<String, T>) {
        stepsChecked = 0
        equalSuccess = 0
        lastUpdatedMetrics = metrics
    }
}
