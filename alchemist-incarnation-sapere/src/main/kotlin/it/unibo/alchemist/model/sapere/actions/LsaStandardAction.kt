/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.sapere.actions

import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.sapere.ILsaMolecule
import it.unibo.alchemist.model.sapere.ILsaNode
import it.unibo.alchemist.model.sapere.dsl.ITreeNode
import it.unibo.alchemist.model.sapere.dsl.impl.ConstTreeNode
import it.unibo.alchemist.model.sapere.dsl.impl.NumTreeNode
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule
import org.apache.commons.math3.random.RandomGenerator
import org.danilopianini.lang.HashString
import org.slf4j.LoggerFactory

/**
 * Adds the LSA [molecule], with its variables allocated using the current matches, to the local node.
 * The [randomGenerator] is used to allocate #RAND, if present.
 */
open class LsaStandardAction(
    private val randomGenerator: RandomGenerator?,
    reaction: NodeReaction<List<ILsaMolecule>>,
    /**
     * The modified molecule.
     */
    val molecule: ILsaMolecule,
) : AbstractLsaAction(reaction, listOf(molecule)) {
    private val initRand: Boolean
    private val initNode: Boolean
    private val nodeId: ITreeNode<*>?

    init {
        val moleculeString = molecule.toString()
        initRand = moleculeString.contains(LsaMolecule.SYN_RAND)
        initNode = moleculeString.contains(LsaMolecule.SYN_NODE_ID)
        if (initRand && randomGenerator == null) {
            LOGGER.warn(
                "{} is used in {}, but the RandomGenerator has not been initialized. This WILL lead to problems.",
                LsaMolecule.SYN_RAND,
                molecule,
            )
        }
        nodeId = if (initNode) ConstTreeNode(HashString("node${targetNode.id}")) else null
    }

    override fun cloneOnNodeReaction(newReaction: NodeReaction<List<ILsaMolecule>>): LsaStandardAction =
        LsaStandardAction(randomGenerator, newReaction, molecule)

    override fun execute() = setConcentration(lsaNode)

    /**
     * Executes on the passed [node].
     */
    protected open fun setConcentration(node: ILsaNode) {
        if (initRand) {
            addMatch(LsaMolecule.SYN_RAND, NumTreeNode(checkNotNull(randomGenerator).nextDouble()))
        }
        if (initNode) {
            addMatch(LsaMolecule.SYN_NODE_ID, checkNotNull(nodeId))
        }
        node.setConcentration(LsaMolecule(molecule.allocateVar(matches)))
    }

    override fun toString(): String = molecule.toString()

    private companion object {
        private val LOGGER = LoggerFactory.getLogger(LsaStandardAction::class.java)
    }
}
