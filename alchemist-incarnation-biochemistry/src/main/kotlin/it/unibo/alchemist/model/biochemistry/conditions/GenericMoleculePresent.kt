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

/** A condition requiring at least [quantity] units of [molecule] in its node. */
open class GenericMoleculePresent<T : Number>(node: Node<T>, private val molecule: Molecule, private val quantity: T) :
    AbstractCondition<T>(node) {

    private val observedQuantity: Observable<Double> = node.observeConcentration(molecule).map { concentration ->
        concentration.fold(ifEmpty = { 0.0 }, ifSome = Number::toDouble)
    }

    init {
        require(quantity.toDouble() > 0.0) { "The quantity of compound must be positive." }
        addObservableDependency(observedQuantity)
        setValidity(observedQuantity.map { it >= quantity.toDouble() })
    }

    override fun cloneCondition(newNode: Node<T>, newReaction: NodeReaction<T>): GenericMoleculePresent<T> =
        GenericMoleculePresent(newNode, molecule, quantity)

    /** Current amount read by the owning biochemical reaction's mass-action law. */
    open fun getCurrentQuantity(): Double = observedQuantity.current

    /** Required quantity configured for this condition. */
    fun getQuantity(): T = quantity

    /** Molecule whose quantity this condition observes. */
    fun getMolecule(): Molecule = molecule

    override fun toString(): String = "$molecule>=$quantity"
}
