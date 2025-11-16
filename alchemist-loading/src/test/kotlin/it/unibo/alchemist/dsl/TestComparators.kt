/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.dsl

import it.unibo.alchemist.boundary.Loader
import it.unibo.alchemist.model.Position

/**
 * Main test comparison class that handles static and runtime comparisons
 *
 * This class provides an interface for comparing DSL and YAML loaders
 * with the option to include runtime behavior testing.
 */
object TestComparators {

    /**
     * Compares a DSL loader with a YAML loader.
     *
     * @param dslLoader The DSL loader to compare.
     * @param yamlResource The YAML resource path to compare against.
     * @param includeRuntime Whether to include runtime behavior comparison.
     * @param steps The number of steps for runtime comparison (only used if includeRuntime is true).
     *   Exactly one of steps, targetTime, or stableForSteps must be provided.
     * @param targetTime Target time to run until (only used if includeRuntime is true).
     *   Exactly one of steps, targetTime, or stableForSteps must be provided.
     * @param stableForSteps If provided, terminates when environment is stable (checkInterval, equalIntervals).
     *   Exactly one of steps, targetTime, or stableForSteps must be provided.
     * @param timeTolerance Tolerance for time comparison in seconds (default: 0.01s).
     */
    fun <T, P : Position<P>> compare(
        dslLoader: () -> Loader,
        yamlResource: String,
        includeRuntime: Boolean = false,
        steps: Long? = null,
        targetTime: Double? = null,
        stableForSteps: Pair<Long, Long>? = null,
        timeTolerance: Double = 0.01,
    ) {
        val yamlLoader = LoaderFactory.loadYaml(yamlResource)

        // Always perform static comparison
        StaticComparisonHelper.compareBasicProperties(dslLoader(), yamlLoader)
        StaticComparisonHelper.compareSimulations<T, P>(dslLoader(), yamlLoader)

        // Optionally perform runtime comparison
        if (includeRuntime) {
            RuntimeComparisonHelper.compareLoaders<T, P>(
                dslLoader(),
                yamlLoader,
                steps = steps,
                targetTime = targetTime,
                stableForSteps = stableForSteps,
                timeTolerance = timeTolerance,
                positionTolerance = null,
            )
        }
    }

    /**
     * Compares DSL code with a YAML resource.
     *
     * @param dslCode The DSL code resource path.
     * @param yamlResource The YAML resource path to compare against.
     * @param includeRuntime Whether to include runtime behavior comparison.
     * @param steps The number of steps for runtime comparison (only used if includeRuntime is true).
     *   Exactly one of steps, targetTime, or stableForSteps must be provided.
     * @param targetTime Target time to run until (only used if includeRuntime is true).
     *   Exactly one of steps, targetTime, or stableForSteps must be provided.
     * @param stableForSteps If provided, terminates when environment is stable (checkInterval, equalIntervals).
     *   Exactly one of steps, targetTime, or stableForSteps must be provided.
     * @param timeTolerance Tolerance for time comparison in seconds (default: 0.01s).
     */
    fun <T, P : Position<P>> compare(
        dslCode: String,
        yamlResource: String,
        includeRuntime: Boolean = false,
        steps: Long? = null,
        targetTime: Double? = null,
        stableForSteps: Pair<Long, Long>? = null,
        timeTolerance: Double = 0.01,
    ) {
        compare<T, P>({
            LoaderFactory.loadDsl(dslCode)
        }, yamlResource, includeRuntime, steps, targetTime, stableForSteps, timeTolerance)
    }

    /**
     * Compares two loaders directly.
     *
     * @param dslLoader The DSL loader to compare.
     * @param yamlLoader The YAML loader to compare against.
     * @param includeRuntime Whether to include runtime behavior comparison.
     * @param steps The number of steps for runtime comparison (only used if includeRuntime is true).
     *   Exactly one of steps, targetTime, or stableForSteps must be provided.
     * @param targetTime Target time to run until (only used if includeRuntime is true).
     *   Exactly one of steps, targetTime, or stableForSteps must be provided.
     * @param stableForSteps If provided, terminates when environment is stable (checkInterval, equalIntervals).
     *   Exactly one of steps, targetTime, or stableForSteps must be provided.
     * @param timeTolerance Tolerance for time comparison in seconds (default: 0.01s).
     */
    fun <T, P : Position<P>> compare(
        dslLoader: Loader,
        yamlLoader: Loader,
        includeRuntime: Boolean = false,
        steps: Long? = null,
        targetTime: Double? = null,
        stableForSteps: Pair<Long, Long>? = null,
        timeTolerance: Double = 0.01,
    ) {
        // Always perform static comparison
        StaticComparisonHelper.compareBasicProperties(dslLoader, yamlLoader)
        StaticComparisonHelper.compareSimulations<T, P>(dslLoader, yamlLoader)

        // Optionally perform runtime comparison
        if (includeRuntime) {
            RuntimeComparisonHelper.compareLoaders<T, P>(
                dslLoader,
                yamlLoader,
                steps = steps,
                targetTime = targetTime,
                stableForSteps = stableForSteps,
                timeTolerance = timeTolerance,
                positionTolerance = null,
            )
        }
    }
}

