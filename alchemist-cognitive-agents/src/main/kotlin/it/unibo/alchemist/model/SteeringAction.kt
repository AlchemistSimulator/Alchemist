/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model

/**
 * Action whose purpose is moving a node inside the environment it is in.
 */
interface SteeringAction<T, P : Position<P>> : Action<T> {

    /**
     * The position the owner of this action moves to when it is executed,
     * in relative coordinates with respect to its current position.
     */
    fun nextPosition(): P
}
