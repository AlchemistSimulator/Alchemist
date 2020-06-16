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
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position2D
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector2D

/**
 * Moves the pedestrian where the scalar field is higher.
 */
class FollowScalarField<T, P, A>(
    env: Environment<T, P>,
    reaction: Reaction<T>,
    pedestrian: Pedestrian<T, P, A>,
    concentrationIn: (P) -> Double,
    center: P? = null
) : AbstractScalarFieldAction<T, P, A>(env, reaction, pedestrian, concentrationIn, center)
    where P : Position2D<P>, P : Vector2D<P>,
          A : GeometricTransformation<P> {

    override fun Sequence<P>.selectPosition(currentPosition: P): P = this
        .maxBy { concentrationIn(it) }
        ?.takeIf { concentrationIn(it) > concentrationIn(currentPosition) }
        ?: currentPosition

    override fun cloneAction(n: Node<T>, r: Reaction<T>): Action<T> =
        requireNodeTypeAndProduce<Pedestrian<T, P, A>, FollowScalarField<T, P, A>>(n) {
            FollowScalarField(env, reaction,  it, concentrationIn, center)
        }
}
