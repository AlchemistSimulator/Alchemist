/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.conditions

import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.conditions.AbstractCondition
import it.unibo.alchemist.model.observation.Observable

/** A condition requiring at least [requiredQuantity] units of [molecule] in its node. */
open class GenericMoleculePresent<T : Number>(
    node: Node<T>,
    private val molecule: Molecule,
    /** Required quantity configured for this condition. */
    val requiredQuantity: T,
) : AbstractCondition<T>(node) {

    private val nodeQuantity: Observable<Double> = node.observeConcentration(molecule).map { concentration ->
        concentration.fold(ifEmpty = { 0.0 }, ifSome = Number::toDouble)
    }

    init {
        require(requiredQuantity.toDouble() > 0.0) { "The quantity of compound must be positive." }
        setValidity(nodeQuantity.map { it >= requiredQuantity.toDouble() })
    }

    override fun cloneCondition(newNode: Node<T>, newReaction: NodeReaction<T>): GenericMoleculePresent<T> =
        GenericMoleculePresent(newNode, molecule, requiredQuantity)

    /** Observable quantity consumed by the owning biochemical reaction's mass-action law. */
    open val quantity: Observable<Double> get() = nodeQuantity

    /** Molecule whose quantity this condition observes. */
    fun getMolecule(): Molecule = molecule

    override fun toString(): String = "$molecule>=$requiredQuantity"
}
