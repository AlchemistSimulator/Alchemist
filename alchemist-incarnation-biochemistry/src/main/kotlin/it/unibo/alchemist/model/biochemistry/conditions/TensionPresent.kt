/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.conditions

import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.Node.Companion.asPropertyOrNull
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.biochemistry.CircularCellProperty
import it.unibo.alchemist.model.biochemistry.CircularDeformableCellProperty
import it.unibo.alchemist.model.biochemistry.EnvironmentSupportingDeformableCells
import it.unibo.alchemist.model.conditions.AbstractLocalCondition
import it.unibo.alchemist.model.observables.util.ObservableSets.combineLatest
import it.unibo.alchemist.model.observation.Observable

/** A condition requiring mechanical tension from at least one nearby circular cell. */
class TensionPresent(private val environment: EnvironmentSupportingDeformableCells<*>, reaction: NodeReaction<Double>) :
    AbstractLocalCondition<Double>(reaction) {

    private val mechanics: Observable<MechanicalState> = environment
        .observeNodesWithinRange(targetNode, environment.maxDiameterAmongCircularDeformableCells)
        .combineLatest(environment::getPosition) { computeMechanicalState() }

    init {
        requireNotNull(targetNode.asPropertyOrNull<Double, CircularDeformableCellProperty>()) {
            "Node must have a ${CircularDeformableCellProperty::class.simpleName}"
        }
        setValidity(mechanics.map(MechanicalState::valid))
    }

    override fun cloneOnNodeReaction(newReaction: NodeReaction<Double>): TensionPresent =
        TensionPresent(environment, newReaction)

    /** Current tension factor consumed by [it.unibo.alchemist.model.biochemistry.reactions.BiochemicalNodeReaction]. */
    fun getTension(): Double = mechanics.current.tension

    /** Observable mechanical state consumed by the owning biochemical reaction. */
    fun observeMechanicalState(): Observable<MechanicalState> = mechanics

    private fun computeMechanicalState(): MechanicalState {
        val thisNode = targetNode
        val thisCell = thisNode.asProperty<Double, CircularDeformableCellProperty>()
        var valid = false
        var totalTension = 0.0
        environment
            .getNodesWithinRange(thisNode, environment.maxDiameterAmongCircularDeformableCells)
            .mapNotNull { neighbor ->
                neighbor.asPropertyOrNull<Double, CircularCellProperty>()?.let { neighbor to it }
            }.forEach { (neighbor, neighborCell) ->
                val distance = environment.getDistanceBetweenNodes(neighbor, thisNode)
                val neighborMaximumRadius =
                    (neighborCell as? CircularDeformableCellProperty)?.maximumRadius ?: neighborCell.radius
                val maximumRadiusSum = thisCell.maximumRadius + neighborMaximumRadius
                valid = valid || distance < maximumRadiusSum
                val currentRadiusSum = thisCell.radius + neighborCell.radius
                totalTension += when {
                    maximumRadiusSum < distance -> 0.0
                    maximumRadiusSum == currentRadiusSum -> 1.0
                    else -> (maximumRadiusSum - distance) / (maximumRadiusSum - currentRadiusSum)
                }
            }
        return MechanicalState(valid, totalTension)
    }

    /** Mechanical validity and tension computed from nearby cells. */
    data class MechanicalState(
        /** Whether the mechanical condition is valid. */
        val valid: Boolean,
        /** Aggregate tension applied by nearby cells. */
        val tension: Double,
    )
}
