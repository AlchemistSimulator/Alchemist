/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces.properties

import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.PhysicsEnvironment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.geometry.Transformation
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.model.interfaces.geometry.GeometricShapeFactory

/**
 * A node's capability to experience physical forces.
 */
interface PhysicalProperty<T, P, A, F> : NodeProperty<T>
    where P : Position<P>, P : Vector<P>,
          A : Transformation<P>,
          F : GeometricShapeFactory<P, A> {

    /**
     * @returns a list of vectors representing the physical forces acting on this node
     */
    fun physicalForces(environment: PhysicsEnvironment<T, P, A, F>): List<P>
}
