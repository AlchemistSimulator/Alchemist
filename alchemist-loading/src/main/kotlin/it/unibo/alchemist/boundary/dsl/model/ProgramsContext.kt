/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.model

import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.PositionBasedFilter
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.TimeDistribution

/**
 * Context interface for configuring programs (reactions) in a deployment.
 *
 * Programs define the behavior of nodes through reactions that execute actions
 * when conditions are met. Programs can be applied to all nodes or filtered by position.
 *
 * ## Usage Example
 *
 * ```kotlin
 * deployments {
 *     deploy(deployment) {
 *         programs {
 *             all {
 *                 timeDistribution("1")
 *                 program = "{token} --> {firing}"
 *             }
 *             inside(RectangleFilter(-1.0, -1.0, 2.0, 2.0)) {
 *                 program = "{firing} --> +{token}"
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * @param T The type of molecule concentration.
 * @param P The type of position, must extend [Position].
 *
 * @see [DeploymentContext.programs] for configuring programs in a deployment
 * @see [Reaction] for the reaction interface
 * @see [TimeDistribution] for time distribution configuration
 */
@DslMarker
annotation class ProgramsMarker

/**
 * Context interface for configuring programs (reactions) in a deployment.
 *
 * Programs define the behavior of nodes through reactions that execute actions
 * when conditions are met. Programs can be applied to all nodes or filtered by position.
 *
 * @param T The type of molecule concentration.
 * @param P The type of position, must extend [Position].
 */
@ProgramsMarker
interface ProgramsContext<T, P : Position<P>> {
    /**
     * The deployment context this programs context belongs to.
     */
    val ctx: DeploymentContext<T, P>

    /**
     * Configures a program for all nodes in the deployment.
     *
     * @param block The program configuration block.
     */
    fun all(block: ProgramContext<T, P>.() -> Unit)

    /**
     * Configures a program for nodes inside a position filter.
     *
     * Only nodes whose positions match the filter will receive the configured program.
     *
     * @param filter The position filter to apply.
     * @param block The program configuration block.
     * @see [PositionBasedFilter]
     */
    fun inside(filter: PositionBasedFilter<P>, block: ProgramContext<T, P>.() -> Unit)
}

/**
 * Context interface for configuring a single program (reaction) for a node.
 *
 * This context is used within [ProgramsContext] blocks to define reactions with
 * their time distributions, conditions, and actions.
 *
 * @param T The type of molecule concentration.
 * @param P The type of position, must extend [Position].
 *
 * @see [ProgramsContext] for the parent context
 * @see [Reaction] for the reaction interface
 * @see [TimeDistribution] for time distribution
 * @see [Action] for reaction actions
 * @see [Condition] for reaction conditions
 */
@DslMarker
annotation class ProgramMarker

/**
 * Context interface for configuring a single program (reaction) for a node.
 *
 * This context is used within [ProgramsContext] blocks to define reactions with
 * their time distributions, conditions, and actions.
 *
 * @param T The type of molecule concentration.
 * @param P The type of position, must extend [Position].
 */
@ProgramMarker
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
     * @see [TimeDistribution]
     */
    var timeDistribution: TimeDistribution<T>?

    /**
     * An optional custom reaction instance.
     *
     * If provided, this reaction will be used instead of creating one from [program].
     *
     * @see [Reaction]
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
    fun timeDistribution(td: String)

    /**
     * Adds an action to the program.
     *
     * Actions are executed when the reaction fires and all conditions are met.
     *
     * @param block A factory function that creates the action.
     * @see [Action]
     */
    fun addAction(block: () -> Action<T>)

    /**
     * Adds a condition to the program.
     *
     * Conditions must all be satisfied for the reaction to fire.
     *
     * @param block A factory function that creates the condition.
     * @see [Condition]
     */
    fun addCondition(block: () -> Condition<T>)

    /**
     * Unary plus operator for type-safe casting of [TimeDistribution].
     *
     * This operator allows casting a [TimeDistribution] with wildcard type parameter
     * to a specific type parameter, enabling type-safe usage in generic contexts.
     *
     * @return The same [TimeDistribution] instance cast to the specified type parameter.
     */
    @Suppress("UNCHECKED_CAST")
    operator fun <T> TimeDistribution<*>.unaryPlus(): TimeDistribution<T> = this as TimeDistribution<T>
}
