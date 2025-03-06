/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.movestrategies.target

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.movestrategies.TargetSelectionStrategy
import org.danilopianini.util.regex.Patterns
import org.slf4j.LoggerFactory
import kotlin.CharSequence
import kotlin.Number

/**
 * This strategy reads the value of a "target" molecule and tries to interpret
 * it as a coordinate.
 *
 * @param environment the environment
 * @param node the node
 * @param targetMolecule the target molecule
 * @param <T> Concentration type
</T> */
open class FollowTarget<T, P : Position<P>>(
    protected val environment: Environment<T, P>,
    private val node: Node<T>,
    val targetMolecule: Molecule,
) : TargetSelectionStrategy<T, P> {
    /**
     * @param x first coordinate extracted from the target concentration
     * @param y second coordinate extracted from the target concentration
     * @return a [Position] built using such parameters
     */
    protected open fun createPosition(
        x: Double,
        y: Double,
    ): P = environment.makePosition(x, y)

    /**
     * the current position.
     */
    protected val currentPosition: P get() = environment.getPosition(node)

    override fun getTarget(): P {
        val conc = node.getConcentration(targetMolecule) ?: return currentPosition
        @Suppress("UNCHECKED_CAST")
        return when (conc) {
            is Position<*> -> conc as P
            is CharSequence, is Iterable<*> -> conc.extractCoordinates() ?: currentPosition
            else -> conc.toString().extractCoordinates() ?: currentPosition
        }
    }

    /**
     * Extracts a pair of coordinates from an iterable or a string representation.
     */
    private fun Any.extractCoordinates(): P? {
        val values: Sequence<Number> =
            when (this) {
                is CharSequence -> Regex(Patterns.FLOAT).findAll(this).map { it.value.toNumber() }
                is Iterable<*> -> asSequence().map { it.toNumber() }
                else -> emptySequence()
            }
        val coords = values.toList()
        return when (coords.size) {
            environment.dimensions -> environment.makePosition(coords)
            in 0..<environment.dimensions -> {
                LOGGER.warn(
                    "Parsing {} (current value of {}) as a target returned {} coordinates ({}), but {} are required",
                    this,
                    targetMolecule,
                    coords.size,
                    coords,
                    environment.dimensions,
                )
                null
            }
            else -> {
                val trimmed = coords.take(environment.dimensions)
                LOGGER.warn(
                    "Parsing {} (current value of {}) as a target returned {}, of which only {} will be used",
                    this,
                    targetMolecule,
                    coords,
                    trimmed,
                )
                environment.makePosition(trimmed)
            }
        }
    }

    private fun conversionError(value: Any?): Nothing =
        error(
            "${this::class.simpleName} tried to convert " +
                "$this (${value?.let { it::class.simpleName}}) to a Number, but failed",
        )

    /**
     * Tries to convert an object to Double, handling exceptions safely.
     */
    private fun Any?.toNumber(): Number =
        when (this) {
            is Number -> this
            is CharSequence -> toString().toDoubleOrNull()
            else -> null
        } ?: conversionError(this)

    override fun cloneIfNeeded(
        destination: Node<T>,
        reaction: Reaction<T>,
    ): FollowTarget<T, P> = FollowTarget(environment, destination, this.targetMolecule)

    private companion object {
        private const val serialVersionUID = -446053307821810438L
        private val LOGGER = LoggerFactory.getLogger(FollowTarget::class.java)
    }
}
