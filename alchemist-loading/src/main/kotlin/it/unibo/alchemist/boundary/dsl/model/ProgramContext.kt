/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.model

import it.unibo.alchemist.boundary.dsl.AlchemistDsl
import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.TimeDistribution
import org.apache.commons.math3.random.RandomGenerator

/**
 * Context interface for configuring a single program (reaction) for a node.
 *
 * This context is used within [ProgramsContext] blocks to define reactions with
 * their time distributions, conditions, and actions.
 *
 * @param T The type of molecule concentration.
 * @param P The type of position, must extend [it.unibo.alchemist.model.Position].
 *
 * @see [ProgramsContext] for the parent context
 * @see [it.unibo.alchemist.model.Reaction] for the reaction interface
 * @see [it.unibo.alchemist.model.TimeDistribution] for time distribution
 * @see [it.unibo.alchemist.model.Action] for reaction actions
 * @see [it.unibo.alchemist.model.Condition] for reaction conditions
 */
// TODO: remove when detekt false positive is fixed
@Suppress("UndocumentedPublicFunction") // Detekt false positive with context parameters
@AlchemistDsl
interface ProgramContext<T, P : Position<P>> {
    /**
     * The programs context this program context belongs to.
     */
    val ctx: ProgramsContext<T, P>

    /**
     * The node this program context is configuring.
     */
    val node: Node<T>

    /**
     * The program specification as a string.
     *
     * The format depends on the incarnation being used.
     */
    var program: String?

    /**
     * The time distribution for the reaction.
     *
     * @see [it.unibo.alchemist.model.TimeDistribution]
     */
    var timeDistribution: TimeDistribution<T>?

    /**
     * An optional custom reaction instance.
     *
     * If provided, this reaction will be used instead of creating one from [program].
     *
     * @see [it.unibo.alchemist.model.Reaction]
     */
    var reaction: Reaction<T>?

    /**
     * Sets the time distribution using a string specification.
     *
     * The string is processed by the incarnation to create a [TimeDistribution].
     *
     * ```kotlin
     * timeDistribution("1")
     * ```
     *
     * @param td The time distribution specification string.
     * @see [TimeDistribution]
     * @see [it.unibo.alchemist.model.Incarnation.createTimeDistribution]
     */
    context(_: Incarnation<T, P>, _: RandomGenerator, _: Environment<T, P>, _: Node<T>)
    fun timeDistribution(td: String)

    /**
     * Adds an action to the program.
     *
     * Actions are executed when the reaction fires and all conditions are met.
     *
     * @param block A factory function that creates the action.
     * @see [it.unibo.alchemist.model.Action]
     */
    fun addAction(block: () -> Action<T>)

    /**
     * Adds an action to the program using the incarnation createAction function.
     *
     * @param action the action
     * @see [it.unibo.alchemist.model.Incarnation.createAction]
     */
    context(_: Incarnation<T, P>, _: RandomGenerator, _: Environment<T, P>, _: Node<T>, _: Actionable<T>)
    fun addAction(action: String) = addAction {
        contextOf<Incarnation<T, P>>().createAction(
            contextOf<RandomGenerator>(),
            contextOf<Environment<T, P>>(),
            contextOf<Node<T>>(),
            contextOf<Actionable<T>>(),
            action,
        )
    }

    /**
     * Adds a condition to the program.
     *
     * Conditions must all be satisfied for the reaction to fire.
     *
     * @param block A factory function that creates the condition.
     * @see [it.unibo.alchemist.model.Condition]
     */
    fun addCondition(block: () -> Condition<T>)

    /**
     * Adds a condition to the program, using the incarnation createCondition function.
     *
     * @param condition the condition
     * @see [Incarnation.createCondition]
     */
    context(_: Incarnation<T, P>, _: RandomGenerator, _: Environment<T, P>, _: Node<T>, _: Actionable<T>)
    fun addCondition(condition: String) = addCondition {
        contextOf<Incarnation<T, P>>().createCondition(
            contextOf<RandomGenerator>(),
            contextOf<Environment<T, P>>(),
            contextOf<Node<T>>(),
            contextOf<Actionable<T>>(),
            condition,
        )
    }
}
