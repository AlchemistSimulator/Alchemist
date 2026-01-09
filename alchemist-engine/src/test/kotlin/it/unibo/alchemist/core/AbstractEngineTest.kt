/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.core

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.core.util.DependencyUtils
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.biochemistry.BiochemistryIncarnation
import it.unibo.alchemist.model.biochemistry.molecules.Biomolecule
import it.unibo.alchemist.model.conditions.NeighborHasConcentration
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.timedistributions.DiracComb
import it.unibo.alchemist.model.times.DoubleTime

@Suppress("UnnecessaryAbstractClass")
abstract class AbstractEngineTest(
    val engineFactory: (Environment<Double, *>) -> Simulation<Double, *>,
) : FreeSpec({

    "Neighbor Condition: Reaction executes when neighbor has concentration" {
        val incarnation = BiochemistryIncarnation()
        val environment = Continuous2DEnvironment(incarnation)
        environment.linkingRule = ConnectWithinDistance(2.0)

        val nodeA = GenericNode(environment)
        val nodeB = GenericNode(environment)
        val molM = Biomolecule("M")
        val molN = Biomolecule("N")

        nodeA.setConcentration(molN, 0.0)
        nodeB.setConcentration(molM, 0.0)

        nodeB.addReaction(
            DependencyUtils.SimpleReaction(nodeB, DiracComb(1.0)) {
                nodeB.setConcentration(molM, 1.0)
            },
        )

        DependencyUtils.SimpleReaction(nodeA, DiracComb(1.0)) {
            nodeA.setConcentration(molN, 1.0)
        }.apply {
            conditions = listOf(NeighborHasConcentration(nodeA, environment, molM, 1.0))
            nodeA.addReaction(this)
        }

        environment.addNode(nodeA, environment.makePosition(0, 0))
        environment.addNode(nodeB, environment.makePosition(1, 0))

        environment.addTerminator { it.simulation.time > DoubleTime(5.0) }

        val engine = engineFactory(environment)
        engine.play()
        engine.run()

        if (engine.error.isPresent) {
            throw engine.error.get()
        }

        // B should have M=1.0 (from Node B's Reaction)
        nodeB.getConcentration(molM) shouldBe 1.0

        // A should have N=1.0 (executed after observing B)
        nodeA.getConcentration(molN) shouldBe 1.0
    }
})
