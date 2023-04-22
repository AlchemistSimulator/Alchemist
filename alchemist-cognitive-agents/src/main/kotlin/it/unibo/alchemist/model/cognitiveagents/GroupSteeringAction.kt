/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitiveagents

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.geometry.Vector

/**
 * A [SteeringAction] related to a group of pedestrians.
 */
interface GroupSteeringAction<T, P> : SteeringAction<T, P> where P : Position<P>, P : Vector<P> {

    /**
     * The list of pedestrians influencing this action.
     */
    fun group(): List<Node<T>>
}
