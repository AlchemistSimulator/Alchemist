/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.reactions

import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.biochemistry.actions.AbstractNeighborAction
import it.unibo.alchemist.model.biochemistry.conditions.AbstractNeighborCondition
import it.unibo.alchemist.model.biochemistry.conditions.BiomolPresentInEnv
import it.unibo.alchemist.model.biochemistry.conditions.EnvPresent
import it.unibo.alchemist.model.biochemistry.conditions.GenericMoleculePresent
import it.unibo.alchemist.model.biochemistry.conditions.GenericMoleculeUnderLevel
import it.unibo.alchemist.model.biochemistry.conditions.TensionPresent
import it.unibo.alchemist.model.biochemistry.util.toMoleculeCount
import it.unibo.alchemist.model.observables.CompositeDisposable
import it.unibo.alchemist.model.reactions.ChemicalNodeReaction
import org.apache.commons.math3.distribution.EnumeratedDistribution
import org.apache.commons.math3.random.RandomGenerator
import org.apache.commons.math3.util.CombinatoricsUtils.binomialCoefficientDouble
import org.apache.commons.math3.util.Pair

/**
 * A memoryless biochemical reaction with an explicit contract for its supported condition semantics.
 * Every factor is validated even when an earlier factor has already made the total rate zero.
 */
class BiochemicalNodeReaction(
    node: Node<Double>,
    timeDistribution: TimeDistribution<Double>,
    private val environment: Environment<Double, *>,
    private val randomGenerator: RandomGenerator,
) : ChemicalNodeReaction<Double>(node, timeDistribution) {

    private var validNeighbors: Map<Node<Double>, Double> = emptyMap()

    private val neighborConditions: List<AbstractNeighborCondition<Double>>
        get() = conditions.filterIsInstance<AbstractNeighborCondition<Double>>()

    override fun cloneOnNewNode(node: Node<Double>, currentTime: Time): BiochemicalNodeReaction = prepareClone(
        BiochemicalNodeReaction(node, timeDistribution.newInstanceOn(node), environment, randomGenerator),
        currentTime,
    )

    override fun validateConditions(conditions: List<Condition<Double>>) {
        val unsupported = conditions.filterNot(::isSupportedCondition)
        require(unsupported.isEmpty()) {
            "Biochemical reactions do not define rate semantics for conditions $unsupported"
        }
        conditions
            .filterIsInstance<GenericMoleculePresent<*>>()
            .filterNot { it is GenericMoleculeUnderLevel<*> }
            .forEach { it.requiredQuantity.toDouble().toMoleculeCount(it) }
    }

    override fun refreshReactionState(currentTime: Time, environment: Environment<Double, *>) {
        validNeighbors = intersectValidNeighbors(neighborConditions)
        super.refreshReactionState(currentTime, environment)
    }

    override fun subscribeToSchedulingInputs(subscriptions: CompositeDisposable) {
        conditions.forEach { condition ->
            val input = when (condition) {
                is GenericMoleculePresent<*> -> condition.quantity
                is AbstractNeighborCondition<*> -> condition.observeValidNeighbors()
                is TensionPresent -> condition.observeMechanicalState()
                is EnvPresent -> condition.isValid()
                else -> error("Unsupported biochemical condition: $condition")
            }
            subscriptions.add(
                input.subscribe(invokeOnSubscription = false) {
                    schedulingInputChanged()
                },
            )
        }
    }

    override fun computeRate(currentTime: Time, environment: Environment<Double, *>): Double =
        conditions.fold(baseRate) { rate, condition ->
            val factor = rateFactor(condition)
            if (rate == 0.0 || factor == 0.0) 0.0 else rate * factor
        }

    override fun performModelMutation() = when {
        neighborConditions.isEmpty() -> super.performModelMutation()
        else -> {
            val candidates = validNeighbors.map { (neighbor, weight) -> Pair(neighbor, weight) }
            val target: Node<Double>? = when (candidates.size) {
                0 -> null
                1 -> candidates.first().key
                else -> EnumeratedDistribution(randomGenerator, candidates).sample()
            }
            actions.forEach { action ->
                if (action is AbstractNeighborAction<Double>) {
                    target?.let(action::execute)
                } else {
                    action.execute()
                }
            }
        }
    }

    private fun isSupportedCondition(condition: Condition<Double>): Boolean = condition is GenericMoleculePresent<*> ||
        condition is AbstractNeighborCondition<*> ||
        condition is EnvPresent ||
        condition is TensionPresent

    private fun rateFactor(condition: Condition<Double>): Double = when (condition) {
        is GenericMoleculeUnderLevel<*> ->
            (condition.requiredQuantity.toDouble() - condition.quantity.current).coerceAtLeast(0.0)
        is BiomolPresentInEnv<*> -> combinations(
            condition.quantity.current.toMoleculeCount(condition),
            condition.requiredQuantity.toMoleculeCount(condition),
        )
        is GenericMoleculePresent<*> -> combinations(
            condition.quantity.current.toMoleculeCount(condition),
            condition.requiredQuantity.toDouble().toMoleculeCount(condition),
        )
        is AbstractNeighborCondition<*> -> condition.getValidNeighbors().values.sum()
        is TensionPresent -> condition.getTension()
        is EnvPresent -> 1.0
        else -> error("Unsupported biochemical condition: $condition")
    }.also { factor ->
        require(!factor.isNaN() && factor >= 0.0) { "Condition $condition produced an invalid rate factor: $factor" }
    }

    private fun intersectValidNeighbors(
        conditions: List<AbstractNeighborCondition<Double>>,
    ): Map<Node<Double>, Double> = conditions
        .map(AbstractNeighborCondition<Double>::getValidNeighbors)
        .reduceOrNull { left, right ->
            left.keys.intersect(right.keys).associateTo(LinkedHashMap()) { node ->
                node to left.getValue(node) * right.getValue(node)
            }
        }.orEmpty()

    private fun combinations(available: Int, required: Int): Double =
        if (required > available) 0.0 else binomialCoefficientDouble(available, required)
}
