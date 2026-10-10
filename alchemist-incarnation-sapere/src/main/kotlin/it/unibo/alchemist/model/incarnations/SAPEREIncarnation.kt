/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.incarnations

import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.sapere.ILsaMolecule
import it.unibo.alchemist.model.sapere.ILsaNode
import it.unibo.alchemist.model.sapere.actions.LsaAllNeighborsAction
import it.unibo.alchemist.model.sapere.actions.LsaRandomNeighborAction
import it.unibo.alchemist.model.sapere.actions.LsaStandardAction
import it.unibo.alchemist.model.sapere.conditions.LsaNeighborhoodCondition
import it.unibo.alchemist.model.sapere.conditions.LsaStandardCondition
import it.unibo.alchemist.model.sapere.dsl.impl.Type
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule
import it.unibo.alchemist.model.sapere.nodes.LsaNode
import it.unibo.alchemist.model.sapere.reactions.SAPERENodeReaction
import it.unibo.alchemist.model.sapere.timedistributions.SAPEREExponentialTime
import it.unibo.alchemist.model.times.DoubleTime
import org.apache.commons.math3.random.RandomGenerator

/**
 * The SAPERE incarnation, whose concentrations are lists of LSAs.
 *
 * @param P position type
 */
class SAPEREIncarnation<P : Position<out P>> : Incarnation<List<ILsaMolecule>, P> {
    private var saperePropertyNumber = -1
    private var moleculeCache: Molecule? = null
    private var propertyCache: String? = null

    override fun getProperty(node: Node<List<ILsaMolecule>>, molecule: Molecule, property: String?): Double {
        if (molecule is ILsaMolecule && node is ILsaNode && node.contains(molecule)) {
            val cacheUpdated = molecule != moleculeCache || property != propertyCache
            if (cacheUpdated) {
                moleculeCache = molecule
                propertyCache = property
            }
            return sapereProperty(node, molecule, property, cacheUpdated)
        }
        return Double.NaN
    }

    private fun sapereProperty(
        node: ILsaNode,
        molecule: ILsaMolecule,
        property: String?,
        cacheUpdated: Boolean,
    ): Double {
        if (cacheUpdated) {
            saperePropertyNumber = (0 until molecule.argsNumber()).firstOrNull { index ->
                val argument = molecule.getArg(index)
                when (argument.rootNodeType) {
                    Type.COMPARATOR -> argument.leftChildren.toString() == property
                    Type.VAR -> argument.rootNode.toString() == property
                    else -> false
                }
            } ?: -1
        }
        if (saperePropertyNumber >= 0) {
            // Potential concurrency issue: a size check is mandatory.
            val argument = node.getConcentration(molecule).firstOrNull()?.getArg(saperePropertyNumber)
            if (argument?.rootNodeType == Type.NUM) {
                return argument.rootNodeData as Double
            }
        }
        return Double.NaN
    }

    override fun createMolecule(s: String): ILsaMolecule {
        require(s.isNotEmpty()) { "An empty specification is not a valid LSA" }
        return if (s.trim().startsWith("{") &&
            s.endsWith("}")
        ) {
            LsaMolecule(s.substring(1, s.length - 1))
        } else {
            LsaMolecule(s)
        }
    }

    override fun toString(): String = this::class.simpleName.orEmpty()

    override fun createNode(
        randomGenerator: RandomGenerator,
        environment: Environment<List<ILsaMolecule>, P>,
        parameter: Any?,
    ): ILsaNode = LsaNode(environment)

    override fun createTimeDistribution(
        randomGenerator: RandomGenerator,
        environment: Environment<List<ILsaMolecule>, P>,
        node: Node<List<ILsaMolecule>>?,
        parameter: Any?,
    ): TimeDistribution<List<ILsaMolecule>> {
        // Trailing empty arguments are dropped, as in "1," meaning "1".
        val arguments = parameter?.toString()?.takeIf { it.isNotEmpty() }?.split(",")?.dropLastWhile { it.isEmpty() }
        return when (arguments?.size) {
            null, 0 -> defaultTimeDistribution(randomGenerator)
            1 -> SAPEREExponentialTime(arguments[0], randomGenerator)
            2 -> SAPEREExponentialTime(arguments[0], DoubleTime(arguments[1].toDouble()), randomGenerator)
            else -> throw IllegalArgumentException("$parameter could not be used")
        }
    }

    override fun createReaction(
        randomGenerator: RandomGenerator,
        environment: Environment<List<ILsaMolecule>, P>,
        node: Node<List<ILsaMolecule>>,
        timeDistribution: TimeDistribution<List<ILsaMolecule>>,
        parameter: Any?,
    ): NodeReaction<List<ILsaMolecule>> {
        val result = SAPERENodeReaction(environment, node as LsaNode, randomGenerator, timeDistribution)
        val specification = parameter?.toString()?.takeIf { it.isNotEmpty() } ?: return result
        val reactionMatch = MATCH_REACTION.matchEntire(specification)
            ?: illegalSpec("must match regex $REACTION_REGEX", specification)
        val conditionsSpec = reactionMatch.group(CONDITIONS_GROUP)
        if (!CONDITIONS_SEQUENCE.matches(conditionsSpec)) {
            illegalSpec(INVALID_SEQUENCE, conditionsSpec)
        }
        val conditions = MATCH_CONDITION.findAll(conditionsSpec)
            .map { createCondition(randomGenerator, environment, node, result, it.group(CONDITION_GROUP)) }
            .toList()
        val actionsSpec = reactionMatch.group(ACTIONS_GROUP)
        if (!ACTIONS_SEQUENCE.matches(actionsSpec)) {
            illegalSpec(INVALID_SEQUENCE, conditionsSpec)
        }
        val actions = MATCH_ACTION.findAll(actionsSpec)
            .map { createAction(randomGenerator, environment, node, result, it.group(ACTION_GROUP)) }
            .toList()
        result.conditions = conditions
        result.actions = actions
        return result
    }

