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
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.core.Status
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.TerminationPredicate
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.terminators.AfterTime
import it.unibo.alchemist.model.terminators.StableForSteps
import it.unibo.alchemist.model.terminators.StepCount
import it.unibo.alchemist.model.times.DoubleTime
import kotlin.math.abs
import kotlin.math.max
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail

/**
 * Helper for comparing DSL and YAML loaders by running simulations and comparing final states
 *
 * This class focuses on runtime behavior comparison by executing both simulations
 * for a specified duration and comparing their final states.
 *
 */
object RuntimeComparisonHelper {

    /**
     * Compares loaders by running both simulations and comparing their final states.
     *
     * @param dslLoader The DSL loader to compare
     * @param yamlLoader The YAML loader to compare
     * @param steps The number of steps to run before comparing
     *   (if null, uses targetTime or stableForSteps instead)
     * @param targetTime Target time to run until (if null, uses steps or stableForSteps instead).
     *   Only one termination method should be provided.
     * @param stableForSteps If provided, terminates when environment is stable (checkInterval, equalIntervals).
     *   Only one termination method should be provided.
     * @param timeTolerance Tolerance for time comparison in seconds (default: 0.01s)
     * @param positionTolerance Maximum distance between positions to consider them matching.
     *   If null, calculated as max(timeTolerance * 10, 1e-6).
     *   For random movement tests, consider using a larger value (e.g., 1.0 or more).
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
     *       Small timing differences are expected even with time-based terminators due to thread
     *       scheduling and the terminator being checked after each step completes.
     */
    fun <T, P : Position<P>> compareLoaders(
        dslLoader: Loader,
        yamlLoader: Loader,
        steps: Long? = null,
        targetTime: Double? = null,
        stableForSteps: Pair<Long, Long>? = null,
        timeTolerance: Double = 0.01,
        positionTolerance: Double? = null,
    ) {
        val terminationMethods = listOfNotNull(
            steps?.let { "steps" },
            targetTime?.let { "targetTime" },
            stableForSteps?.let { "stableForSteps" },
        )
        require(terminationMethods.size == 1) {
            "Exactly one termination method must be provided: steps, targetTime, or stableForSteps. " +
                "Provided: $terminationMethods"
        }

        val effectiveSteps = steps ?: 0L

        println("Running simulations for comparison...")

        val dslSimulation = dslLoader.getDefault<T, P>()
        val yamlSimulation = yamlLoader.getDefault<T, P>()

        println(
            "DSL simulation initial step: ${dslSimulation.step}, " +
                "initial time: ${dslSimulation.time}",
        )
        println(
            "YAML simulation initial step: ${yamlSimulation.step}, " +
                "initial time: ${yamlSimulation.time}",
        )

        addTerminators(dslSimulation, yamlSimulation, steps, targetTime, stableForSteps)

        try {
            runAndCompareSimulations(
                dslSimulation,
                yamlSimulation,
                effectiveSteps,
                targetTime,
                stableForSteps,
                steps,
                timeTolerance,
                positionTolerance,
            )
        } catch (e: Exception) {
            fail("Error during simulation execution: ${e.message}")
        } finally {
            // Ensure simulations are terminated (only if not already terminated)
            if (dslSimulation.status != Status.TERMINATED) {
                dslSimulation.terminate()
            }
            if (yamlSimulation.status != Status.TERMINATED) {
                yamlSimulation.terminate()
            }
        }
    }

    private fun hasTerminationCondition(
        effectiveSteps: Long,
        targetTime: Double?,
        stableForSteps: Pair<Long, Long>?,
    ): Boolean = effectiveSteps > 0 || targetTime != null || stableForSteps != null

    private fun <T, P : Position<P>> addTerminators(
        dslSimulation: Simulation<T, P>,
        yamlSimulation: Simulation<T, P>,
        steps: Long?,
        targetTime: Double?,
        stableForSteps: Pair<Long, Long>?,
    ) {
        when {
            steps != null -> {
                addStepTerminator(dslSimulation, steps)
                addStepTerminator(yamlSimulation, steps)
                println("Added step-based terminators for $steps steps")
            }
            targetTime != null -> {
                val time = DoubleTime(targetTime)
                addTimeTerminator(dslSimulation, time)
                addTimeTerminator(yamlSimulation, time)
                println("Added time-based terminators for ${targetTime}s")
            }
            stableForSteps != null -> {
                val (checkInterval, equalIntervals) = stableForSteps
                addStableTerminator(dslSimulation, checkInterval, equalIntervals)
                addStableTerminator(yamlSimulation, checkInterval, equalIntervals)
                println(
                    "Added stable-for-steps terminators " +
                        "(checkInterval: $checkInterval, equalIntervals: $equalIntervals)",
                )
            }
        }
    }

