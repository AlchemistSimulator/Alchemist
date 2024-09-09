/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.movestrategies.target

import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.none
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.movestrategies.TargetSelectionStrategy
import it.unibo.alchemist.model.observation.Observable

/**
 * This strategy reads the value of a "target" molecule and tries to interpret
 * it as a coordinate.
 *
 * @param <T>
 * Concentration type
</T> */
open class FollowTarget<T, P : Position<P>>(
    /**
     * @return the environment
     */
    protected val environment: Environment<T, P>,
    private val node: Node<T>,
    /**
     * @return the molecule holding the destination information
     */
    val targetMolecule: Molecule,
) : TargetSelectionStrategy<T, P> {

    /**
     * The target position, as [Observable] property.
     */
    val target: Observable<Option<P>> = node.getConcentration(targetMolecule).map { optionalConcentration ->
        optionalConcentration.map { concentration ->
            when (concentration) {
                is Number -> environment.makePosition(
                    *generateSequence { concentration }.take(environment.getDimensions()).toList().toTypedArray(),
                )
                is Position<*> -> positionOf(concentration)
                is Collection<*> -> {
                    environment.makePosition(*concentration.map { it.toNumber() }.toTypedArray())
                }
                is CharSequence -> environment.makePosition(
                    *concentration.split(",") // Split by comma
                        .flatMap { it.trim().split(';') } // Split by semicolon
                        .map { it.trim().toNumber() } // Convert to number
                        .toTypedArray(),
                )
                else -> error("Cannot convert $concentration to a position")
            }
        }
    }

    /**
     * The target position, if available.
     */
    var observedTarget: Option<P> = none()

    init {
        target.onChange(this) {
            observedTarget = it
        }
    }

    /**
     * @param x
     * first coordinate extracted from the target concentration
     * @param y
     * second coordinate extracted from the target concentration
     * @return a [Position] built using such parameters
     */
    protected open fun positionOf(x: Double, y: Double): P {
        return environment.makePosition(x, y)
    }

    protected open fun positionOf(original: Position<*>): P {
        val typeTrick: Array<out Number> = original.coordinates.map { it }.toTypedArray()
        return environment.makePosition(*typeTrick)
    }

    protected val currentPosition: P get() = environment.getPosition(node)

    override fun getTarget(): P = observedTarget.getOrElse { currentPosition }

    override fun cloneIfNeeded(destination: Node<T>, reaction: Reaction<T>): FollowTarget<T, P> {
        return FollowTarget(environment, destination, targetMolecule)
    }

    companion object {
//        private val floatRegex = Regex("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?")

        private fun Any?.toNumber(): Number {
            return when (this) {
                is Number -> this
                is String -> this.toDouble()
                else -> error("Cannot convert $this to a number")
            }
        }
    }
}
