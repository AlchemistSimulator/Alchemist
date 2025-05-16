package it.unibo.alchemist.model.terminators

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.TerminationPredicate
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.Time.Companion.INFINITY
import it.unibo.alchemist.model.Time.Companion.ZERO
import kotlin.Long.Companion.MAX_VALUE
import kotlin.require

/**
 * [stableForTotalSteps] for how many steps the [metricsToCheck] should be stable,
 * zero if taking into account just the time.
 * [checkStepInterval] every step it should check, if zero it checks at every invocation.
 * [stableForTotalTime] for how much time the [metricsToCheck] should be stable,
 * zero if taking into account just the steps.
 * [checkTimeInterval] every time-step it should check, if zero it checks at every invocation.
 * Both [stableForTotalSteps] and [stableForTotalTime] can be used together to check for stability.
 */
class StableMetrics<T>(
    private val stableForTotalSteps: Long,
    private val checkStepInterval: Long,
    private val stableForTotalTime: Time,
    private val checkTimeInterval: Time,
    private val metricsToCheck: (Environment<T, Position<*>>) -> Map<String, T>,
) : TerminationPredicate<T, Position<*>> {
    // steps-related checks
    private var lastStepsChecked: Long = 0
    private var stableSteps: Long = 0
    private var lastUpdatedMetricsSteps: Map<String, T> = emptyMap()

    // time-related checks
    private var lastTimeChecked: Time = ZERO
    private var stableTime: Time = ZERO
    private var lastUpdatedMetricsTime: Map<String, T> = emptyMap()
    init {
        require((stableForTotalTime > ZERO) || (stableForTotalSteps > 0)) {
            "At least one of the stability conditions (stableForTime or stableForSteps) must be greater than zero."
        }
        require(checkTimeInterval <= stableForTotalTime) {
            "The time interval to check should be less than or equal to the stable time."
        }
        require(checkStepInterval <= stableForTotalSteps) {
            "The step interval to check should be less than or equal to the stable steps."
        }
    }

    override fun invoke(environment: Environment<T, Position<*>>): Boolean = when {
        stableForTotalTime > ZERO && stableForTotalSteps > 0 -> {
            val timeStable = checkTime(environment)
            val stepStable = checkSteps(environment)
            timeStable && stepStable
        }
        stableForTotalTime > ZERO -> checkTime(environment)
        stableForTotalSteps > 0 -> checkSteps(environment)
        else -> error("This should never happen, at least one of the stability conditions must be greater than zero.")
    }

    private fun checkSteps(environment: Environment<T, Position<*>>): Boolean {
        require(stableForTotalSteps > 0) { "Steps check and equal interval must be positive." }
        val currentStep = environment.simulation.step
        val checkedStepsInterval = currentStep - lastStepsChecked
        return when {
            stableSteps == MAX_VALUE -> true
            checkedStepsInterval >= checkStepInterval -> {
                // update the last checked step and get the current metrics
                lastStepsChecked = currentStep
                val currentMetrics = metricsToCheck(environment).also {
                    require(it.isNotEmpty()) { "There should be at least one metric to check." }
                }
                when {
                    lastUpdatedMetricsSteps == currentMetrics -> { // metrics are the same as before = stable
                        stableSteps += checkedStepsInterval
                        return (stableSteps >= stableForTotalSteps).also { if (it) stableSteps = MAX_VALUE }
                    }
                    else -> { // reset the counters
                        stableSteps = 0
                        lastUpdatedMetricsSteps = currentMetrics
                        false
                    }
                }
            }
            else -> false
        }
    }

    private fun checkTime(environment: Environment<T, Position<*>>): Boolean {
        require(stableForTotalTime > ZERO) { "The amount of time to check the stability should be more than zero." }
        val currentTime = environment.simulation.time
        val checkedTimeInterval = currentTime - lastTimeChecked
        return when {
            stableTime == INFINITY -> true
            checkedTimeInterval >= checkTimeInterval -> {
                lastTimeChecked = currentTime
                val currentMetrics = metricsToCheck(environment).also {
                    require(it.isNotEmpty()) { "There should be at least one metric to check." }
                }
                when {
                    lastUpdatedMetricsTime == currentMetrics -> { // metrics are the same as before = stable
                        stableTime += checkedTimeInterval
                        return (stableTime >= stableForTotalTime).also { if (it) stableTime = INFINITY }
                    }
                    else -> { // reset the counters
                        stableTime = ZERO
                        lastUpdatedMetricsTime = currentMetrics
                        false
                    }
                }
            }
            else -> false
        }
    }
}
