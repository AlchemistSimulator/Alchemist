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
import it.unibo.alchemist.model.conditions.AbstractLocalCondition
import it.unibo.alchemist.model.sapere.ILsaCondition
import it.unibo.alchemist.model.sapere.ILsaMolecule
import it.unibo.alchemist.model.sapere.ILsaNode
import it.unibo.alchemist.model.sapere.dsl.IExpression
import it.unibo.alchemist.model.sapere.dsl.ITreeNode
import it.unibo.alchemist.model.sapere.dsl.impl.ListTreeNode
import it.unibo.alchemist.model.sapere.dsl.impl.NumTreeNode
import it.unibo.alchemist.model.sapere.dsl.impl.Type
import it.unibo.alchemist.model.sapere.dsl.impl.UIDNode
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule
import org.danilopianini.lang.HashString

/**
 * Base implementation of an [ILsaCondition], evaluated on the [ILsaNode] hosting its [reaction].
 *
 * The [molecules] are the templates on which this condition acts.
 */
abstract class AbstractLsaCondition(
    reaction: NodeReaction<List<ILsaMolecule>>,
    protected val molecules: Set<ILsaMolecule>,
) : AbstractLocalCondition<List<ILsaMolecule>>(reaction),
    ILsaCondition {
    /**
     * The node hosting this condition, as an [ILsaNode].
     */
    val lsaNode: ILsaNode get() = targetNode as ILsaNode

    abstract override fun toString(): String

    abstract override fun cloneOnNodeReaction(newReaction: NodeReaction<List<ILsaMolecule>>): AbstractLsaCondition

    /**
     * Matching utilities shared by the LSA conditions.
     */
    protected companion object {
        /**
         * Returns the molecules in [lsaSpace] that match the [partialInstance] (a template, possibly partly
         * instanced, which contains the same variable multiple times if [duplicateVariables]), excluding those
         * [alreadyRemoved] from the node.
         */
        @JvmStatic
        protected fun calculateMatches(
            partialInstance: List<IExpression>,
            duplicateVariables: Boolean,
            lsaSpace: List<ILsaMolecule>,
            alreadyRemoved: List<ILsaMolecule>,
        ): List<ILsaMolecule> = lsaSpace.filterTo(ArrayList(lsaSpace.size - alreadyRemoved.size)) { matched ->
            matched.matches(partialInstance, duplicateVariables) &&
                lsaSpace.count { it == matched } > alreadyRemoved.count { it == matched }
        }

        /**
         * Populates [matchesList] with a match for each molecule of the LSA space of [node] matching [template],
         * and [retrieved] with the molecule removed from [node] for each match.
         * Both lists should be empty when calling this method.
         */
        @JvmStatic
        protected fun createMatches(
            template: ILsaMolecule,
            node: ILsaNode,
            matchesList: MutableList<MutableMap<HashString, ITreeNode<*>>>,
            retrieved: MutableList<MutableMap<ILsaNode, MutableList<ILsaMolecule>>>,
        ) {
            val lsaSpace = node.lsaSpace
            for (matched in lsaSpace) {
                if (template.matches(matched)) {
                    /*
                     * If a match is found, the matched LSA must be added to the list of removed items corresponding
                     * to the match, and the matches map must be created and added to the possible matches.
                     */
                    val matches = HashMap<HashString, ITreeNode<*>>(matched.argsNumber() * 2 + 1, 1f)
                    matches[LsaMolecule.SYN_MOL_ID] = UIDNode(matched.toHashString())
                    updateMap(matches, matched, template)
                    matchesList.add(matches)
                    val retrievedInThisNode = HashMap<ILsaNode, MutableList<ILsaMolecule>>(lsaSpace.size, 1f)
                    retrievedInThisNode[node] = ArrayList<ILsaMolecule>(lsaSpace.size).apply { add(matched) }
                    retrieved.add(retrievedInThisNode)
                }
            }
        }

        /**
         * Updates [map] with the associations between the variables of [template] and the contents of [instance].
         */
        @JvmStatic
        protected fun updateMap(
            map: MutableMap<HashString, ITreeNode<*>>,
            instance: Iterable<IExpression>,
            template: ILsaMolecule,
        ) {
            instance.forEachIndexed { index, instanceArgument ->
                val templateArgument = template.getArg(index)
                when (templateArgument.rootNodeType) {
                    Type.VAR -> map[templateArgument.rootNodeData as HashString] = instanceArgument.rootNode
                    Type.COMPARATOR ->
                        if (templateArgument.ast.root.leftChild.type == Type.VAR) {
                            map[templateArgument.leftChildren.toHashString()] = instanceArgument.rootNode
                        }
                    Type.LISTCOMPARATOR ->
                        if (templateArgument.ast.root.leftChild.type == Type.VAR &&
                            instanceArgument.rootNodeData is Set<*>
                        ) {
                            map[templateArgument.leftChildren.toHashString()] = instanceArgument.rootNode
                        }
                    Type.LIST -> {
                        // Assignment of variables within lists.
                        val instanceList = (instanceArgument.ast.root as ListTreeNode).data.iterator()
                        (templateArgument.ast.root as ListTreeNode).data
                            .filter { it.type == Type.VAR }
                            .forEach { variable -> map[variable.toHashString()] = instanceList.next() }
                    }
                    else -> Unit
                }
            }
        }

        /**
         * Incorporates the [otherMatches] of [template] found on [node] in the [oldMatches]:
         * the first one updates [oldMatches] and the molecules [alreadyRemoved] for the current match,
         * each of the others creates a new entry in [matchesList] and [retrieved].
         */
        @JvmStatic
        @Suppress("LongParameterList") // Mirrors the state threaded through the matching algorithm.
        protected fun incorporateNewMatches(
            node: ILsaNode,
            otherMatches: List<ILsaMolecule>,
            oldMatches: MutableMap<HashString, ITreeNode<*>>,
            template: ILsaMolecule,
            matchesList: MutableList<MutableMap<HashString, ITreeNode<*>>>,
            alreadyRemoved: MutableMap<ILsaNode, MutableList<ILsaMolecule>>,
            retrieved: MutableList<MutableMap<ILsaNode, MutableList<ILsaMolecule>>>,
        ) {
            for (instance in otherMatches.drop(1)) {
                // Make copies of the match under analysis, then populate them with the new matches.
                val newMap = HashMap(oldMatches)
                updateMap(newMap, instance, template)
                matchesList.add(newMap)
                val contentMap = HashMap(alreadyRemoved)
                // If this node already has some modified molecule, copy them. Otherwise, create a new list.
                contentMap[node] = ArrayList(contentMap[node].orEmpty()).apply { add(instance) }
                retrieved.add(contentMap)
            }
            // Now, update the matches for the first entry.
            val instance = otherMatches[0]
            updateMap(oldMatches, instance, template)
            alreadyRemoved.getOrPut(node) { ArrayList() }.add(instance)
        }

        /**
         * Incorporates the node-specific [otherMatchesMap] of [template], available for multiple nodes,
         * replacing [oldMatches] in [matchesList] (and its entry in [retrieved]) with a new match for each
         * molecule of each node. The molecules [alreadyRemoved] for the current match are copied in each new entry.
         */
        @JvmStatic
        protected fun incorporateNewMatches(
            otherMatchesMap: Map<ILsaNode, List<ILsaMolecule>>,
            oldMatches: MutableMap<HashString, ITreeNode<*>>,
            template: ILsaMolecule,
            matchesList: MutableList<MutableMap<HashString, ITreeNode<*>>>,
            alreadyRemoved: MutableMap<ILsaNode, MutableList<ILsaMolecule>>,
            retrieved: MutableList<MutableMap<ILsaNode, MutableList<ILsaMolecule>>>,
        ) {
            for ((node, otherMatches) in otherMatchesMap) {
                for (instance in otherMatches) {
                    // Make copies of the match under analysis, then populate them with the new matches.
                    val newMap = HashMap(oldMatches)
                    updateMap(newMap, instance, template)
                    newMap[LsaMolecule.SYN_SELECTED] = NumTreeNode(node.id)
                    matchesList.add(newMap)
                    val contentMap = HashMap(alreadyRemoved)
                    // If this node already has some modified molecule, copy them. Otherwise, create a new list.
                    val newRetrieved = contentMap[node]?.let(::ArrayList) ?: ArrayList(node.lsaSpace.size)
                    newRetrieved.add(instance)
                    contentMap[node] = newRetrieved
                    retrieved.add(contentMap)
                }
            }
            // Remove the original entry.
            val index = matchesList.indexOf(oldMatches)
            matchesList.removeAt(index)
            retrieved.removeAt(index)
        }
    }
}
