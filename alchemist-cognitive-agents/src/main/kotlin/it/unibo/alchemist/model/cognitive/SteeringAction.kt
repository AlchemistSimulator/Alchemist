/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive

import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Position

/**
 * An action that describes movement of a node inside its environment.
 *
 * @param T the concentration type.
 * @param P the [Position] type used by the action.
 */
interface SteeringAction<T, P : Position<P>> : Action<T> {
    /**
     * Returns the target relative position the owner will move to when this action is executed.
     * The position is relative to the owner's current position.
     *
     * @return the next relative position as a [P].
     */
    fun nextPosition(): P
}
