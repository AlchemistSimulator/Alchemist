/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.cognitive.properties.Pedestrian
import it.unibo.alchemist.model.cognitive.properties.Perceptive2D
import it.unibo.alchemist.model.cognitive.properties.Social
import it.unibo.alchemist.model.linkingrules.NoLinks
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.physics.FieldOfView2D
import it.unibo.alchemist.model.physics.environments.ContinuousPhysics2DEnvironment
import it.unibo.alchemist.model.physics.environments.Physics2DEnvironment
import it.unibo.alchemist.model.physics.properties.CircularArea
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.random.RandomGenerator

/**
 * Tests that pedestrians can detect other objects in the environment.
 *
 * @param T used internally
 */
class TestSensory<T> :
    StringSpec({

        fun createHomogeneousPedestrian(
            incarnation: Incarnation<T, Euclidean2DPosition>,
            randomGenerator: RandomGenerator,
            environment: Physics2DEnvironment<T>,
        ) = GenericNode(incarnation, environment).apply {
            listOf(
                Pedestrian(randomGenerator, this),
                Social(this),
                Perceptive2D(environment, this),
                CircularArea(environment, this),
            ).forEach(this::addProperty)
        }

        "field of view" {
            val environment =
                ContinuousPhysics2DEnvironment<T>(
                    SupportedIncarnations.get<T, Euclidean2DPosition>("protelis").orElseThrow(),
                )
            val rand = MersenneTwister(1)
            environment.linkingRule = NoLinks()
            val incarnation: Incarnation<T, Euclidean2DPosition> =
                SupportedIncarnations
                    .get<T, Euclidean2DPosition>(SupportedIncarnations.getAvailableIncarnations().first())
                    .get()
            val observed = createHomogeneousPedestrian(incarnation, rand, environment)
            val origin = Euclidean2DPosition(5.0, 5.0)
            environment.addNode(observed, origin)
            val radius = 10.0
            origin.surrounding(radius).forEach {
                with(createHomogeneousPedestrian(incarnation, rand, environment)) {
                    environment.addNode(this, it)
                    environment.setHeading(this, origin - it)
                }
            }
            environment.nodes.minusElement(observed).forEach {
                with(FieldOfView2D(environment, it, radius, Math.PI / 2)) {
                    influentialNodes().size shouldBe 1
                    influentialNodes().first() shouldBe observed
                }
            }
        }
    })
