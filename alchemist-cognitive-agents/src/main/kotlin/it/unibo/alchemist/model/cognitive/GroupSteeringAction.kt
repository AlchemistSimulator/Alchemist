/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.geometry.Vector

/**
 * A [SteeringAction] that is influenced by a group of pedestrians.
 *
 * @param T the concentration type.
 * @param P the position/vector type used by the steering action.
 */
interface GroupSteeringAction<T, P> : SteeringAction<T, P> where P : Position<P>, P : Vector<P> {
    /**
     * Returns the list of nodes (pedestrians) that influence this group steering action.
     *
     * @return a [List] of [Node] instances representing the influencing group.
     */
    fun group(): List<Node<T>>
}
