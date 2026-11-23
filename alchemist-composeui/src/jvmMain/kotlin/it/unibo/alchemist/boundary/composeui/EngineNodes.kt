/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui

import it.unibo.alchemist.boundary.composeui.model.callbacks.AlchemistUiCallbacks
import it.unibo.alchemist.boundary.composeui.model.drag.NodePositionUpdate
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position

/**
 * Changes the nodes of the simulation of [bridge], between two steps.
 * Molecules and values are parsed by the incarnation, as in simulation files.
 * If a move, a copy, a deletion or a molecule removal fails, the user gets a dialog;
 * a value that fails is returned to the caller.
 */
internal class EngineNodes<T, P : Position<P>>(private val bridge: EngineBridge<T, P>) : AlchemistUiCallbacks.Nodes {
    override suspend fun move(nodePositions: List<NodePositionUpdate>) = change {
        nodePositions.forEach { (nodeId, coordinates) ->
            moveNodeTo(nodeWithId(nodeId), makePosition(coordinates))
        }
    }

    override suspend fun clone(copies: List<NodePositionUpdate>) = change {
        copies.forEach { (nodeId, coordinates) ->
            check(addNode(nodeWithId(nodeId).cloneNode(), makePosition(coordinates))) {
                "The copy of node $nodeId cannot be placed at $coordinates"
            }
        }
    }

    override suspend fun delete(nodeIds: List<Int>) = change {
        nodeIds.map(::nodeWithId).forEach(::removeNode)
    }

    override suspend fun setConcentration(nodeIds: List<Int>, molecule: String, value: String) = bridge.ifActive {
        apply {
            val parsedMolecule = incarnation.createMolecule(molecule)
            // Parsed once per node, as values may be mutable.
            nodeIds.forEach { nodeWithId(it)[parsedMolecule] = incarnation.createConcentration(value) }
        }
    }

    override suspend fun removeMolecule(nodeIds: List<Int>, molecule: String) = change {
        val parsedMolecule = incarnation.createMolecule(molecule)
        nodeIds.map(::nodeWithId)
            .filter { parsedMolecule in it }
            .forEach { it.removeAllConcentrationOf(parsedMolecule) }
    }

    private suspend fun apply(action: Environment<T, P>.() -> Unit): Result<Unit> =
        bridge.onSimulationThread { bridge.simulation.environment.action() }
            .onSuccess { bridge.syncSimulationState(refreshScene = true) }

    private suspend fun change(action: Environment<T, P>.() -> Unit) = bridge.whileActive {
        apply(action).onFailure {
            bridge.showDialog("Invalid edit", it.message ?: "The simulation refused the change: $it")
        }
    }
}
