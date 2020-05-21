/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.EnvironmentWithObstacles
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.environments.PhysicsEnvironment
import it.unibo.alchemist.model.interfaces.geometry.Vector
import java.util.Optional
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Discards the positions the pedestrian can't fit.
 */
fun <T, P> Sequence<P>.discardUnsuitablePositions(
    environment: Environment<T, P>,
    pedestrian: Node<T>
): Sequence<P> where P : Position<P>, P : Vector<P> = this
    .map { if (environment is EnvironmentWithObstacles<*, T, P>) {
        /*
         * Take into account obstacles
         */
        environment.next(environment.getPosition(pedestrian), it)
    } else {
        it
    } }
    .filter {
        /*
         * Take into account other pedestrians.
         */
        environment !is PhysicsEnvironment<T, P, *, *> || environment.canNodeFitPosition(pedestrian, it)
    }
