/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.actions

import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.VisibleNode
import it.unibo.alchemist.model.physics.environments.Physics2DEnvironment
import it.unibo.alchemist.model.positions.Euclidean2DPosition

/**
 * Given a list of [VisibleNode] associated to [visionMolecule],
 * it finds the closest to a point located at [distance] from the node owning this action
 * in the direction of its heading,
 * and injects its position in [targetMolecule].
 *
 * If there are no [VisibleNode]s, [targetMolecule] will be removed from the node.
 */
class CameraInjectVisibleNodeClosestToDistance(
    reaction: NodeReaction<Any>,
    private val environment: Physics2DEnvironment<Any>,
    private val distance: Double,
    private val visionMolecule: Molecule,
    private val targetMolecule: Molecule,
) : AbstractLocalAction<Any>(reaction) {
    override fun cloneOnNodeReaction(newReaction: NodeReaction<Any>) =
        CameraInjectVisibleNodeClosestToDistance(newReaction, environment, distance, visionMolecule, targetMolecule)

    override fun execute() {
        if (targetNode.contains(visionMolecule)) {
            val visibleNodes = targetNode.getConcentration(visionMolecule)
            require(visibleNodes is List<*>) { "visionMolecule contains ${visibleNodes::class} instead of a List" }
            if (visibleNodes.isEmpty()) {
                if (targetNode.contains(targetMolecule)) targetNode.removeConcentration(targetMolecule)
            } else {
                val aNode = visibleNodes.first()
                require(aNode is VisibleNode<*, *>) {
                    "visionMolecule contains List<${aNode?.javaClass}> instead of a List<VisibleNode>"
                }
                require(aNode.position is Euclidean2DPosition) {
                    "The VisibleNode contained in visionMolecule is from a different environment"
                }
                @Suppress("UNCHECKED_CAST")
                val nodes = visibleNodes as List<VisibleNode<*, Euclidean2DPosition>>
                val myPosition = environment.getCurrentPosition(targetNode).surroundingPointAt(
                    versor = environment.getHeading(targetNode),
                    distance = distance,
                )
                nodes.map { it.position }
                    .reduce { n1, n2 -> minOf(n1, n2, compareBy { it.distanceTo(myPosition) }) }
                    .also { targetNode.setConcentration(targetMolecule, it) }
            }
        }
    }
}