    private fun <T, P : Position<P>> runAndCompareSimulations(
        dslSimulation: Simulation<T, P>,
        yamlSimulation: Simulation<T, P>,
        effectiveSteps: Long,
        targetTime: Double?,
        stableForSteps: Pair<Long, Long>?,
        steps: Long?,
        timeTolerance: Double,
        positionTolerance: Double?,
    ) {
        println("Running DSL simulation...")
        runSimulationSynchronously(dslSimulation)
        println(
            "DSL simulation completed with status: ${dslSimulation.status}, " +
                "step: ${dslSimulation.step}, time: ${dslSimulation.time}",
        )

        println("Running YAML simulation...")
        runSimulationSynchronously(yamlSimulation)
        println(
            "YAML simulation completed with status: ${yamlSimulation.status}, " +
                "step: ${yamlSimulation.step}, time: ${yamlSimulation.time}",
        )

        checkSimulationTimeAdvancement(dslSimulation, yamlSimulation, effectiveSteps, targetTime, stableForSteps)

        val effectivePositionTolerance = positionTolerance ?: max(timeTolerance * 10, 1e-6)
        compareRuntimeStates(
            dslSimulation,
            yamlSimulation,
            timeTolerance,
            compareSteps = steps != null,
            positionTolerance = effectivePositionTolerance,
        )
    }

    private fun <T, P : Position<P>> checkSimulationTimeAdvancement(
        dslSimulation: Simulation<T, P>,
        yamlSimulation: Simulation<T, P>,
        effectiveSteps: Long,
        targetTime: Double?,
        stableForSteps: Pair<Long, Long>?,
    ) {
        val shouldHaveAdvanced = hasTerminationCondition(effectiveSteps, targetTime, stableForSteps)
        if (dslSimulation.time.toDouble() == 0.0 && shouldHaveAdvanced) {
            println(
                "WARNING: DSL simulation time is 0.0. " +
                    "Ensure all reactions have explicit time distributions.",
            )
        }
        if (yamlSimulation.time.toDouble() == 0.0 && shouldHaveAdvanced) {
            println(
                "WARNING: YAML simulation time is 0.0. " +
                    "Ensure all reactions have explicit time distributions.",
            )
        }
    }

    /**
     * Adds step-based terminator to a simulation.
     */
    private fun <T, P : Position<P>> addStepTerminator(simulation: Simulation<T, P>, steps: Long) {
        simulation.environment.addTerminator(StepCount(steps))
    }

    /**
     * Adds time-based terminator to a simulation.
     */
    private fun <T, P : Position<P>> addTimeTerminator(simulation: Simulation<T, P>, targetTime: Time) {
        simulation.environment.addTerminator(AfterTime(targetTime))
    }

    /**
     * Adds stable-for-steps terminator to a simulation.
     * Terminates when environment (positions + node contents) remains unchanged
     * for checkInterval * equalIntervals steps.
     */
    private fun <T, P : Position<P>> addStableTerminator(
        simulation: Simulation<T, P>,
        checkInterval: Long,
        equalIntervals: Long,
    ) {
        @Suppress("UNCHECKED_CAST")
        val terminator = StableForSteps<Any>(checkInterval, equalIntervals) as
            TerminationPredicate<T, P>
        simulation.environment.addTerminator(terminator)
    }

    /**
     * Runs a simulation synchronously (terminator will stop it).
     */
    private fun <T, P : Position<P>> runSimulationSynchronously(simulation: Simulation<T, P>) {
        println("  Starting simulation thread, initial step: ${simulation.step}, initial time: ${simulation.time}")
        val simulationThread = Thread(simulation, "Simulation-${System.currentTimeMillis()}")
        simulationThread.start()

        while (simulation.status == Status.INIT) {
            Thread.sleep(10)
        }
        println("  Simulation reached status: ${simulation.status}, step: ${simulation.step}, time: ${simulation.time}")

        while (simulation.status != Status.READY && simulation.status != Status.TERMINATED) {
            Thread.sleep(10)
        }
        println(
            "  Simulation status after waiting: ${simulation.status}, " +
                "step: ${simulation.step}, time: ${simulation.time}",
        )

        if (simulation.status == Status.TERMINATED) {
            println("  Simulation already terminated before play()")
            simulation.error.ifPresent { throw it }
            return
        }

        println("  Calling play(), step: ${simulation.step}, time: ${simulation.time}")
        simulation.play().get()
        println("  After play(), status: ${simulation.status}, step: ${simulation.step}, time: ${simulation.time}")

        simulationThread.join()
        println("  Thread joined, final step: ${simulation.step}, final time: ${simulation.time}")

        simulation.error.ifPresent { throw it }
    }

