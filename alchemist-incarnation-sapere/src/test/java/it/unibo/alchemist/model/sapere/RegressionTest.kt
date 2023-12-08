/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.sapere

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.sapere.nodes.LsaNode
import it.unibo.alchemist.model.timedistributions.DiracComb
import it.unibo.alchemist.test.createSimulation
import it.unibo.alchemist.test.loadAlchemistFromResource
import it.unibo.alchemist.test.runInCurrentThread
import org.apache.commons.math3.random.MersenneTwister

class RegressionTest : StringSpec(
    {
        "reactions in format a --> *b should generate outgoing dependencies for both a and b" {
            val twoOutGoingDependencies = with(SAPEREIncarnation<Euclidean2DPosition>()) {
                with(Continuous2DEnvironment<List<ILsaMolecule>>(this)) {
                    createReaction(
                        MersenneTwister(),
                        this,
                        LsaNode(this),
                        DiracComb(1.0),
                        "{x} --> *{y}",
                    )
                }
            }
            twoOutGoingDependencies.outboundDependencies.size shouldBe 2
            loadAlchemistFromResource("it/unibo/alchemist/regressions/bug1718.yml")
                .getDefault<Any, Nothing>()
                .createSimulation(finalStep = 100)
                .runInCurrentThread()
                .error
                .ifPresent { throw it }
        }
    },
)
