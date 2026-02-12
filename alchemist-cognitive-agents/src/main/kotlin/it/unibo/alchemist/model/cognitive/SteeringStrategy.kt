/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive

import it.unibo.alchemist.model.Position

/**
 * Strategy describing how steering actions are combined to compute the next position.
 *
 * @param T the concentration type.
 * @param P the [Position] type used by actions.
 */
interface SteeringStrategy<T, P : Position<P>> {
    /**
     * Computes the next relative position by combining the provided steering actions.
     *
     * @param actions the list of actions to combine.
     * @return the next position relative to the current position as a [P].
     */
    fun computeNextPosition(actions: List<SteeringAction<T, P>>): P

    /**
     * Computes the absolute target position derived from the provided steering actions.
     *
     * @param actions the list of actions to combine.
     * @return the target position as a [P].
     */
    fun computeTarget(actions: List<SteeringAction<T, P>>): P
}