    /**
     * Compares the final states of two simulations after runtime execution.
     */
    private fun <T, P : Position<P>> compareRuntimeStates(
        dslSimulation: Simulation<T, P>,
        yamlSimulation: Simulation<T, P>,
        timeTolerance: Double = 0.01,
        compareSteps: Boolean = true,
        positionTolerance: Double = 1e-6,
    ) {
        println("Comparing runtime simulation states...")

        val dslEnv = dslSimulation.environment
        val yamlEnv = yamlSimulation.environment

        // Compare simulation execution state
        compareSimulationExecutionState(dslSimulation, yamlSimulation, timeTolerance, compareSteps)

        // Compare environment states
        compareRuntimeEnvironmentStates(dslEnv, yamlEnv, positionTolerance)
    }

    /**
     * Compares simulation execution state (time, step, status, errors).
     */
    private fun <T, P : Position<P>> compareSimulationExecutionState(
        dslSimulation: Simulation<T, P>,
        yamlSimulation: Simulation<T, P>,
        timeTolerance: Double = 0.01,
        compareSteps: Boolean = true,
    ) {
        println("Comparing simulation execution state...")

        val dslTime = dslSimulation.time.toDouble()
        val yamlTime = yamlSimulation.time.toDouble()
        val timeDiff = abs(dslTime - yamlTime)

        println("DSL simulation time: ${dslSimulation.time}, step: ${dslSimulation.step}")
        println("YAML simulation time: ${yamlSimulation.time}, step: ${yamlSimulation.step}")
        println("Time difference: ${timeDiff}s (tolerance: ${timeTolerance}s)")

        if (timeDiff > timeTolerance) {
            fail<Nothing>(
                "Simulation times differ by ${timeDiff}s (tolerance: ${timeTolerance}s). " +
                    "DSL: ${dslTime}s, YAML: ${yamlTime}s",
            )
        }

        // Compare step counts (only if using step-based terminator)
        if (compareSteps) {
            assertEquals(
                yamlSimulation.step,
                dslSimulation.step,
                "Simulation step counts should match",
            )
        } else {
            val stepDiff = abs(yamlSimulation.step - dslSimulation.step)
            println("Step difference: $stepDiff (not comparing - using time-based terminator)")
        }

        // Compare status
        assertEquals(
            yamlSimulation.status,
            dslSimulation.status,
            "Simulation status should match",
        )

        // Compare error states
        val dslError = dslSimulation.error
        val yamlError = yamlSimulation.error

        if (dslError.isPresent != yamlError.isPresent) {
            fail<Nothing>(
                "Error states differ: DSL has error=${dslError.isPresent}, YAML has error=${yamlError.isPresent}",
            )
        }

        if (dslError.isPresent && yamlError.isPresent) {
            // Both have errors, compare error messages
            val dslErrorMsg = dslError.get().message ?: "Unknown error"
            val yamlErrorMsg = yamlError.get().message ?: "Unknown error"
            if (dslErrorMsg != yamlErrorMsg) {
                fail<Nothing>("Error messages differ: DSL='$dslErrorMsg', YAML='$yamlErrorMsg'")
            }
        }
    }

    /**
     * Compares environment states after runtime execution.
     */
    private fun <T, P : Position<P>> compareRuntimeEnvironmentStates(
        dslEnv: Environment<T, P>,
        yamlEnv: Environment<T, P>,
        positionTolerance: Double = 1e-6,
    ) {
        println("Comparing runtime environment states...")

        // Compare basic environment properties
        assertEquals(
            yamlEnv.nodeCount,
            dslEnv.nodeCount,
            "Node counts should match after runtime",
        )

        assertEquals(
            yamlEnv.dimensions,
            dslEnv.dimensions,
            "Environment dimensions should match",
        )

        // Compare node positions and contents
        compareRuntimeNodeStates(dslEnv, yamlEnv, positionTolerance)

        // Compare global reactions
        compareRuntimeGlobalReactions(dslEnv, yamlEnv)

        // Compare layers
        compareRuntimeLayers(dslEnv, yamlEnv)
    }

