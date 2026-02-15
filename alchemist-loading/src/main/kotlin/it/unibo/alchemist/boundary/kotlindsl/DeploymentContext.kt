/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.kotlindsl

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.TimeDistribution
import org.apache.commons.math3.random.RandomGenerator

/**
 * DSL scope for configuring a single node during a deployment.
 *
 * A [DeploymentContext] is typically entered once per deployed position, and provides:
 * - the target [position] being deployed;
 * - utilities to create and attach reactions (optionally configuring actions/conditions);
 * - utilities to initialize node contents and attach [NodeProperty] instances.
 *
 * Most operations rely on Kotlin context receivers, so the relevant [Incarnation], [Environment],
 * [RandomGenerator], and [Node] instances are expected to be available in the surrounding scope.
 *
 * @param T the concentration type used by the simulation.
 * @param P the position type used by the environment.
 */
interface DeploymentContext<T, P : Position<P>> {

    /**
     * The position where the node is being deployed.
     *
     * This is the position currently produced by the deployment strategy and that will be passed to
     * the environment insertion logic.
     */
    val position: P

    /**
     * Enters a [TimeDistributionContext] bound to the provided [timeDistribution].
     *
     * The [block] is executed with [timeDistribution] as a context receiver, enabling reaction definition
     * utilities that share this distribution.
     *
     * @param timeDistribution the time distribution to use for reactions configured inside [block].
     * @param block the configuration block for defining reactions sharing [timeDistribution].
     */
    fun <TimeDistributionType : TimeDistribution<T>> timeDistribution(
        timeDistribution: TimeDistributionType,
        block: context(TimeDistributionType) TimeDistributionContext<T, P>.() -> Unit,
    ) {
        context(timeDistribution) {
            object : TimeDistributionContext<T, P> { }.block()
        }
    }

    /**
     * Enters a [TimeDistributionContext] using a time distribution derived from [parameter].
     *
     * If [parameter] is already a [TimeDistribution], it is used as-is. Otherwise, a new distribution is created
     * via [Incarnation.createTimeDistribution], passing [parameter] through to the incarnation.
     *
     * @param parameter either a concrete [TimeDistribution] instance, an incarnation-specific descriptor, or `null`.
     * @param block the configuration block for defining reactions sharing the selected time distribution.
     */
    @Suppress("UNCHECKED_CAST")
    context(
        incarnation: Incarnation<T, P>,
        randomGenerator: RandomGenerator,
        environment: Environment<T, P>,
        node: Node<T>
    )
    fun withTimeDistribution(
        parameter: Any? = null,
        block: context(TimeDistribution<T>) TimeDistributionContext<T, P>.() -> Unit,
    ) = timeDistribution(
        parameter as? TimeDistribution<T> ?: makeTimeDistribution(parameter),
        block,
    )

    /**
     * Convenience utility to create and register a reaction on the current node.
     *
     * A time distribution is obtained through [withTimeDistribution], using [timeDistribution] as parameter.
     * The reaction is then created via [Incarnation.createReaction], using [program] as an incarnation-specific
     * descriptor (or `null` to delegate the choice to the incarnation), and finally registered on the node.
     *
     * The optional [block] can be used to attach actions and conditions through [ActionableContext].
     *
     * @param program an incarnation-specific reaction/program descriptor, possibly `null`.
     * @param timeDistribution
     *      either a concrete [TimeDistribution] instance, an incarnation-specific descriptor, or `null`.
     * @param block an optional configuration block for actions and conditions.
     */
    context(
        incarnation: Incarnation<T, P>,
        randomGenerator: RandomGenerator,
        environment: Environment<T, P>,
        node: Node<T>
    )
    fun program(
        program: String? = null,
        timeDistribution: Any? = null,
        block: context(Reaction<T>) ActionableContext.() -> Unit = { },
    ) = withTimeDistribution(timeDistribution) {
        program(program, block)
    }

    /**
     * Configures the initial contents of the current node.
     *
     * The [block] is executed with the current [Incarnation] available as a context receiver and with
     * [ContentContext] as receiver, enabling concise content creation and assignment utilities.
     *
     * @param block the configuration block for node contents.
     */
    context(_: Incarnation<T, P>, _: Node<T>)
    fun contents(block: context(Incarnation<T, P>) ContentContext.() -> Unit) {
        ContentContext.block()
    }

    /**
     * Attaches a [NodeProperty] to the current node.
     *
     * @param property the property instance to add to the node.
     */
    context(node: Node<T>)
    fun nodeProperty(property: NodeProperty<T>) {
        node.addProperty(property)
    }

    private companion object {

        context(
            incarnation: Incarnation<T, P>,
            randomGenerator: RandomGenerator,
            environment: Environment<T, P>,
            node: Node<T>
        )
        private fun <T, P : Position<P>> makeTimeDistribution(parameter: Any? = null): TimeDistribution<T> =
            incarnation.createTimeDistribution(
                randomGenerator,
                environment,
                node,
                parameter,
            )

        context(
            incarnation: Incarnation<T, P>,
            randomGenerator: RandomGenerator,
            environment: Environment<T, P>,
            node: Node<T>,
            timeDistribution: TimeDistribution<T>
        )
        private fun <T, P : Position<P>> makeReaction(descriptor: String?): Reaction<T> = incarnation.createReaction(
            randomGenerator,
            environment,
            node,
            timeDistribution,
            null,
        )
    }
}
