/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.kotlindsl

import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node

/**
 * DSL utilities for defining the initial contents of a [Node].
 *
 * This context provides helpers to:
 * - build concentrations using the current [Incarnation];
 * - assign concentrations to a [Node] using the unary minus operator.
 *
 * The API is based on Kotlin context receivers: the required [Incarnation] and/or [Node] are expected to be
 * available in the current context.
 */
// TODO: Detekt false positive. Remove once Detekt supports context parameters.
@Suppress("UndocumentedPublicFunction")
object ContentContext {

    /**
     * Creates a concentration value using the current [Incarnation].
     *
     * This is a convenience wrapper around [Incarnation.createConcentration] to keep DSL code concise.
     *
     * @param origin an optional value used by the [Incarnation] to derive the concentration.
     * @return a concentration instance of type [T].
     */
    context(incarnation: Incarnation<T, *>)
    fun <T> concentrationOf(origin: Any?): T = incarnation.createConcentration(origin)

    /**
     * Assigns a concentration to the current [Node] for the given [Molecule].
     *
     * This operator enables concise DSL statements by interpreting `-(molecule to concentration)` as
     * "set the concentration of this molecule on the current node".
     *
     * @receiver a pair `(molecule, concentration)` to assign.
     */
    context(node: Node<T>)
    operator fun <T> Pair<Molecule, T>.unaryMinus() {
        node.setConcentration(first, second)
    }

    /**
     * Assigns a default concentration for this [Molecule] to the current [Node].
     *
     * The concentration is created via [Incarnation.createConcentration] with no explicit origin.
     *
     * @receiver the molecule whose concentration should be set.
     */
    context(incarnation: Incarnation<T, *>, _: Node<T>)
    operator fun <T> Molecule.unaryMinus() = -Pair(this, incarnation.createConcentration())

    /**
     * Assigns a default concentration for the molecule identified by this string to the current [Node].
     *
     * The [Molecule] is created via [Incarnation.createMolecule], while the concentration is created via
     * [Incarnation.createConcentration] with no explicit origin.
     *
     * @receiver the molecule name to create and set.
     */
    context(incarnation: Incarnation<T, *>, _: Node<T>)
    operator fun <T> String.unaryMinus() = -Pair(incarnation.createMolecule(this), incarnation.createConcentration())

    /**
     * Assigns the provided concentration to the molecule identified by the given name on the current [Node].
     *
     * The molecule is created via [Incarnation.createMolecule].
     *
     * @receiver a pair `(moleculeName, concentration)` to assign.
     */
    context(incarnation: Incarnation<T, *>, _: Node<T>)
    operator fun <T> Pair<String, T>.unaryMinus() = -Pair(incarnation.createMolecule(first), second)
}
