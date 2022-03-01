/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.SupportedIncarnations
import it.unibo.alchemist.model.implementations.capabilities.BasePedestrianMovementCapability
import it.unibo.alchemist.model.implementations.capabilities.BasePerceptionOfOthers2D
import it.unibo.alchemist.model.implementations.capabilities.BaseSocialCapability
import it.unibo.alchemist.model.implementations.capabilities.BaseSpatial2DCapability
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.implementations.linkingrules.NoLinks
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.implementations.geometry.euclidean2d.FieldOfView2D
import it.unibo.alchemist.model.implementations.groups.Alone
import it.unibo.alchemist.model.implementations.nodes.GenericNode
import it.unibo.alchemist.model.interfaces.Incarnation
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.random.RandomGenerator

/**
 * Tests that pedestrians can detect other objects in the environment.
 *
 * @param T used internally
 */
class TestSensory<T> : StringSpec({

    fun createHomogeneousPedestrian(
        incarnation: Incarnation<T, Euclidean2DPosition>,
        randomGenerator: RandomGenerator,
        environment: Physics2DEnvironment<T>
    ): Node<T> {
        val node = GenericNode(incarnation, environment)
        node.addCapability(BasePedestrianMovementCapability(randomGenerator, node))
        node.addCapability(BaseSocialCapability(node, Alone(node)))
        node.addCapability(BasePerceptionOfOthers2D(environment, node))
        node.addCapability(BaseSpatial2DCapability(node, environment.shapeFactory.circle(0.3)))
        return node
    }

    "field of view" {
        val env = Continuous2DEnvironment<T>(
            SupportedIncarnations.get<T, Euclidean2DPosition>("protelis").orElseThrow()
        )
        val rand = MersenneTwister(1)
        env.linkingRule = NoLinks()
        val incarnation: Incarnation<T, Euclidean2DPosition> = SupportedIncarnations
            .get<T, Euclidean2DPosition>(SupportedIncarnations.getAvailableIncarnations().first())
            .get()
        val observed = createHomogeneousPedestrian(incarnation, rand, env)
        val origin = Euclidean2DPosition(5.0, 5.0)
        env.addNode(observed, origin)
        val radius = 10.0
        origin.surrounding(radius).forEach {
            with(createHomogeneousPedestrian(incarnation, rand, env)) {
                env.addNode(this, it)
                env.setHeading(this, origin - it)
            }
        }
        env.nodes.minusElement(observed).forEach {
            with(FieldOfView2D(env, it, radius, Math.PI / 2)) {
                influentialNodes().size shouldBe 1
                influentialNodes().first() shouldBe observed
            }
        }
    }
})