private fun shouldUseDefaultSteps(
    includeRuntime: Boolean,
    steps: Long?,
    targetTime: Double?,
    stableForSteps: Pair<Long, Long>?,
): Boolean = includeRuntime && steps == null && targetTime == null && stableForSteps == null

/**
 * Extension function for easier test writing with static comparison only.
 */
fun Loader.shouldEqual(yamlResource: String) {
    @Suppress("UNCHECKED_CAST")
    TestComparators.compare<Any, Nothing>({ this }, yamlResource, includeRuntime = false)
}

/**
 * Extension function for comparing two loaders.
 *
 * @param other The other loader to compare against
 * @param includeRuntime Whether to include runtime behavior comparison
 * @param steps The number of steps for runtime comparison.
 *   If includeRuntime is true and no termination method is provided, defaults to 3000L.
 * @param targetTime Target time to run until.
 *   Exactly one of steps, targetTime, or stableForSteps must be provided when includeRuntime is true.
 * @param stableForSteps If provided, terminates when environment is stable (checkInterval, equalIntervals).
 *   Exactly one of steps, targetTime, or stableForSteps must be provided when includeRuntime is true.
 * @param timeTolerance Tolerance for time comparison in seconds (default: 0.01s)
 */
fun Loader.shouldEqual(
    other: Loader,
    includeRuntime: Boolean = true,
    steps: Long? = null,
    targetTime: Double? = null,
    stableForSteps: Pair<Long, Long>? = null,
    timeTolerance: Double = 0.01,
) {
    val effectiveSteps = if (shouldUseDefaultSteps(includeRuntime, steps, targetTime, stableForSteps)) {
        3000L
    } else {
        steps
    }
    @Suppress("UNCHECKED_CAST")
    TestComparators.compare<Any, Nothing>(
        this,
        other,
        includeRuntime,
        effectiveSteps,
        targetTime,
        stableForSteps,
        timeTolerance,
    )
}

/**
 * Extension function for comparing DSL function with YAML resource.
 *
 * @param yamlResource The YAML resource path to compare against
 * @param includeRuntime Whether to include runtime behavior comparison
 * @param steps The number of steps for runtime comparison.
 *   If includeRuntime is true and no termination method is provided, defaults to 3000L.
 * @param targetTime Target time to run until.
 *   Exactly one of steps, targetTime, or stableForSteps must be provided when includeRuntime is true.
 * @param stableForSteps If provided, terminates when environment is stable (checkInterval, equalIntervals).
 *   Exactly one of steps, targetTime, or stableForSteps must be provided when includeRuntime is true.
 * @param timeTolerance Tolerance for time comparison in seconds (default: 0.01s)
 *
 * @note For simulations to advance time, all reactions must have explicit time distributions.
 *       Reactions without time distributions default to "Infinity" rate, which schedules
 *       them at time 0.0, preventing time from advancing.
 *
 * @note Step-based terminators ensure both simulations execute the same number of steps,
 *       but final times may differ slightly due to randomness. Time-based terminators
 *       ensure both simulations reach approximately the same time, but step counts may differ.
 *       StableForSteps terminators ensure both simulations terminate at a stable state, which
 *       works well for deterministic simulations (e.g., ReproduceGPSTrace) but may not work
 *       for random simulations (e.g., BrownianMove) if reactions execute in different orders.
 */
fun (() -> Loader).shouldEqual(
    yamlResource: String,
    includeRuntime: Boolean = true,
    steps: Long? = null,
    targetTime: Double? = null,
    stableForSteps: Pair<Long, Long>? = null,
    timeTolerance: Double = 0.01,
) {
    val effectiveSteps = if (shouldUseDefaultSteps(includeRuntime, steps, targetTime, stableForSteps)) {
        3000L
    } else {
        steps
    }
    @Suppress("UNCHECKED_CAST")
    TestComparators.compare<Any, Nothing>(
        this,
        yamlResource,
        includeRuntime,
        effectiveSteps,
        targetTime,
        stableForSteps,
        timeTolerance,
    )
}
