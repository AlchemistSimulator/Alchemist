/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.sapere.actions

import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.maps.MapEnvironment
import it.unibo.alchemist.model.maps.actions.MoveOnMap
import it.unibo.alchemist.model.maps.movestrategies.routing.OnStreets
import it.unibo.alchemist.model.maps.movestrategies.target.FollowTrace
import it.unibo.alchemist.model.maps.routingservices.GraphHopperOptions
import it.unibo.alchemist.model.maps.routingservices.GraphHopperRoutingService
import it.unibo.alchemist.model.movestrategies.speed.InteractWithOthers
import it.unibo.alchemist.model.sapere.ILsaAction
import it.unibo.alchemist.model.sapere.ILsaMolecule
import it.unibo.alchemist.model.sapere.ILsaNode
import it.unibo.alchemist.model.sapere.dsl.ITreeNode
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule
import org.danilopianini.lang.HashString

/**
 * Follows a GPS trace on the streets, slowing down by the [interaction] factor near the nodes tagged with [tag]
 * within [range]. The [reaction] is used to compute the movement length, the node walks at an average [speed].
 */
class SAPEREWalker(
    environment: MapEnvironment<List<ILsaMolecule>, GraphHopperOptions, GraphHopperRoutingService>,
    reaction: NodeReaction<List<ILsaMolecule>>,
    private val tag: ILsaMolecule,
    private val speed: Double,
    private val interaction: Double,
    private val range: Double,
) : MoveOnMap<List<ILsaMolecule>, GraphHopperOptions, GraphHopperRoutingService>(
    environment,
    reaction,
    OnStreets(environment, GraphHopperOptions.defaultOptions),
    InteractWithOthers(environment, reaction.host, reaction, tag, speed, range, interaction),
    FollowTrace(reaction),
),
    ILsaAction {
    /**
     * Builds a walker interacting with the nodes tagged with [DEFAULT_INTERACTING_TAG].
     */
    constructor(
        environment: MapEnvironment<List<ILsaMolecule>, GraphHopperOptions, GraphHopperRoutingService>,
        reaction: NodeReaction<List<ILsaMolecule>>,
        speed: Double,
        interaction: Double,
        range: Double,
    ) : this(environment, reaction, DEFAULT_INTERACTING_TAG, speed, interaction, range)

    /**
     * The node hosting this action, as an [ILsaNode].
     */
    val lsaNode: ILsaNode get() = targetNode as ILsaNode

    override fun cloneOnNodeReaction(newReaction: NodeReaction<List<ILsaMolecule>>): SAPEREWalker =
        SAPEREWalker(environment, newReaction, tag, speed, interaction, range)

    override fun setExecutionContext(matches: MutableMap<HashString, ITreeNode<*>>?, nodes: MutableList<ILsaNode>?) =
        Unit

    /**
     * Constants.
     */
    companion object {
        /**
         * The default molecule that identifies an interacting object.
         */
        @JvmField
        val DEFAULT_INTERACTING_TAG: ILsaMolecule = LsaMolecule("person")
    }
}
