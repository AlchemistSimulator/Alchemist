/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.sapere.actions

import com.google.common.collect.Sets
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.actions.AbstractLocalAction
import it.unibo.alchemist.model.sapere.ILsaAction
import it.unibo.alchemist.model.sapere.ILsaMolecule
import it.unibo.alchemist.model.sapere.ILsaNode
import it.unibo.alchemist.model.sapere.dsl.IExpression
import it.unibo.alchemist.model.sapere.dsl.ITreeNode
import it.unibo.alchemist.model.sapere.dsl.impl.Expression
import it.unibo.alchemist.model.sapere.dsl.impl.ListTreeNode
import it.unibo.alchemist.model.sapere.dsl.impl.NumTreeNode
import it.unibo.alchemist.model.sapere.dsl.impl.Type
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule
import org.danilopianini.lang.HashString

/**
 * Base implementation of an [ILsaAction], executed on the [ILsaNode] hosting its [reaction].
 *
 * The [modifiedMolecules] are the templates of the molecules this action may modify.
 */
// The helpers to access and update the matches form the toolkit offered to user-defined SAPERE actions.
@Suppress("TooManyFunctions")
abstract class AbstractLsaAction(
    reaction: NodeReaction<List<ILsaMolecule>>,
    protected val modifiedMolecules: List<ILsaMolecule>,
) : AbstractLocalAction<List<ILsaMolecule>>(reaction),
    ILsaAction {
    /**
     * The map containing the variable / value associations of the current execution context.
     */
    // The map is owned by the firing reaction and replaced at each execution, while actions write variables into it.
    @Suppress("DoubleMutabilityForCollection")
    protected var matches: MutableMap<HashString, ITreeNode<*>>? = null
        private set

    /**
     * The nodes in the current execution context. This backs the internal representation: handle with care.
     */
    protected var nodes: List<ILsaNode>? = null
        private set

    /**
     * The node hosting this action, as an [ILsaNode].
     */
    val lsaNode: ILsaNode get() = targetNode as ILsaNode

    /**
     * Used to add a new match to the matches map: [key] is the variable, [value] the associated value.
     */
    protected fun addMatch(key: HashString, value: Double) = addMatch(key, NumTreeNode(value))

    /**
     * Used to add a new match to the matches map: [key] is the variable, [value] the associated value.
     */
    protected fun addMatch(key: ITreeNode<*>, value: ITreeNode<*>) {
        require(key.type == Type.VAR) { "Only variables can be used as keys when inserting matches." }
        when (value.type) {
            Type.COMPARATOR, Type.LISTCOMPARATOR, Type.OPERATOR, Type.VAR ->
                throw IllegalArgumentException("Only instanced elements can be used as values when inserting matches.")
            else -> addMatch(key.toHashString(), value)
        }
    }

    /**
     * Used to add a new match to the matches map: [key] is the variable, [value] the associated value.
     */
    protected fun addMatch(key: IExpression, value: ITreeNode<*>) = addMatch(key.rootNode, value)

    /**
     * Used to add a new match to the matches map: [key] is the variable, [value] the associated value.
     */
    protected fun addMatch(key: IExpression, value: IExpression) = addMatch(key.rootNode, value.rootNode)

    /**
     * Used to add a new match to the matches map: [key] is the variable, [value] the associated value.
     */
    protected fun addMatch(key: HashString, value: ITreeNode<*>) {
        val currentMatches = matches ?: HashMap<HashString, ITreeNode<*>>().also { matches = it }
        currentMatches[key] = value
    }

    /**
     * Used to add a new match to the matches map: [key] is the variable, [value] the associated value.
     */
    protected fun addMatch(key: String, value: Double) = addMatch(HashString(key), NumTreeNode(value))

    /**
     * Used to add a new match to the matches map: [key] is the variable, [value] the associated value.
     */
    protected fun addMatch(key: String, value: ITreeNode<*>) = addMatch(HashString(key), value)

    /**
     * Given an LSA [template], allocates all the variables using the current matches.
     * The allocations may or not produce an instance: it does only if all the variables within the LSA have a
     * corresponding entry in the matches map.
     */
    protected open fun allocateVars(template: ILsaMolecule): List<IExpression> = template.allocateVar(matches)

    /**
     * Same of [allocateVars], but also builds an [ILsaMolecule].
     */
    protected open fun allocateVarsAndBuildLSA(template: ILsaMolecule): ILsaMolecule =
        LsaMolecule(template.allocateVar(matches))

    abstract override fun cloneOnNodeReaction(newReaction: NodeReaction<List<ILsaMolecule>>): AbstractLsaAction

    /**
     * Returns the value of the argument in position [argNumber] of [mol] (supposed to be a computable expression or
     * a number) as Double, computed using the current matches.
     */
    protected open fun getLSAArgumentAsDouble(mol: ILsaMolecule, argNumber: Int): Double =
        getLSAArgumentAsObject(mol, argNumber) as Double

    /**
     * Returns the value of the argument in position [argNumber] of [mol] (supposed to be a computable expression or
     * a number) as Double, computed using the current matches.
     */
    protected open fun getLSAArgumentAsDouble(mol: List<IExpression>, argNumber: Int): Double =
        getLSAArgumentAsObject(mol, argNumber) as Double

    /**
     * Returns the value of the argument in position [argNumber] of [mol] as Int, computed using the current matches.
     */
    protected open fun getLSAArgumentAsInt(mol: ILsaMolecule, argNumber: Int): Int =
        getLSAArgumentAsDouble(mol, argNumber).toInt()

    /**
     * Returns the value of the argument in position [argNumber] of [mol] as Int, computed using the current matches.
     */
    protected open fun getLSAArgumentAsInt(mol: List<IExpression>, argNumber: Int): Int =
        getLSAArgumentAsDouble(mol, argNumber).toInt()

    /**
     * Returns the value of the argument in position [argNumber] of [mol], computed using the current matches.
     */
    protected open fun getLSAArgumentAsObject(mol: ILsaMolecule, argNumber: Int): Any =
        mol.getArg(argNumber).calculate(matches).getValue(matches)

    /**
     * Returns the value of the argument in position [argNumber] of [mol], computed using the current matches.
     */
    protected open fun getLSAArgumentAsObject(mol: List<IExpression>, argNumber: Int): Any =
        mol[argNumber].calculate(matches).getValue(matches)

    /**
     * Returns the value of the argument in position [argNumber] of [mol] as String,
     * computed using the current matches.
     */
    protected open fun getLSAArgumentAsString(mol: ILsaMolecule, argNumber: Int): String =
        getLSAArgumentAsObject(mol, argNumber).toString()

    /**
     * Returns the value of the argument in position [argNumber] of [mol] as String,
     * computed using the current matches.
     */
    protected open fun getLSAArgumentAsString(mol: List<IExpression>, argNumber: Int): String =
        getLSAArgumentAsObject(mol, argNumber).toString()

    /**
     * Retrieves from the LSA space of [node] the molecules matching [template].
     */
    protected open fun getLSAs(node: ILsaNode, template: ILsaMolecule): List<ILsaMolecule> =
        node.getConcentration(template)

    /**
     * Returns the value associated with [variable].
     */
    protected open fun getMatch(variable: HashString): ITreeNode<*>? = checkNotNull(matches)[variable]

    /**
     * Returns the value associated with [variable].
     */
    protected open fun getMatch(variable: String): ITreeNode<*>? = getMatch(HashString(variable))

    /**
     * Returns the numeric value associated with [variable].
     */
    protected open fun getMatchAsDouble(variable: HashString): Double = (getMatch(variable) as NumTreeNode).data

    /**
     * Returns the numeric value associated with [variable].
     */
    protected open fun getMatchAsDouble(variable: String): Double = (getMatch(variable) as NumTreeNode).data

    /**
     * Returns the String representation of the value associated with [variable].
     */
    protected open fun getMatchAsString(variable: HashString): String = getMatch(variable).toString()

    /**
     * Returns the String representation of the value associated with [variable].
     */
    protected open fun getMatchAsString(variable: String): String = getMatch(variable).toString()

    /**
     * Injects the LSA [molecule], with its variables allocated using the current matches, in [destination].
     */
    protected open fun inject(destination: ILsaNode, molecule: ILsaMolecule) {
        destination.setConcentration(LsaMolecule(molecule.allocateVar(matches)))
    }

    /**
     * Injects [molecule] locally. It must be instanced: no variables, no comparators, no operations of any kind.
     */
    protected open fun injectLocally(molecule: ILsaMolecule) = inject(lsaNode, molecule)

    override fun setExecutionContext(matches: MutableMap<HashString, ITreeNode<*>>?, nodes: MutableList<ILsaNode>?) {
        this.matches = matches
        this.nodes = nodes
    }

    /**
     * Returns a copy of [template] where the argument in position [argNumber] is replaced by [data].
     */
    protected open fun substitute(template: ILsaMolecule, data: Double, argNumber: Int): ILsaMolecule =
        substitute(template, NumTreeNode(data), argNumber)

    /**
     * Returns a copy of [template] where the argument in position [argNumber] is replaced by [data].
     */
    protected open fun substitute(template: ILsaMolecule, data: ITreeNode<*>, argNumber: Int): ILsaMolecule {
        val arguments = template.allocateVar(null)
        arguments.removeAt(argNumber)
        arguments.add(argNumber, Expression(data))
        return LsaMolecule(arguments)
    }

    /**
     * Sets #D to [distance].
     */
    protected open fun setSyntheticD(distance: Double) {
        checkNotNull(matches)[LsaMolecule.SYN_D] = NumTreeNode(distance)
    }

    /**
     * Sets #ROUTE to [distance].
     */
    protected open fun setSyntheticRoute(distance: Double) {
        checkNotNull(matches)[LsaMolecule.SYN_ROUTE] = NumTreeNode(distance)
    }

    /**
     * Sets #NEIG to the identifiers of the nodes in [neighbors].
     */
    protected open fun setSyntheticNeigh(neighbors: Collection<Node<List<ILsaMolecule>>>) {
        val identifiers: MutableSet<ITreeNode<*>> =
            neighbors.mapTo(Sets.newHashSetWithExpectedSize(neighbors.size)) { NumTreeNode(it.id) }
        checkNotNull(matches)[LsaMolecule.SYN_NEIGH] = ListTreeNode(identifiers)
    }

    /**
     * Sets #O to the local node.
     */
    protected open fun setSyntheticO() {
        checkNotNull(matches)[LsaMolecule.SYN_O] = NumTreeNode(targetNode.id)
    }

    abstract override fun toString(): String
}
