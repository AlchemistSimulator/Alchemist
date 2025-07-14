package it.unibo.alchemist.model.terminators

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.TerminationPredicate
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.Time.Companion.INFINITY
import it.unibo.alchemist.model.Time.Companion.ZERO
import kotlin.Long.Companion.MAX_VALUE

/**
 * [stableForTotalSteps] for how many steps the [metricsToCheck] should be stable,
 * zero if taking into account just the time.
 * [checkStepInterval] every step it should check, if zero it checks at every invocation.
 * [stableForTotalTime] for how much time the [metricsToCheck] should be stable,
 * zero if taking into account just the steps.
 * [checkTimeInterval] every time-step it should check, if zero it checks at every invocation.
 */
class StableMetrics<T>(
    private val stableForTotalSteps: Long, // total steps to be stable
    private val checkStepInterval: Long, // steps interval to check
    private val stableForTotalTime: Time, // total time to be stable
    private val checkTimeInterval: Time, // time interval to check
    private val metricsToCheck: (Environment<T, Position<*>>) -> Map<String, T>,
) : TerminationPredicate<T, Position<*>> {
    constructor(
        stableForTotalSteps: Long,
        checkStepInterval: Long,
        metricsToCheck: (Environment<T, Position<*>>) -> Map<String, T>,
    ) : this(
        stableForTotalSteps = stableForTotalSteps,
        checkStepInterval = checkStepInterval,
        stableForTotalTime = ZERO,
        checkTimeInterval = ZERO,
        metricsToCheck = metricsToCheck,
    )
    constructor(
        stableForTotalTime: Time,
        checkTimeInterval: Time,
        metricsToCheck: (Environment<T, Position<*>>) -> Map<String, T>,
    ) : this(
        stableForTotalSteps = 0,
        checkStepInterval = 0,
        stableForTotalTime = stableForTotalTime,
        checkTimeInterval = checkTimeInterval,
        metricsToCheck = metricsToCheck,
    )

    private val stepTracker by lazy { StepMetricTracker() }
    private val timeTracker by lazy { TimeMetricTracker() }
    init {
        require(stableForTotalTime > ZERO || (stableForTotalSteps > 0)) {
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
        stableForTotalTime > DEFAULT_TIME && stableForTotalSteps > DEFAULT_STEP -> {
            val stableSteps = checkStability(environment, stepTracker, STEP_MAX_VALUE, DEFAULT_STEP)
            val stableTime = checkStability(environment, timeTracker, TIME_MAX_VALUE, DEFAULT_TIME)
            stableTime && stableSteps
        }
        stableForTotalTime > DEFAULT_TIME ->
            checkStability(environment, timeTracker, TIME_MAX_VALUE, DEFAULT_TIME)
        stableForTotalSteps > DEFAULT_STEP ->
            checkStability(environment, stepTracker, STEP_MAX_VALUE, DEFAULT_STEP)
        else -> error("This should never happen.")
    }

    private fun <Type : Comparable<Type>> checkStability(
        env: Environment<T, Position<*>>,
        tracker: MetricTracker<T, Type>,
        maxValue: Type,
        defaultValue: Type,
    ): Boolean = with(tracker) {
        val current = current(env)
        val interval = evaluateInterval(current)
        return when {
            stableValue >= maxValue -> true
            shouldBeChecked(interval) -> {
                lastChecked = current
                val currentMetrics = metricsToCheck(env).also {
                    require(it.isNotEmpty()) { "There should be at least one metric to check." }
                }
                when {
                    currentMetrics == lastMetrics -> {
                        increaseStability(interval)
                        return (stableValue >= totalStability).also { if (it) stableValue = maxValue }
                    }
                    else -> {
                        stableValue = defaultValue
                        lastMetrics = currentMetrics
                        false
                    }
                }
            }
            else -> false
        }
    }

    private interface MetricTracker<T, Type : Comparable<Type>> {
        var lastChecked: Type
        var stableValue: Type
        var lastMetrics: Map<String, T>
        val totalStability: Type
        val checkInterval: Type
        fun current(env: Environment<T, Position<*>>): Type
        fun evaluateInterval(current: Type): Type
        fun shouldBeChecked(interval: Type): Boolean = interval >= checkInterval
        fun increaseStability(interval: Type)
    }

    private inner class StepMetricTracker : MetricTracker<T, Long> {
        override var lastChecked = DEFAULT_STEP
        override var stableValue = DEFAULT_STEP
        override var lastMetrics: Map<String, T> = emptyMap()
        override val totalStability = stableForTotalSteps
        override val checkInterval = checkStepInterval
        override fun current(env: Environment<T, Position<*>>): Long = env.simulation.step
        override fun evaluateInterval(current: Long): Long = current - lastChecked
        override fun increaseStability(interval: Long) {
            stableValue += interval
        }
    }

    private inner class TimeMetricTracker : MetricTracker<T, Time> {
        override var lastChecked = DEFAULT_TIME
        override var stableValue = DEFAULT_TIME
        override var lastMetrics: Map<String, T> = emptyMap()
        override val totalStability = stableForTotalTime
        override val checkInterval = checkTimeInterval
        override fun current(env: Environment<T, Position<*>>): Time = env.simulation.time
        override fun evaluateInterval(current: Time): Time = current - lastChecked
        override fun increaseStability(interval: Time) {
            stableValue += interval
        }
    }

    /**
     * Companion object for StableMetrics.
     *
     * Provides constants relevant to the stability checks, such as maximum
     * and default values for steps and time.
     */
    companion object {
        /**
         * The maximum value for steps, used to determine when the step stability is reached.
         */
        const val STEP_MAX_VALUE: Long = MAX_VALUE

        /**
         * The default value for steps, used when no stability is required.
         */
        const val DEFAULT_STEP: Long = 0L

        /**
         * The maximum value for time, used to determine when the time stability is reached.
         */
        val TIME_MAX_VALUE: Time = INFINITY

        /**
         * The default time value, used when no stability is required.
         */
        val DEFAULT_TIME: Time = ZERO
    }
}
