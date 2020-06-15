/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.Action
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.EnvironmentWithObstacles
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.environments.PhysicsEnvironment
import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * Discards the positions the pedestrian can't fit.
 */
fun <T, P> Sequence<P>.discardUnsuitablePositions(
    environment: Environment<T, P>,
    pedestrian: Node<T>
): Sequence<P> where P : Position<P>, P : Vector<P> = this
    .map { desirablePosition ->
        if (environment is EnvironmentWithObstacles<*, T, P>) {
            /*
             * Takes into account obstacles.
             */
            environment.next(environment.getPosition(pedestrian), desirablePosition)
        } else {
            desirablePosition
        }
    }
    .filter {
        /*
         * Takes into account other pedestrians.
         */
        environment !is PhysicsEnvironment<T, P, *, *> || environment.canNodeFitPosition(pedestrian, it)
    }

/**
 * Requires [node] is [N] and returns the object produced by [builder]. Used to clone steering actions.
 */
inline fun <reified N : Node<*>, S : Action<*>> requireNodeTypeAndProduce(node: Node<*>, builder: (N) -> S): S {
    require(node is N) { "Incompatible node type. Required ${N::class}, found ${node::class}" }
    return builder(node)
}
