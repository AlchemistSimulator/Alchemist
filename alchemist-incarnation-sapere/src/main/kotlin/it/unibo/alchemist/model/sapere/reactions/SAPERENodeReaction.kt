/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.sapere.reactions

import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.observables.CompositeDisposable
import it.unibo.alchemist.model.reactions.AbstractNodeReaction
import it.unibo.alchemist.model.sapere.ILsaAction
import it.unibo.alchemist.model.sapere.ILsaCondition
import it.unibo.alchemist.model.sapere.ILsaMolecule
import it.unibo.alchemist.model.sapere.ILsaNode
import it.unibo.alchemist.model.sapere.dsl.ITreeNode
import it.unibo.alchemist.model.sapere.dsl.impl.NumTreeNode
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule
import it.unibo.alchemist.model.sapere.timedistributions.SAPEREExponentialTime
import it.unibo.alchemist.model.sapere.timedistributions.SAPERETimeDistribution
import it.unibo.alchemist.model.timedistributions.AbstractDistribution
import it.unibo.alchemist.model.timedistributions.ExponentialTime
import org.apache.commons.math3.random.RandomGenerator
import org.danilopianini.lang.HashString

/**
 * A reaction with LSA concentrations, hosted by [node] in [environment].
 * Its conditions compute the possible matches, one of which is selected and passed to the actions when firing.
 */