    /**
     * Compares node states after runtime execution using position-based matching with tolerance.
     *
     * @param positionTolerance Maximum distance between positions to consider them matching (default: 1e-6)
     */
    private fun <T, P : Position<P>> compareRuntimeNodeStates(
        dslEnv: Environment<T, P>,
        yamlEnv: Environment<T, P>,
        positionTolerance: Double = 1e-6,
    ) {
        println("Comparing runtime node states... (position tolerance: $positionTolerance)")

        val dslNodesWithPos = dslEnv.nodes.map { it to dslEnv.getPosition(it) }
        val yamlNodesWithPos = yamlEnv.nodes.map { it to yamlEnv.getPosition(it) }.toMutableList()

        val (matchedPairs, unmatchedDslNodes, distances) = matchNodesByPosition(
            dslNodesWithPos,
            yamlNodesWithPos,
            positionTolerance,
        )

        printMatchingStatistics(distances, matchedPairs, dslNodesWithPos.size)
        checkUnmatchedNodes(unmatchedDslNodes, yamlNodesWithPos, distances, positionTolerance)
        compareMatchedNodes(matchedPairs, dslEnv, yamlEnv, positionTolerance)
    }

    private fun <T, P : Position<P>> matchNodesByPosition(
        dslNodesWithPos: List<Pair<Node<T>, P>>,
        yamlNodesWithPos: MutableList<Pair<Node<T>, P>>,
        positionTolerance: Double,
    ): Triple<List<Pair<Node<T>, Node<T>>>, List<Pair<Node<T>, P>>, List<Double>> {
        val matchedPairs = mutableListOf<Pair<Node<T>, Node<T>>>()
        val unmatchedDslNodes = mutableListOf<Pair<Node<T>, P>>()
        val distances = mutableListOf<Double>()

        for ((dslNode, dslPos) in dslNodesWithPos) {
            val closest = yamlNodesWithPos.minByOrNull { (_, yamlPos) ->
                dslPos.distanceTo(yamlPos)
            }
            if (closest != null) {
                val (yamlNode, yamlPos) = closest
                val distance = dslPos.distanceTo(yamlPos)
                distances.add(distance)
                if (distance <= positionTolerance) {
                    matchedPairs.add(dslNode to yamlNode)
                    yamlNodesWithPos.remove(closest)
                } else {
                    unmatchedDslNodes.add(dslNode to dslPos)
                }
            } else {
                unmatchedDslNodes.add(dslNode to dslPos)
            }
        }

        return Triple(matchedPairs, unmatchedDslNodes, distances)
    }

    private fun <T> printMatchingStatistics(
        distances: List<Double>,
        matchedPairs: List<Pair<Node<T>, Node<T>>>,
        totalNodes: Int,
    ) {
        if (distances.isNotEmpty()) {
            val minDistance = distances.minOrNull() ?: Double.MAX_VALUE
            val maxDistance = distances.maxOrNull() ?: 0.0
            val avgDistance = distances.average()
            println(
                "Position matching statistics: min=$minDistance, max=$maxDistance, " +
                    "avg=$avgDistance, matched=${matchedPairs.size}/$totalNodes",
            )
        }
    }

    private fun <T, P : Position<P>> checkUnmatchedNodes(
        unmatchedDslNodes: List<Pair<Node<T>, P>>,
        yamlNodesWithPos: List<Pair<Node<T>, P>>,
        distances: List<Double>,
        positionTolerance: Double,
    ) {
        if (unmatchedDslNodes.isNotEmpty()) {
            val minDistance = distances.minOrNull() ?: Double.MAX_VALUE
            val maxDistance = distances.maxOrNull() ?: 0.0
            val avgDistance = distances.average()
            val positions = unmatchedDslNodes.take(10).joinToString(", ") { (_, pos) -> pos.toString() }
            val moreInfo = if (unmatchedDslNodes.size > 10) {
                " ... and ${unmatchedDslNodes.size - 10} more"
            } else {
                ""
            }
            fail<Nothing>(
                "DSL simulation has ${unmatchedDslNodes.size} unmatched nodes " +
                    "(tolerance: $positionTolerance). Distance stats: min=$minDistance, " +
                    "max=$maxDistance, avg=$avgDistance. First 10 positions: $positions$moreInfo",
            )
        }
        if (yamlNodesWithPos.isNotEmpty()) {
            val positions = yamlNodesWithPos.take(10).joinToString(", ") { (_, pos) -> pos.toString() }
            val moreInfo = if (yamlNodesWithPos.size > 10) {
                " ... and ${yamlNodesWithPos.size - 10} more"
            } else {
                ""
            }
            fail<Nothing>(
                "YAML simulation has ${yamlNodesWithPos.size} unmatched nodes " +
                    "at positions: $positions$moreInfo",
            )
        }
    }

