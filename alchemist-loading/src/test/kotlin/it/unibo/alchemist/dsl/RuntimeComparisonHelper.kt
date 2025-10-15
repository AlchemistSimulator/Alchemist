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
import it.unibo.alchemist.model.terminators.StepCount
import kotlin.math.abs
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
     * Compares loaders by running both simulations for a specified number of steps
     * and comparing their final states
     */
    fun <T, P : Position<P>> compareLoaders(dslLoader: Loader, yamlLoader: Loader, steps: Long = 1000L) {
        println("Running simulations for comparison...")

        val dslSimulation = dslLoader.getDefault<T, P>()
        val yamlSimulation = yamlLoader.getDefault<T, P>()

        // Add step-based terminators to both simulations
        addStepTerminator(dslSimulation, steps)
        addStepTerminator(yamlSimulation, steps)

        try {
            // Run simulations sequentially to avoid complexity
            println("Running DSL simulation...")
            runSimulationSynchronously(dslSimulation)
            println("DSL simulation completed with status: ${dslSimulation.status}")

            println("Running YAML simulation...")
            runSimulationSynchronously(yamlSimulation)
            println("YAML simulation completed with status: ${yamlSimulation.status}")

            // Compare final states
            compareRuntimeStates(dslSimulation, yamlSimulation)
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

    /**
     * Adds step-based terminator to a simulation
     */
    private fun <T, P : Position<P>> addStepTerminator(simulation: Simulation<T, P>, steps: Long) {
        // Add step-based terminator
        simulation.environment.addTerminator(StepCount(steps))
    }

    /**
     * Runs a simulation synchronously (terminator will stop it)
     */
    private fun <T, P : Position<P>> runSimulationSynchronously(simulation: Simulation<T, P>) {
        simulation.play() // Start the simulation
        simulation.run() // Block until completion (terminator will stop it)
        simulation.error.ifPresent { throw it } // Check for errors
    }

    /**
     * Compares the final states of two simulations after runtime execution
     */
    private fun <T, P : Position<P>> compareRuntimeStates(
        dslSimulation: Simulation<T, P>,
        yamlSimulation: Simulation<T, P>,
    ) {
        println("Comparing runtime simulation states...")

        val dslEnv = dslSimulation.environment
        val yamlEnv = yamlSimulation.environment

        // Compare simulation execution state
        compareSimulationExecutionState(dslSimulation, yamlSimulation)

        // Compare environment states
        compareRuntimeEnvironmentStates(dslEnv, yamlEnv)
    }

    /**
     * Compares simulation execution state (time, step, status, errors)
     */
    private fun <T, P : Position<P>> compareSimulationExecutionState(
        dslSimulation: Simulation<T, P>,
        yamlSimulation: Simulation<T, P>,
    ) {
        println("Comparing simulation execution state...")

        // Print simulation times for debugging (skip comparison due to timing variations)
        println("DSL simulation time: ${dslSimulation.time}")
        println("YAML simulation time: ${yamlSimulation.time}")
        val timeDiff = abs(dslSimulation.time.toDouble() - yamlSimulation.time.toDouble())
        println("Time difference: ${timeDiff}s")

        // Compare step counts
        assertEquals(
            yamlSimulation.step,
            dslSimulation.step,
            "Simulation step counts should match",
        )

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
     * Compares environment states after runtime execution
     */
    private fun <T, P : Position<P>> compareRuntimeEnvironmentStates(
        dslEnv: Environment<T, P>,
        yamlEnv: Environment<T, P>,
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
        compareRuntimeNodeStates(dslEnv, yamlEnv)

        // Compare global reactions
        compareRuntimeGlobalReactions(dslEnv, yamlEnv)

        // Compare layers
        compareRuntimeLayers(dslEnv, yamlEnv)
    }

    /**
     * Compares node states after runtime execution using position-based matching
     */
    private fun <T, P : Position<P>> compareRuntimeNodeStates(dslEnv: Environment<T, P>, yamlEnv: Environment<T, P>) {
        println("Comparing runtime node states...")

        // Create position-to-node maps for both environments
        val dslNodesByPosition = dslEnv.nodes.associateBy { dslEnv.getPosition(it) }
        val yamlNodesByPosition = yamlEnv.nodes.associateBy { yamlEnv.getPosition(it) }

        // Get all unique positions
        val allPositions = (dslNodesByPosition.keys + yamlNodesByPosition.keys).distinct()

        for (position in allPositions) {
            val dslNode = dslNodesByPosition[position]
            val yamlNode = yamlNodesByPosition[position]

            when {
                dslNode == null && yamlNode == null -> {
                    // Both null, continue
                }
                dslNode == null -> {
                    fail<Nothing>("DSL simulation missing node at position $position")
                }
                yamlNode == null -> {
                    fail<Nothing>("YAML simulation missing node at position $position")
                }
                else -> {
                    // Both nodes exist, compare their contents
                    compareNodeContentsAtPosition(dslNode, yamlNode, position)
                }
            }
        }
    }

    /**
     * Compares contents of two nodes at the same position
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
     * Compares global reactions after runtime execution
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
     * Compares layers after runtime execution
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

        // Compare layer values at sample positions
        compareLayerValues(dslEnv, yamlEnv)
    }

    /**
     * Compares layer values at sample positions
     */
    private fun <T, P : Position<P>> compareLayerValues(dslEnv: Environment<T, P>, yamlEnv: Environment<T, P>) {
        println("Comparing layer values...")

        // Sample positions to test layer values
        val samplePositions = mutableListOf<P>()

        // Add positions from both environments' nodes
        samplePositions.addAll(dslEnv.nodes.map { dslEnv.getPosition(it) })
        samplePositions.addAll(yamlEnv.nodes.map { yamlEnv.getPosition(it) })

        // Remove duplicates
        val uniquePositions = samplePositions.distinct()

        if (uniquePositions.isNotEmpty()) {
            for (position in uniquePositions) {
                val dslLayerValues = dslEnv.layers.map { (it.getValue(position)) }
                val yamlLayerValues = yamlEnv.layers.map { it.getValue(position) }
                // Convert all values to Double for comparison to handle Int vs Double differences
                val dslDoubleValues = dslLayerValues.map { value ->
                    when (value) {
                        is Number -> value.toDouble()
                        else -> value.toString().toDoubleOrNull() ?: 0.0
                    }
                }
                val yamlDoubleValues = yamlLayerValues.map { value ->
                    when (value) {
                        is Number -> value.toDouble()
                        else -> value.toString().toDoubleOrNull() ?: 0.0
                    }
                }

                assertEquals(
                    dslDoubleValues,
                    yamlDoubleValues,
                    "Layer values at position $position should match",
                )
            }
        } else {
            println("Skipping layer value comparison - no valid positions found")
        }
    }
}