class SAPERENodeReaction(
    private val environment: Environment<List<ILsaMolecule>, *>,
    node: ILsaNode,
    private val randomGenerator: RandomGenerator,
    timeDistribution: TimeDistribution<List<ILsaMolecule>>,
) : AbstractNodeReaction<List<ILsaMolecule>>(node, timeDistribution) {
    private val sapereTimeDistribution: SAPERETimeDistribution? = timeDistribution as? SAPERETimeDistribution
    private var possibleMatches: List<MutableMap<HashString, ITreeNode<*>>> = emptyList()
    private var possibleRemove: List<Map<ILsaNode, List<ILsaMolecule>>> = emptyList()
    private var propensities: List<Double> = emptyList()
    private var totalPropensity = 0.0
    private var validNodes: List<ILsaNode> = emptyList()

    private val sapereActions: List<ILsaAction> get() = actions.map { it as ILsaAction }

    private val sapereConditions: List<ILsaCondition> get() = conditions.map { it as ILsaCondition }

    private val numericRate: Boolean get() = sapereTimeDistribution?.isStatic ?: true

    private val baseRate: Double get() = sapereTimeDistribution?.rate ?: super.rate

    override val rate: Double get() = totalPropensity

    override val rateAsString: String
        get() = sapereTimeDistribution
            ?.takeUnless { numericRate }
            ?.rateEquation
            ?.toString()
            ?: baseRate.toString()

    override fun cloneOnNewNode(node: Node<List<ILsaMolecule>>, currentTime: Time): NodeReaction<List<ILsaMolecule>> =
        prepareClone(
            SAPERENodeReaction(environment, node as ILsaNode, randomGenerator, timeDistribution.newInstanceOn(node)),
            currentTime,
        )

    override fun validateConditions(conditions: List<Condition<List<ILsaMolecule>>>) {
        val unsupported = conditions.filterNot { it is ILsaCondition }
        require(unsupported.isEmpty()) { "SAPERE reactions require ILsaCondition instances, got $unsupported" }
    }

    override fun subscribeToSchedulingInputs(subscriptions: CompositeDisposable) {
        sapereConditions.forEach { condition ->
            subscriptions.add(
                condition.matchingInput.subscribe(invokeOnSubscription = false) {
                    schedulingInputChanged()
                },
            )
        }
    }

    override fun performModelMutation() {
        if (possibleMatches.isEmpty()) {
            executeActions(null)
        } else {
            val selectedMatchIndex = selectMatchIndex()
            val matches = possibleMatches[selectedMatchIndex]
            // The matched LSAs must be removed from the local space, unless an action adds them back.
            removeMatchedMolecules(possibleRemove[selectedMatchIndex])
            // #T must be loaded by the reaction, the only structure aware of the time.
            // The other special values (#NEIG, #O, #D) are allocated by the actions.
            matches[LsaMolecule.SYN_T] = NumTreeNode(nextOccurrence.current.toDouble())
            executeActions(matches)
        }
    }

    private fun executeActions(matches: Map<HashString, ITreeNode<*>>?) {
        sapereActions.forEach { action ->
            action.setExecutionContext(matches, validNodes)
            action.execute()
        }
    }

    private fun removeMatchedMolecules(toRemove: Map<ILsaNode, List<ILsaMolecule>>) {
        toRemove.forEach { (node, molecules) -> molecules.forEach(node::removeConcentration) }
    }

    private fun selectMatchIndex(): Int = when {
        // With infinite propensity, the last match added is the one that generated the infinite value.
        totalPropensity == Double.POSITIVE_INFINITY -> possibleMatches.lastIndex
        // With a numeric rate, the choice is just random.
        numericRate -> randomGenerator.nextInt(possibleMatches.size)
        // Otherwise, the matches are selected randomly, weighted by their propensities.
        else -> selectWeightedMatchIndex()
    }

    private fun selectWeightedMatchIndex(): Int {
        val selectedPropensity = randomGenerator.nextDouble() * totalPropensity
        var cumulativePropensity = 0.0
        val selected = propensities.indexOfFirst { propensity ->
            cumulativePropensity += propensity
            cumulativePropensity > selectedPropensity
        }
        // Floating-point rounding can cause selectedPropensity == totalPropensity: fall back to the last bucket.
        return if (selected >= 0) selected else propensities.lastIndex
    }

    override fun onInitializationComplete(atTime: Time, environment: Environment<List<ILsaMolecule>, *>) {
        if (!isNewlyInstantiatedProgram) {
            refreshReactionState(atTime, environment)
            initializeNewProgramScheduling(atTime)
        }
    }

    override fun refreshReactionState(currentTime: Time, environment: Environment<List<ILsaMolecule>, *>) {
        // Valid nodes must be re-initialized at every refresh.
        val nodes = this.environment.getNeighborhood(host).current.neighbors.mapTo(mutableListOf()) { it as ILsaNode }
        validNodes = nodes
        if (conditions.isEmpty()) {
            totalPropensity = baseRate
        } else {
            totalPropensity = 0.0
            propensities = emptyList()
            // The conditions filter these lists in place.
            val matches = mutableListOf<MutableMap<HashString, ITreeNode<*>>>()
            val removals = mutableListOf<MutableMap<ILsaNode, MutableList<ILsaMolecule>>>()
            possibleMatches = matches
            possibleRemove = removals
            // Apply all the conditions as filters: a failing condition leaves no match to evaluate.
            val allValid = sapereConditions.all { it.filter(matches, nodes, removals) }
            if (allValid) {
                computePropensities()
            }
        }
    }

    private fun computePropensities() {
        if (numericRate) {
            totalPropensity = possibleMatches.size * baseRate
        } else {
            val distribution = checkNotNull(sapereTimeDistribution)
            val matchPropensities = mutableListOf<Double>()
            propensities = matchPropensities
            for (match in possibleMatches) {
                distribution.setMatches(match)
                val propensity = distribution.rate
                check(!propensity.isNaN() && propensity >= 0.0) { "Invalid SAPERE propensity for match: $propensity" }
                matchPropensities += propensity
                totalPropensity += propensity
                if (totalPropensity == Double.POSITIVE_INFINITY) {
                    return
                }
            }
        }
    }

    override fun scheduleNextOccurrenceAfterFiring(currentTime: Time) = scheduleFreshOccurrence(currentTime)

    override fun scheduleAfterInvalidation(currentTime: Time) = scheduleFreshOccurrence(currentTime)

    private fun scheduleFreshOccurrence(currentTime: Time) {
        val totalRate = rate
        if (totalRate == 0.0) {
            setNextOccurrence(Time.INFINITY)
            return
        }
        check(!totalRate.isNaN() && totalRate >= 0) { "Invalid SAPERE propensity: total=$totalRate" }
        val distribution = timeDistribution
        /*
         * SAPERE exponential distributions evaluate their rate against the currently installed match.
         * Refreshing the reaction leaves the last match installed, which may have zero propensity even when the total
         * propensity is positive. Install a match with a positive propensity before drawing, otherwise sampling would
         * produce infinity and the subsequent zero scaling would yield NaN.
         */
        if (distribution is SAPEREExponentialTime && !numericRate) {
            distribution.setMatches(possibleMatches[findPositiveMatch()])
        }
        val generatorRate = when (distribution) {
            is SAPEREExponentialTime -> distribution.rate
            is ExponentialTime<*> -> distribution.lambda
            else -> totalRate
        }
        check(!generatorRate.isNaN() && generatorRate >= 0) {
            "Invalid SAPERE propensity: generator=$generatorRate, total=$totalRate"
        }
        val bothInfinite = generatorRate.isInfinite() && totalRate.isInfinite()
        val scaling = if (generatorRate == totalRate || bothInfinite) 1.0 else generatorRate / totalRate
        val sample = validatedSample()
        val schedulingTime = (distribution as? AbstractDistribution<*>)
            ?.startTime
            ?.takeIf { currentTime < it }
            ?: currentTime
        val delay = sample.times(scaling)
        check(delay.isFinite && delay >= Time.ZERO) { "Invalid transformed SAPERE delay: $delay" }
        setNextOccurrence(schedulingTime.plus(delay))
    }

    private fun findPositiveMatch(): Int = propensities.indexOfFirst { it == Double.POSITIVE_INFINITY }
        .takeIf { it >= 0 }
        ?: propensities.indexOfFirst { it > 0 }.takeIf { it >= 0 }
        ?: error("Positive SAPERE propensity without a positive match")
}
