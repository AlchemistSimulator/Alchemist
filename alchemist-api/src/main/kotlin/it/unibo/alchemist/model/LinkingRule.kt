/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model

/**
 * A policy that computes node neighborhoods.
 *
 * @param T concentration type
 * @param P position type
 */
interface LinkingRule<T, P : Position<out P>> {
    /**
     * Computes an immutable neighborhood snapshot for [center].
     */
    fun computeNeighborhood(center: Node<T>, environment: Environment<T, P>): Neighborhood<T>

    /**
     * Whether one application is sufficient to keep the topology consistent.
     */
    fun isLocallyConsistent(): Boolean
}
