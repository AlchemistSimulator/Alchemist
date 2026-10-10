/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.sapere.conditions

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.observables.util.Observables.combineLatest
import it.unibo.alchemist.model.observables.util.Observables.switchMap
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.sapere.ILsaMolecule
import it.unibo.alchemist.model.sapere.ILsaNode
import it.unibo.alchemist.model.sapere.dsl.IExpression
import it.unibo.alchemist.model.sapere.dsl.ITreeNode
import it.unibo.alchemist.model.sapere.dsl.impl.NumTreeNode
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule
import org.danilopianini.lang.HashString

/**
 * An LSA condition searching an instance of the [molecule] template in the neighbors of the node.
 */
class LsaNeighborhoodCondition(
    reaction: NodeReaction<List<ILsaMolecule>>,
    molecule: ILsaMolecule,
    private val environment: Environment<List<ILsaMolecule>, *>,
) : LsaStandardCondition(molecule, reaction) {
    /*
     * The matches depend on the LSA space of every neighbor: an update is triggered every time the neighborhood
     * changes, or the LSA space of one of its members changes.
     */
    private val neighborhoodMatchInput: Observable<*> = environment.getNeighborhood(targetNode)
        .map { it.neighbors }
        .switchMap { neighbors ->
            neighbors.filterIsInstance<ILsaNode>().map { it.observeLsaSpace() }.combineLatest { it }
        }

    override fun getMatchingInput(): Observable<*> = neighborhoodMatchInput

    override fun cloneOnNodeReaction(newReaction: NodeReaction<List<ILsaMolecule>>): LsaNeighborhoodCondition =
        LsaNeighborhoodCondition(newReaction, molecule, environment)

    override fun filter(
        matchesList: MutableList<MutableMap<HashString, ITreeNode<*>>>,
        validNodes: MutableList<ILsaNode>,
        retrieved: MutableList<MutableMap<ILsaNode, MutableList<ILsaMolecule>>>,
    ): Boolean = when {
        validNodes.isEmpty() -> false
        matchesList.isEmpty() -> createInitialMatches(matchesList, validNodes, retrieved)
        else -> filterExistingMatches(matchesList, validNodes, retrieved)
    }

    /**
     * This is the first condition: it must create all the matches, checking every neighbor.
     */
    private fun createInitialMatches(
        matchesList: MutableList<MutableMap<HashString, ITreeNode<*>>>,
        validNodes: MutableList<ILsaNode>,
        retrieved: MutableList<MutableMap<ILsaNode, MutableList<ILsaMolecule>>>,
    ): Boolean {
        var lastSize = 0
        var i = 0
        while (i < validNodes.size) {
            val neighbor = validNodes[i]
            createMatches(molecule, neighbor, matchesList, retrieved)
            if (matchesList.size > lastSize) {
                /*
                 * This neighbor has the LSA we are checking for, so new matches have been created. Each of them
                 * gets the selected node special property, so that the actions on a single neighbor are bound.
                 */
                val nodeId = NumTreeNode(neighbor.id)
                while (lastSize < matchesList.size) {
                    matchesList[lastSize][LsaMolecule.SYN_SELECTED] = nodeId
                    lastSize++
                }
                i++
            } else {
                // This neighbor is not valid, and is removed from the valid nodes.
                validNodes.removeAt(i)
            }
        }
        // Valid if at least a valid match has been created.
        return makeValid(matchesList.isNotEmpty())
    }

    /**
     * At least a condition has been run before: this condition must check the existing matches,
     * removing all those which are no longer valid.
     */
    private fun filterExistingMatches(
        matchesList: MutableList<MutableMap<HashString, ITreeNode<*>>>,
        validNodes: MutableList<ILsaNode>,
        retrieved: MutableList<MutableMap<ILsaNode, MutableList<ILsaMolecule>>>,
    ): Boolean {
        var matchesFound = false
        val newValidNodes = LinkedHashSet<ILsaNode>(validNodes.size)
        for (i in matchesList.indices.reversed()) {
            val match = MatchUnderAnalysis(
                index = i,
                matches = matchesList[i],
                alreadyRemoved = retrieved[i],
                partialInstance = molecule.allocateVar(matchesList[i]),
                duplicateVariables = molecule.hasDuplicateVariables(),
            )
            /*
             * Other neighborhood conditions may have run before: if the selected node has been instanced,
             * the condition runs on that node only.
             */
            val selectedNode = match.matches[LsaMolecule.SYN_SELECTED]
            matchesFound = if (selectedNode != null) {
                val selectedNodeId = (selectedNode.data as Double).toInt()
                filterMatchesForSelectedNode(validNodes, retrieved, matchesList, newValidNodes, match, selectedNodeId)
            } else {
                filterMatchesWithoutSelectedNode(validNodes, retrieved, matchesList, newValidNodes, match)
            } || matchesFound
        }
        // Valid nodes redefinition.
        validNodes.retainAll(newValidNodes)
        return makeValid(matchesFound)
    }

    private fun filterMatchesForSelectedNode(
        validNodes: List<ILsaNode>,
        retrieved: MutableList<MutableMap<ILsaNode, MutableList<ILsaMolecule>>>,
        matchesList: MutableList<MutableMap<HashString, ITreeNode<*>>>,
        newValidNodes: MutableSet<ILsaNode>,
        match: MatchUnderAnalysis,
        selectedNodeId: Int,
    ): Boolean {
        val selected = validNodes.lastOrNull { it.id == selectedNodeId }
        val otherMatches = selected?.let {
            val alreadyRemoved = match.alreadyRemoved.getOrPut(it) { ArrayList() }
            calculateMatches(match.partialInstance, match.duplicateVariables, it.lsaSpace, alreadyRemoved)
        }
        return when {
            selected == null || otherMatches == null -> false
            otherMatches.isEmpty() -> {
                /*
                 * This match is removed, but the node may still be valid for other matches:
                 * the node validity check is performed afterwards.
                 */
                retrieved.removeAt(match.index)
                matchesList.removeAt(match.index)
                false
            }
            else -> {
                incorporateNewMatches(
                    selected,
                    otherMatches,
                    match.matches,
                    molecule,
                    matchesList,
                    match.alreadyRemoved,
                    retrieved,
                )
                newValidNodes.add(selected)
                true
            }
        }
    }

    private fun filterMatchesWithoutSelectedNode(
        validNodes: List<ILsaNode>,
        retrieved: MutableList<MutableMap<ILsaNode, MutableList<ILsaMolecule>>>,
        matchesList: MutableList<MutableMap<HashString, ITreeNode<*>>>,
        newValidNodes: MutableSet<ILsaNode>,
        match: MatchUnderAnalysis,
    ): Boolean {
        // No node has been selected for this match yet.
        val matchesPerNode = HashMap<ILsaNode, List<ILsaMolecule>>()
        for (neighbor in validNodes.asReversed()) {
            val alreadyRemoved = match.alreadyRemoved.getOrPut(neighbor) { ArrayList() }
            val otherMatches =
                calculateMatches(match.partialInstance, match.duplicateVariables, neighbor.lsaSpace, alreadyRemoved)
            if (otherMatches.isNotEmpty()) {
                matchesPerNode[neighbor] = otherMatches
                newValidNodes.add(neighbor)
            }
        }
        if (matchesPerNode.isEmpty()) {
            /*
             * All the neighbors which are still valid have been checked:
             * this condition is not valid for the current match, which is removed.
             */
            matchesList.removeAt(match.index)
            retrieved.removeAt(match.index)
            return false
        }
        incorporateNewMatches(matchesPerNode, match.matches, molecule, matchesList, match.alreadyRemoved, retrieved)
        return true
    }

    override fun toString(): String = "+${super.toString()}"

    private class MatchUnderAnalysis(
        val index: Int,
        val matches: MutableMap<HashString, ITreeNode<*>>,
        val alreadyRemoved: MutableMap<ILsaNode, MutableList<ILsaMolecule>>,
        val partialInstance: List<IExpression>,
        val duplicateVariables: Boolean,
    )
}
