/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.properties

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.environments.PhysicsEnvironment
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.properties.PhysicalProperty
import it.unibo.alchemist.model.interfaces.Node.Companion.asProperty
import it.unibo.alchemist.model.interfaces.properties.AreaProperty
import org.dyn4j.dynamics.Body
import org.dyn4j.dynamics.PhysicsBody
import org.dyn4j.geometry.Circle
import org.dyn4j.geometry.MassType

/**
 * PhysicalBody
 */
class Physical2D<T>(
    override val node: Node<T>,
) : PhysicalProperty<T, Euclidean2DPosition, Euclidean2DTransformation, Euclidean2DShapeFactory>,
    PhysicsBody by Body() {

    private val shape = node.asProperty<T, AreaProperty<T>>().shape

    init {
        this.addFixture(Circle(shape.radius)) // TODO: Generalize
        this.fixtures.first().restitution = 0.8 // TODO: Is it a valid coefficient?
        this.setMass(MassType.NORMAL)
    }

    override fun physicalForces(
        environment: PhysicsEnvironment<T, Euclidean2DPosition, Euclidean2DTransformation, Euclidean2DShapeFactory>
    ): List<Euclidean2DPosition> = TODO()

    override fun cloneOnNewNode(node: Node<T>): Physical2D<T> = Physical2D(node)
}
