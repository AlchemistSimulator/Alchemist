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
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.implementations.linkingrules.NoLinks
import it.unibo.alchemist.model.implementations.nodes.HomogeneousPedestrian2D
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.implementations.geometry.euclidean2d.FieldOfView2D
import it.unibo.alchemist.model.interfaces.Incarnation
import org.apache.commons.math3.random.MersenneTwister

/**
 * Tests that pedestrians can detect other objects in the environment.
 *
 * @param T used internally
 */
class TestSensory<T> : StringSpec({

    "field of view" {
        val env = Continuous2DEnvironment<T>(
            SupportedIncarnations.get<T, Euclidean2DPosition>("protelis").orElseThrow()
        )
        val rand = MersenneTwister(1)
        env.linkingRule = NoLinks()
        val incarnation: Incarnation<T, Euclidean2DPosition> = SupportedIncarnations
            .get<T, Euclidean2DPosition>(SupportedIncarnations.getAvailableIncarnations().first())
            .get()
        val observed = HomogeneousPedestrian2D(incarnation, rand, env)
        val origin = Euclidean2DPosition(5.0, 5.0)
        env.addNode(observed, origin)
        val radius = 10.0
        origin.surrounding(radius).forEach {
            with(HomogeneousPedestrian2D(incarnation, rand, env)) {
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