    private fun <T, P : Position<P>> compareMatchedNodes(
        matchedPairs: List<Pair<Node<T>, Node<T>>>,
        dslEnv: Environment<T, P>,
        yamlEnv: Environment<T, P>,
        positionTolerance: Double,
    ) {
        for ((dslNode, yamlNode) in matchedPairs) {
            val dslPos = dslEnv.getPosition(dslNode)
            val yamlPos = yamlEnv.getPosition(yamlNode)
            val distance = dslPos.distanceTo(yamlPos)
            if (distance > positionTolerance) {
                println(
                    "WARNING: Matched nodes have distance $distance " +
                        "(tolerance: $positionTolerance)",
                )
            }
            compareNodeContentsAtPosition(dslNode, yamlNode, dslPos)
        }
    }

    /**
     * Compares contents of two nodes at the same position.
     */
    private fun <T> compareNodeContentsAtPosition(dslNode: Node<T>, yamlNode: Node<T>, position: Any) {
        // Compare molecule counts
        assertEquals(
            yamlNode.moleculeCount,
            dslNode.moleculeCount,
            "Molecule counts should match at position $position",
        )

        // Compare all molecule concentrations
        val dslContents = dslNode.contents
        val yamlContents = yamlNode.contents

        // Get all unique molecule names
        val allMolecules = (dslContents.keys + yamlContents.keys).distinct()

        for (molecule in allMolecules) {
            val dslConcentration = dslContents[molecule]
            val yamlConcentration = yamlContents[molecule]

            when {
                dslConcentration == null && yamlConcentration == null -> {
                    // Both null, continue
                }
                dslConcentration == null -> {
                    fail<Nothing>("DSL node missing molecule $molecule at position $position")
                }
                yamlConcentration == null -> {
                    fail<Nothing>("YAML node missing molecule $molecule at position $position")
                }
                else -> {
                    // Both concentrations exist, compare them exactly
                    assertEquals(
                        yamlConcentration,
                        dslConcentration,
                        "Concentration of molecule $molecule should match at position $position",
                    )
                }
            }
        }

        // Compare reaction counts
        assertEquals(
            yamlNode.reactions.size,
            dslNode.reactions.size,
            "Reaction counts should match at position $position",
        )
    }

    /**
     * Compares global reactions after runtime execution.
     */
    private fun <T, P : Position<P>> compareRuntimeGlobalReactions(
        dslEnv: Environment<T, P>,
        yamlEnv: Environment<T, P>,
    ) {
        println("Comparing runtime global reactions...")

        assertEquals(
            yamlEnv.globalReactions.size,
            dslEnv.globalReactions.size,
            "Global reaction counts should match after runtime",
        )

        // Compare global reaction types
        val dslGlobalTypes = dslEnv.globalReactions.map { it::class }.sortedBy { it.simpleName }
        val yamlGlobalTypes = yamlEnv.globalReactions.map { it::class }.sortedBy { it.simpleName }

        assertEquals(
            yamlGlobalTypes,
            dslGlobalTypes,
            "Global reaction types should match after runtime",
        )
    }

    /**
     * Compares layers after runtime execution.
     */
    private fun <T, P : Position<P>> compareRuntimeLayers(dslEnv: Environment<T, P>, yamlEnv: Environment<T, P>) {
        println("Comparing runtime layers...")

        assertEquals(
            yamlEnv.layers.size,
            dslEnv.layers.size,
            "Layer counts should match after runtime",
        )

        // Compare layer types
        val dslLayerTypes = dslEnv.layers.map { it::class }.sortedBy { it.simpleName }
        val yamlLayerTypes = yamlEnv.layers.map { it::class }.sortedBy { it.simpleName }

        assertEquals(
            yamlLayerTypes,
            dslLayerTypes,
            "Layer types should match after runtime",
        )

        LayerComparisonUtils.compareLayerValues(dslEnv, yamlEnv)
    }
}
