/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.actions

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.cognitiveagents.GroupSteeringAction
import it.unibo.alchemist.model.geometry.Transformation
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.model.interfaces.properties.PedestrianProperty

/**
 * An abstract [GroupSteeringAction].
 */
abstract class AbstractGroupSteeringAction<T, P, A>(
    environment: Environment<T, P>,
    reaction: Reaction<T>,
    override val pedestrian: PedestrianProperty<T>,
) : AbstractSteeringAction<T, P, A>(environment, reaction, pedestrian),
    GroupSteeringAction<T, P>
    where P : Position<P>,
          P : Vector<P>,
          A : Transformation<P> {

    /**
     * Computes the centroid of the [group] in absolute coordinates.
     */
    protected fun centroid(): P = with(group()) {
        map { environment.getPosition(it) }.reduce { acc, pos -> acc + pos } / size.toDouble()
    }
}
