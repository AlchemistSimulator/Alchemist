/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.sapere.conditions

import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.observables.util.MutableObservables.observe
import it.unibo.alchemist.model.observation.MutableObservable
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.sapere.ILsaMolecule
import it.unibo.alchemist.model.sapere.ILsaNode
import it.unibo.alchemist.model.sapere.dsl.ITreeNode
import org.danilopianini.lang.HashString

/**
 * Simple LSA condition (example: `<grad, X, 1>`), searching an instance of the [molecule] template in the node.
 * The matched LSA, if any, is not deleted from the LSA space of the node, although the reaction may do so.
 */
open class LsaStandardCondition(
    /**
     * The molecule template whose presence is tested by this condition.
     */
    protected val molecule: ILsaMolecule,
    reaction: NodeReaction<List<ILsaMolecule>>,
) : AbstractLsaCondition(reaction, setOf(molecule)) {
    private val matchInput: Observable<*> = lsaNode.observeMoleculeName(molecule.getArg(0).toString())
    private val valid: MutableObservable<Boolean> = observe(false)

    init {
        setValidity(valid)
    }

    override fun cloneOnNodeReaction(newReaction: NodeReaction<List<ILsaMolecule>>): LsaStandardCondition =
        LsaStandardCondition(molecule, newReaction)

    override fun filter(
        matchesList: MutableList<MutableMap<HashString, ITreeNode<*>>>,
        validNodes: MutableList<ILsaNode>,
        retrieved: MutableList<MutableMap<ILsaNode, MutableList<ILsaMolecule>>>,
    ): Boolean {
        val node = lsaNode
        if (matchesList.isEmpty()) {
            // This is the first condition. It must create all the matches.
            createMatches(molecule, node, matchesList, retrieved)
            return makeValid(matchesList.isNotEmpty())
        }
        /*
         * At least a condition has been run before. This condition must check the existing matches, removing all
         * those which are no longer valid. The list is scanned backwards, so that newly added matches are skipped.
         */
        var matchesFound = false
        for (i in matchesList.indices.reversed()) {
            val alreadyRemoved = retrieved[i]
            val alreadyRemovedInThisNode = alreadyRemoved.getOrPut(node) { ArrayList() }
            val matches = matchesList[i]
            /*
             * There are three possibilities:
             * 1. no valid combinations are found: this match and its retrieved entry are deleted;
             * 2. a single valid match is found: both are updated;
             * 3. further valid matches are found: new entries are created for them.
             */
            val otherMatches = calculateMatches(
                molecule.allocateVar(matches),
                molecule.hasDuplicateVariables(),
                node.lsaSpace,
                alreadyRemovedInThisNode,
            )
            if (otherMatches.isEmpty()) {
                retrieved.removeAt(i)
                matchesList.removeAt(i)
            } else {
                incorporateNewMatches(node, otherMatches, matches, molecule, matchesList, alreadyRemoved, retrieved)
                matchesFound = true
            }
        }
        return makeValid(matchesFound)
    }

    override fun getMatchingInput(): Observable<*> = matchInput

    override fun toString(): String = molecule.toString()

    /**
     * Sets the validity of this condition to [isValid], and returns it. Handle with care.
     */
    protected fun makeValid(isValid: Boolean): Boolean {
        valid.update { isValid }
        return isValid
    }
}
