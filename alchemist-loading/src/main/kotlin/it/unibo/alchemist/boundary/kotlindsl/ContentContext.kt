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
object ContentContext {

    /**
     * Assigns the given [concentration] to this [Molecule] on the current [Node].
     *
     * Context receivers:
     * - [incarnation]: used to create concentrations by default when none is provided.
     * - [node]: the target [Node] that will receive the concentration.
     *
     * @receiver the molecule to which the concentration will be assigned
     * @param concentration the concentration value to assign; when omitted, a fresh
     * concentration instance is created via [Incarnation.createConcentration].
     */
    context(incarnation: Incarnation<T, *>, node: Node<T>)
    operator fun <T> Molecule.invoke(concentration: T = incarnation.createConcentration()) =
        node.setConcentration(this, concentration)

    /**
     * Create the [Molecule] identified by this string using the current [Incarnation]
     * and assign it the provided [concentration] on the current [Node].
     *
     * Context receivers:
     * - [incarnation]: used to create the molecule and, by default, the concentration.
     * - [node]: the target [Node] that will receive the concentration.
     *
     * @receiver the molecule name (string) to create
     * @param concentration the concentration value to assign; when omitted, a fresh
     * concentration instance is created via [Incarnation.createConcentration].
     */
    context(incarnation: Incarnation<T, *>, node: Node<T>)
    operator fun <T> String.invoke(concentration: T = incarnation.createConcentration()) =
        incarnation.createMolecule(this)(concentration)

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
     * Assigns a default concentration for this [Molecule] to the current [Node].
     *
     * This operator invokes the parameterless molecule invoker, which in turn
     * creates a default concentration via [Incarnation.createConcentration] and assigns
     * it to the current [Node].
     *
     * Context receivers:
     * - [incarnation]: used to create the default concentration
     * - [Node]: the target node
     *
     * @receiver the molecule whose default concentration will be set on the current node
     */
    context(incarnation: Incarnation<T, *>, _: Node<T>)
    operator fun <T> Molecule.unaryMinus() = this()

    /**
     * Create a [Molecule] using its name and assign a default concentration to the current [Node].
     *
     * This is equivalent to calling the string-based invoker with no explicit concentration
     * and therefore creates and assigns a default concentration via the current [Incarnation].
     *
     * Context receivers:
     * - [incarnation]: used to create the molecule and its default concentration
     * - [Node]: the target node
     *
     * @receiver the molecule name to create and set with its default concentration
     */
    context(incarnation: Incarnation<T, *>, _: Node<T>)
    operator fun <T> String.unaryMinus() = this()
}
