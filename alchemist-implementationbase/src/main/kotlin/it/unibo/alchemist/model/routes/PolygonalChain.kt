/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.routes

import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Route
import java.io.Serial
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSuperclassOf

/**
 * Abstract route implementation.
 *
 * @param <P> the type of position that the route is composed
 */
open class PolygonalChain<P : Position<*>>(
    override val points: List<P>,
) : Route<P> {
    private var distance = Double.NaN
    private var hash = 0

    /**
     * @param positions the positions this route traverses
     */
    @SafeVarargs
    constructor(vararg positions: P) : this(positions.toList())

    /**
     * @param points the positions this route traverses
     */
    init {
        require(points.isNotEmpty()) { "At least one point is required to create a Route" }
    }

    override fun equals(other: Any?): Boolean =
        other === this || other is PolygonalChain<*> && this::class == other::class && points == other.points

    override fun getPoint(step: Int): P =
        points.getOrNull(step)
            ?: error("$step is not a valid point number for this route (length ${size()})")

    override fun hashCode(): Int {
        if (hash == 0) {
            hash = points.hashCode()
        }
        return hash
    }

    override fun length(): Double {
        if (distance.isNaN()) {
            distance = points.asSequence().zipWithNext().sumOf { (cur, next) -> cur.typedDistanceTo(next) }
        }
        return distance
    }

    override fun size(): Int = points.size

    /**
     * Prints the class name and the list of positions.
     */
    override fun toString(): String = javaClass.getSimpleName() + points

    override fun iterator() = points.iterator()

    private companion object {
        @Serial
        private const val serialVersionUID = 1L

        @Suppress("UNCHECKED_CAST")
        private fun <U : Position<U>> Position<*>.typedDistanceTo(other: Position<*>): Double {
            val thisClass = this::class
            val otherClass = other::class
            return when {
                thisClass.isSuperclassOf(otherClass) -> (this as U).distanceTo(other as U)
                thisClass.isSubclassOf(otherClass) -> (other as U).distanceTo(this as U)
                else ->
                    error(
                        "Incomparable positions: $this (${thisClass.simpleName}) and $other (${otherClass.simpleName})",
                    )
            }
        }
    }
}
