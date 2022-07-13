/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.environments.Dynamics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.testsupport.loadYamlSimulation
import it.unibo.alchemist.testsupport.startSimulation

class TestEnvironmentWithDynamics<T, P> : StringSpec({
    "no node should exit the square" {
        loadYamlSimulation<T, P>("testSquareExit.yml").startSimulation(
            steps = 10000,
            whenFinished = { environment, _, _ ->
                environment as Dynamics2DEnvironment
                environment.getNodesWithin(
                    environment.shapeFactory.rectangle(120.0, 120.0).transformed {
                        origin(environment.origin)
                    }
                ).size shouldBe environment.nodeCount
            }
        )
    }
    "Environemnt should allow physics update rate customization" {
        val environment = loadYamlSimulation<T, P>("testCustomizeGlobalReactionRate.yml")
        environment.globalReactions.size shouldBe 1
        environment.globalReactions.first().rate shouldBe 1.5
    }
    "Ignore time distribution when updateRate is Specified" {
        val environment = loadYamlSimulation<T, P>("testCustomizeGlobalReactionRate2.yml")
        environment.globalReactions.size shouldBe 1
        environment.globalReactions.first().rate shouldBe 0.5
    }
    "Customize rate with time-distribution" {
        val environment = loadYamlSimulation<T, P>("testCustomizeGlobalReactionRate3.yml")
        environment.globalReactions.size shouldBe 1
        environment.globalReactions.first().rate shouldBe 0.5
    }
}) where P : Position<P>, P : Vector<P>
