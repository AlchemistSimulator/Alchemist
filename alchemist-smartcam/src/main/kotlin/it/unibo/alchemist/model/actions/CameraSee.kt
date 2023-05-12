/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.actions

import it.unibo.alchemist.model.Context
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.model.nodes.VisibleNodeImpl
import it.unibo.alchemist.model.physics.FieldOfView2D
import it.unibo.alchemist.model.physics.environments.Physics2DEnvironment
import java.lang.Math.toRadians

/**
 * Checks nodes in the [environment] and writes in [outputMolecule]
 * the list of [it.unibo.alchemist.model.VisibleNode],
 * containing [filterByMolecule].
 * [distance] and [angle] define the field of view.
 */
class CameraSee @JvmOverloads constructor(
    node: Node<Any>,
    private val environment: Physics2DEnvironment<Any>,
    /**
     * Distance of the field of view.
     */
    val distance: Double,
    /**
     * Angle in degrees of the field of view.
     */
    val angle: Double,
    private val outputMolecule: Molecule = SimpleMolecule("vision"),
    private val filterByMolecule: Molecule? = null,
) : AbstractAction<Any>(node) {

    private val fieldOfView =
        FieldOfView2D(
            environment,
            node,
            distance,
            toRadians(angle),
        )

    init {
        node.setConcentration(outputMolecule, emptyList<Any>())
    }

    override fun cloneAction(node: Node<Any>, reaction: Reaction<Any>) =
        CameraSee(node, environment, distance, angle, outputMolecule, filterByMolecule)

    override fun execute() {
        var seen = fieldOfView.influentialNodes()
        filterByMolecule?.run {
            seen = seen.filter { it.contains(filterByMolecule) }
        }
        node.setConcentration(outputMolecule, seen.map { VisibleNodeImpl(it, environment.getPosition(it)) })
    }

    override fun getContext() = Context.LOCAL
}
