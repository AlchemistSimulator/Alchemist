/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.steering

import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.cognitive.SteeringAction
import it.unibo.alchemist.model.cognitive.SteeringStrategy
import it.unibo.alchemist.model.geometry.Vector

/**
 * Decorator for a [SteeringStrategy] that filters the provided steering actions before
 * delegating the combination logic to the wrapped strategy.
 *
 * @param T the concentration type.
 * @param P the [Position] type used by the strategy.
 * @param steerStrategy the underlying strategy to which the filtered actions are delegated.
 * @param filter the filter function applied to the actions list before delegation.
 */
open class Filtered<T, P>(
    private val steerStrategy: SteeringStrategy<T, P>,
    private val filter: List<SteeringAction<T, P>>.() -> List<SteeringAction<T, P>>,
) : SteeringStrategy<T, P> by steerStrategy
    where P : Position<P>, P : Vector<P> {
    /** Delegates to [steerStrategy] after applying [filter] to [actions]. */
    override fun computeNextPosition(actions: List<SteeringAction<T, P>>) =
        steerStrategy.computeNextPosition(actions.filter())
}