    override fun createCondition(
        randomGenerator: RandomGenerator,
        environment: Environment<List<ILsaMolecule>, P>,
        node: Node<List<ILsaMolecule>>?,
        reaction: Reaction<List<ILsaMolecule>>,
        additionalParameters: Any?,
    ): Condition<List<ILsaMolecule>> {
        val specification = requireNotNull(additionalParameters) { "The condition can't be null. Reaction:$reaction" }
            .toString()
        return if (specification.startsWith("+")) {
            LsaNeighborhoodCondition(reaction.asNodeReaction(), createMolecule(specification.substring(1)), environment)
        } else {
            LsaStandardCondition(createMolecule(specification), reaction.asNodeReaction())
        }
    }

    override fun createAction(
        randomGenerator: RandomGenerator,
        environment: Environment<List<ILsaMolecule>, P>,
        node: Node<List<ILsaMolecule>>?,
        reaction: Reaction<List<ILsaMolecule>>,
        additionalParameters: Any?,
    ): Action<List<ILsaMolecule>> {
        val specification = requireNotNull(additionalParameters) {
            "The action parameter can't be null. Actionable:$reaction"
        }.toString()
        return when {
            specification.startsWith("+") -> LsaRandomNeighborAction(
                randomGenerator,
                environment,
                reaction.asNodeReaction(),
                createMolecule(specification.substring(1)),
            )
            specification.startsWith("*") -> LsaAllNeighborsAction(
                randomGenerator,
                environment,
                reaction.asNodeReaction(),
                createMolecule(specification.substring(1)),
            )
            else -> LsaStandardAction(randomGenerator, reaction.asNodeReaction(), createMolecule(specification))
        }
    }

    override fun createConcentration(descriptor: Any?): List<ILsaMolecule> = when (descriptor) {
        null -> createConcentration()
        is Collection<*> -> descriptor.map { it as? ILsaMolecule ?: createMolecule(it.toString()) }
        else -> listOf(createMolecule(descriptor.toString()))
    }

    override fun createConcentration(): List<ILsaMolecule> = emptyList()

    private companion object {
        private const val CONDITION_GROUP = "condition"
        private const val CONDITIONS_GROUP = "conditions"
        private const val ACTION_GROUP = "action"
        private const val ACTIONS_GROUP = "actions"
        private const val INVALID_SEQUENCE =
            "not a sequence of valid conditions(curly bracket enclosed LSAs, with optional '+' prefix)"
        private const val MATCH_START = "(?:\\s*(?<"
        private const val MATCH_END = "?\\{[^{}]+?}))"
        private const val CONDITION = "$MATCH_START$CONDITION_GROUP>\\+"
        private const val ACTION = "$MATCH_START$ACTION_GROUP>[+*]"
        private const val SEQUENCE = "$MATCH_END*\\s*"
        private const val CONDITION_SEQUENCE = "$CONDITION$SEQUENCE"
        private const val ACTION_SEQUENCE = "$ACTION$SEQUENCE"
        private const val REACTION_REGEX =
            "(?<$CONDITIONS_GROUP>$CONDITION_SEQUENCE)-->(?<$ACTIONS_GROUP>$ACTION_SEQUENCE)"
        private val MATCH_CONDITION = Regex(CONDITION + MATCH_END)
        private val MATCH_ACTION = Regex(ACTION + MATCH_END)
        private val CONDITIONS_SEQUENCE = Regex(CONDITION_SEQUENCE)
        private val ACTIONS_SEQUENCE = Regex(ACTION_SEQUENCE)
        private val MATCH_REACTION = Regex(REACTION_REGEX)

        private fun MatchResult.group(name: String): String = checkNotNull(groups[name]).value

        private fun defaultTimeDistribution(randomGenerator: RandomGenerator): TimeDistribution<List<ILsaMolecule>> =
            SAPEREExponentialTime("Infinity", randomGenerator)

        private fun illegalSpec(reason: String, origin: String): Nothing = throw IllegalArgumentException(
            "This is not a valid SAPERE reaction: $reason. Problematic specification part: $origin",
        )

        private fun Reaction<List<ILsaMolecule>>.asNodeReaction(): NodeReaction<List<ILsaMolecule>> =
            this as? NodeReaction<List<ILsaMolecule>>
                ?: throw IllegalArgumentException("SAPERE conditions and actions require a node reaction, got $this")
    }
}
