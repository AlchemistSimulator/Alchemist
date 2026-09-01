/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.conditions

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.Node.Companion.asPropertyOrNull
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.biochemistry.CircularCellProperty
import it.unibo.alchemist.model.biochemistry.CircularDeformableCellProperty
import it.unibo.alchemist.model.biochemistry.EnvironmentSupportingDeformableCells
import it.unibo.alchemist.model.conditions.AbstractCondition
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.observation.ObservableExtensions.ObservableSetExtensions.combineLatest

/** A condition requiring mechanical tension from at least one nearby circular cell. */
class TensionPresent(private val environment: EnvironmentSupportingDeformableCells<*>, node: Node<Double>) :
    AbstractCondition<Double>(node) {

    private val mechanics: Observable<MechanicalState> = environment
        .observeNodesWithinRange(node, environment.maxDiameterAmongCircularDeformableCells)
        .combineLatest(environment::getPosition) { computeMechanicalState() }

    init {
        requireNotNull(node.asPropertyOrNull<Double, CircularDeformableCellProperty>()) {
            "Node must have a ${CircularDeformableCellProperty::class.simpleName}"
        }
        addObservableDependency(mechanics)
        setValidity(mechanics.map(MechanicalState::valid))
    }

    override fun cloneCondition(newNode: Node<Double>, newReaction: NodeReaction<Double>): TensionPresent {
        requireNotNull(newNode.asPropertyOrNull<Double, CircularDeformableCellProperty>()) {
            "Node must have a ${CircularDeformableCellProperty::class.simpleName}"
        }
        return TensionPresent(environment, newNode)
    }

    /** Current tension factor consumed by [it.unibo.alchemist.model.biochemistry.reactions.BiochemicalNodeReaction]. */
    fun getTension(): Double = mechanics.current.tension

    private fun computeMechanicalState(): MechanicalState {
        val thisNode = getNode()
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

    private data class MechanicalState(val valid: Boolean, val tension: Double)
}
