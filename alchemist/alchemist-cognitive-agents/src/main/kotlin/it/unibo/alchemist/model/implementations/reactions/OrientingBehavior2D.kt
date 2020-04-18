/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.reactions

import it.unibo.alchemist.model.cognitiveagents.characteristics.cognitive.OrientingBehavior
import it.unibo.alchemist.model.implementations.actions.Seek2D
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.OrientingPedestrian
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.Time
import it.unibo.alchemist.model.interfaces.TimeDistribution
import it.unibo.alchemist.model.interfaces.environments.Euclidean2DEnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DConvexShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.graph.Euclidean2DPassage

/**
 * A [Reaction] representing the [OrientingBehavior] of a pedestrian.
 */
class OrientingBehavior2D<T, N : Euclidean2DConvexShape, E, M : ConvexPolygon>(
    private val environment: Euclidean2DEnvironmentWithGraph<*, T, M, Euclidean2DPassage>,
    private val pedestrian: OrientingPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, N, E>,
    timeDistribution: TimeDistribution<T>
) : AbstractReaction<T>(pedestrian, timeDistribution) {

    private val behavior: OrientingBehavior<T, N, E, M> = OrientingBehavior(environment, pedestrian)

    override fun updateInternalStatus(curTime: Time, executed: Boolean, env: Environment<T, *>) = behavior.update()

    @Suppress("UNCHECKED_CAST")
    override fun cloneOnNewNode(n: Node<T>, currentTime: Time): Reaction<T> {
        require(n as? OrientingPedestrian<
            T, Euclidean2DPosition, Euclidean2DTransformation, M, Euclidean2DPassage> != null) {
            "node not compatible, required: " + pedestrian.javaClass + ", found: " + n.javaClass
        }
        n as OrientingPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, M, Euclidean2DPassage>
        return OrientingBehavior2D(environment, n, timeDistribution)
    }

    override fun getRate(): Double = timeDistribution.rate

    override fun execute() {
        Seek2D(environment, this, pedestrian, *behavior.desiredPosition.coordinates).execute()
    }
}
