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
 * Strategy interface describing how the next points of the steering actions
 * are combined to calculate the next position to move on.
 */
interface SteeringStrategy<T, P : Position<P>> {

    /**
     * Computes the next position starting from the steering actions the node obey to,
     * in relative coordinates with respect to its current position.
     *
     * @param actions
     *          the list of actions to combine.
     */
    fun computeNextPosition(actions: List<SteeringAction<T, P>>): P

    /**
     * Computes the target to reach starting from the steering actions the node obey to,
     * in absolute coordinates.
     *
     * @param actions
     *          the list of actions to combine.
     */
    fun computeTarget(actions: List<SteeringAction<T, P>>): P
}
