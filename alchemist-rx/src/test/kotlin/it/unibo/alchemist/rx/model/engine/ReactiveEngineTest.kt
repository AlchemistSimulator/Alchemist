/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.rx.model.engine

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Context
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.actions.AbstractAction
import it.unibo.alchemist.model.actions.SetLocalMoleculeConcentration
import it.unibo.alchemist.model.conditions.MoleculeHasConcentration
import it.unibo.alchemist.model.reactions.ChemicalReaction
import it.unibo.alchemist.model.timedistributions.DiracComb
import it.unibo.alchemist.rx.model.adapters.ObservableNode
import it.unibo.alchemist.rx.model.utils.TestEnvironmentFactory.spawnNode
import it.unibo.alchemist.rx.model.utils.TestEnvironmentFactory.withReactiveEngine

@Suppress("AssignedValueIsNeverRead")
class ReactiveEngineTest : FunSpec({
    test("simple engine execution test") {
        lateinit var node: ObservableNode<Double>
        lateinit var reaction1: Reaction<Double>
        lateinit var reaction2: Reaction<Double>

        val molecule = Molecule { "A" }
        var r2Executed = false

        withReactiveEngine({
            node = spawnNode(0.0, 0.0)
            reaction1 = ChemicalReaction(node, DiracComb(1.0)).apply {
                actions = listOf(SetLocalMoleculeConcentration(node, molecule, 1.0))
            }

            reaction2 = ChemicalReaction(node, DiracComb(2.0)).apply {
                conditions = listOf(MoleculeHasConcentration(node, molecule, 1.0))
                actions = listOf(object : AbstractAction<Double>(node) {
                    override fun execute() {
                        r2Executed = true
                    }

                    override fun getContext(): Context = Context.LOCAL

                    override fun cloneAction(node: Node<Double>, reaction: Reaction<Double>): Action<Double> {
                        TODO("Not yet implemented")
                    }
                })
            }

            node.addReaction(reaction1)
            node.addReaction(reaction2)
        }) {
            node.contains(molecule) shouldBe true
            r2Executed shouldBe true
        }
    }
})
