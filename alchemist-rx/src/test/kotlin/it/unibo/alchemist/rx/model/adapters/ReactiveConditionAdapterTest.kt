/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.rx.model.adapters

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.conditions.MoleculeHasConcentration
import it.unibo.alchemist.model.reactions.ChemicalReaction
import it.unibo.alchemist.model.timedistributions.DiracComb
import it.unibo.alchemist.rx.model.adapters.reaction.ReactiveConditionAdapter
import it.unibo.alchemist.rx.model.adapters.reaction.asReactive
import it.unibo.alchemist.rx.model.utils.TestEnvironmentFactory.spawnNode
import it.unibo.alchemist.rx.model.utils.TestEnvironmentFactory.withObservableTestEnvironment

class ReactiveConditionAdapterTest : FunSpec({
    test("An implementation-base condition should be mapped to a reactive condition") {
        withObservableTestEnvironment {
            val node = spawnNode(0.0, 0.0)
            val molecule = Molecule { "A" }
            val base = MoleculeHasConcentration(node, molecule, 1.0)
            val reaction = ChemicalReaction(node, DiracComb(1.0))

            val condition: ReactiveConditionAdapter<Double> = base.asReactive(this, reaction)

            condition.observableInboundDependencies.toSet().shouldNotBeEmpty()
            condition.isValid shouldBe false

            node.setConcentration(molecule, 1.0)
            condition.isValid shouldBe true
            condition.observeValidity.current shouldBe true
            condition.observePropensityContribution.current shouldBe 1.0
        }
    }

    test("An implementation-basse reaction should be mapped to a reactive reation") {
        withObservableTestEnvironment {
            val node = spawnNode(0.0, 0.0)
            val molecule = Molecule { "A" }
            val base = ChemicalReaction(node, DiracComb(1.0)).apply {
                conditions = listOf(MoleculeHasConcentration(node, molecule, 1.0))
            }

            val reaction = base.asReactive(this)
            val seen = mutableListOf<Boolean>()

            reaction.rescheduleRequest.onChange(this) { seen.add(reaction.canExecute()) }

            reaction.canExecute() shouldBe false

            node.setConcentration(molecule, 1.0)
            reaction.canExecute() shouldBe true

            seen.size shouldBe 2
            seen[0] shouldBe false
            seen[1] shouldBe true
        }
    }
})
