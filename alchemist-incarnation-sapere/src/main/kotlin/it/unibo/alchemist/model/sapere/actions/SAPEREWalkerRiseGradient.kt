/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.sapere.actions

import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.maps.MapEnvironment
import it.unibo.alchemist.model.maps.actions.MoveOnMap
import it.unibo.alchemist.model.maps.movestrategies.routing.OnStreets
import it.unibo.alchemist.model.maps.routingservices.GraphHopperOptions
import it.unibo.alchemist.model.maps.routingservices.GraphHopperRoutingService
import it.unibo.alchemist.model.movestrategies.TargetSelectionStrategy
import it.unibo.alchemist.model.movestrategies.speed.InteractWithOthers
import it.unibo.alchemist.model.sapere.ILsaMolecule

/**
 * Walks on the streets following the gradient described by [templateLSA], whose argument in position [neighPos]
 * contains the next hop, slowing down by the [interaction] factor near the nodes tagged with [tag] within [range].
 * The [reaction] is used to compute the movement length, the node walks at an average [speed].
 */
open class SAPEREWalkerRiseGradient(
    environment: MapEnvironment<List<ILsaMolecule>, GraphHopperOptions, GraphHopperRoutingService>,
    reaction: NodeReaction<List<ILsaMolecule>>,
    tag: Molecule,
    speed: Double,
    interaction: Double,
    range: Double,
    templateLSA: Molecule,
    neighPos: Int,
) : MoveOnMap<List<ILsaMolecule>, GraphHopperOptions, GraphHopperRoutingService>(
    environment,
    reaction,
    OnStreets(environment, GraphHopperRoutingService.defaultOptions),
    InteractWithOthers(environment, reaction.host, reaction, tag, speed, range, interaction),
    NextTargetStrategy(environment, reaction.host, templateLSA, neighPos),
) {
    /**
     * Builds a walker interacting with the nodes tagged with [SAPEREWalker.DEFAULT_INTERACTING_TAG].
     */
    constructor(
        environment: MapEnvironment<List<ILsaMolecule>, GraphHopperOptions, GraphHopperRoutingService>,
        reaction: NodeReaction<List<ILsaMolecule>>,
        speed: Double,
        interaction: Double,
        range: Double,
        templateLSA: Molecule,
        neighPos: Int,
    ) : this(
        environment,
        reaction,
        SAPEREWalker.DEFAULT_INTERACTING_TAG,
        speed,
        interaction,
        range,
        templateLSA,
        neighPos,
    )

    private class NextTargetStrategy(
        private val environment: MapEnvironment<List<ILsaMolecule>, GraphHopperOptions, GraphHopperRoutingService>,
        private val node: Node<List<ILsaMolecule>>,
        pattern: Molecule,
        private val argPos: Int,
    ) : TargetSelectionStrategy<List<ILsaMolecule>, GeoPosition> {
        private val template: ILsaMolecule =
            pattern as? ILsaMolecule ?: throw IllegalArgumentException("$pattern is not a valid SAPERE LSA")
        private var currentNode: Node<List<ILsaMolecule>> = node
        private var currentPosition: GeoPosition? = null

        override fun getTarget(): GeoPosition {
            val matches = node.getConcentration(template)
            val position = environment.getCurrentPosition(node)
            // If there is no gradient and there is no goal, or the goal has already been reached, then remain still.
            if (matches.isEmpty()) {
                return currentPosition?.takeUnless { it == position } ?: position
            }
            val nextHop = (matches[0].getArg(argPos).rootNodeData as Double).toInt()
            // If the current target node has moved, the destination should be re-computed.
            val currentNodePosition = environment.getCurrentPosition(currentNode)
            if (currentNode == node ||
                currentPosition != currentNodePosition ||
                environment.getNeighborhood(node).current.contains(currentNode)
            ) {
                currentNode = environment.getNodeByID(nextHop)
                currentPosition = environment.getCurrentPosition(currentNode)
            }
            return checkNotNull(currentPosition)
        }
    }
}
