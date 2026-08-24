/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.cognitive.environments

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.TimeDistributedReaction
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.model.physics.environments.Dynamics2DEnvironment
import it.unibo.alchemist.model.physics.reactions.PhysicsUpdate
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.timedistributions.ExponentialTime
import it.unibo.alchemist.test.loadYamlSimulation
import it.unibo.alchemist.test.startSimulation

class TestEnvironmentWithDynamics<T, P> :
    StringSpec({
        "no node should exit the square" {
            loadYamlSimulation<T, P>("testSquareExit.yml").startSimulation(
                steps = 10000,
                whenFinished = { environment, _, _ ->
                    environment as Dynamics2DEnvironment
                    environment
                        .getNodesWithin(
                            environment.shapeFactory.rectangle(120.0, 120.0).transformed {
                                origin(environment.origin)
                            },
                        ).size shouldBe environment.nodeCount.current
                },
            )
        }
        "Environment should allow physics update rate customization" {
            val environment = loadYamlSimulation<T, P>("testCustomizeGlobalReactionRate.yml").environment
            environment.reactions.size shouldBe 1
            environment.reactions
                .first()
                .shouldBeInstanceOf<TimeDistributedReaction<*>>()
                .rate shouldBe 1.5
        }
        "Ignore time distribution when updateRate is Specified" {
            val environment = loadYamlSimulation<T, P>("testCustomizeGlobalReactionRate2.yml").environment
            environment.reactions.size shouldBe 1
            environment.reactions
                .first()
                .shouldBeInstanceOf<TimeDistributedReaction<*>>()
                .rate shouldBe 0.5
        }
        "Customize rate with time-distribution" {
            val environment = loadYamlSimulation<T, P>("testCustomizeGlobalReactionRate3.yml").environment
            environment.reactions.size shouldBe 1
            val globalReaction =
                environment.reactions
                    .first()
                    .shouldBeInstanceOf<TimeDistributedReaction<*>>()
            globalReaction.timeDistribution::class shouldBe ExponentialTime::class
            globalReaction.rate shouldBe 0.5
        }
        "PhysicsUpdate can be overriden only once" {
            val environment =
                EnvironmentWithDynamics(
                    SupportedIncarnations.get<T, Euclidean2DPosition>("protelis").orElseThrow(),
                )
            environment.addReaction(PhysicsUpdate(environment as Dynamics2DEnvironment<T>, 2.0))
            shouldThrow<IllegalArgumentException> {
                environment.addReaction(PhysicsUpdate(environment as Dynamics2DEnvironment<T>))
            }
            environment.reactions
                .first()
                .shouldBeInstanceOf<TimeDistributedReaction<*>>()
                .rate shouldBe 2.0
        }
    }) where P : Position<P>, P : Vector<P>
