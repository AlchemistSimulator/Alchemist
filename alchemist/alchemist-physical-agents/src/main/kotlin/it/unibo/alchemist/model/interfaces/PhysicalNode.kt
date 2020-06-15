/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.interfaces.environments.PhysicsEnvironment
import it.unibo.alchemist.model.interfaces.geometry.GeometricShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.nodes.NodeWithShape

/**
 * A [NodeWithShape] capable of interacting physically with others (e.g. they may bump into each other, or even push
 * each other). Each physical node is responsible for the computation of the physical forces to which it is subject.
 */
interface PhysicalNode<T, P, A, F> : NodeWithShape<T, P, A>
    where P : Position<P>, P : Vector<P>,
          A : GeometricTransformation<P>,
          F : GeometricShapeFactory<P, A> {

    /**
     * @returns a list of vectors representing the physical forces acting on this node
     */
    fun physicalForces(environment: PhysicsEnvironment<T, P, A, F>): List<P>
}
