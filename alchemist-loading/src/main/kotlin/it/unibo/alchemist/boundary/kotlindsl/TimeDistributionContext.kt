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
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.TimeDistribution
import org.apache.commons.math3.random.RandomGenerator

/**
 * DSL scope for defining one or more [Reaction]s that share a common [TimeDistribution].
 *
 * This scope is meant to be used via Kotlin context receivers: a [TimeDistribution] (and, for some operations,
 * additional objects such as [Node], [Incarnation], [Environment], and [RandomGenerator]) must be available
 * in the surrounding context.
 *
 * The provided helpers support two common workflows:
 * - registering an already-built [Reaction] on the current [Node];
 * - creating a [Reaction] from an incarnation-specific program descriptor and registering it on the current [Node].
 *
 * In both cases, an optional configuration block can be provided to attach [it.unibo.alchemist.model.Action]s
 * and [it.unibo.alchemist.model.Condition]s via [ActionableContext].
 */
interface TimeDistributionContext<T, P : Position<P>> {

    /**
     * Provides access to the current [TimeDistribution] context receiver.
     *
     * This property mainly exists to allow explicit access to the time distribution from within nested DSL blocks,
     * without having to refer to the context receiver name directly.
     */
    context(timeDistribution: TimeDistribution<T>)
    val timeDistribution: TimeDistribution<T> get() = timeDistribution

    /**
     * Registers an existing [reaction] on the current [Node] and optionally configures it.
     *
     * The [block], if provided, is executed in a scope where:
     * - the [reaction] is available as a context receiver, and
     * - [ActionableContext] is the receiver, enabling the addition of actions and conditions.
     *
     * After the configuration block is executed, the reaction is added to the current node via [Node.addReaction].
     *
     * @param reaction the reaction to configure and register.
     * @param block an optional configuration block for actions and conditions.
     */
    context(node: Node<T>)
    fun <R : Reaction<T>> program(reaction: R, block: context(R) ActionableContext.() -> Unit = { }) {
        context(reaction) {
            ActionableContext.block()
        }
        node.addReaction(reaction)
    }

    /**
     * Creates a [Reaction] from an incarnation-specific program descriptor and registers it on the current [Node].
     *
     * The [program] parameter is forwarded to [Incarnation.createReaction] and its meaning depends on the
     * concrete incarnation in use. A `null` descriptor delegates the choice of the program to the incarnation.
     *
     * The created reaction uses the current [timeDistribution] context receiver, and is created within the
     * current [environment], using the provided [randomGenerator], and targeting the current [node].
     *
     * The optional [block] is applied as described in [program]([Reaction], block) before the reaction is added
     * to the node.
     *
     * @param program the incarnation-specific reaction/program descriptor, possibly `null`.
     * @param block an optional configuration block for actions and conditions.
     */
    context(
        incarnation: Incarnation<T, P>,
        randomGenerator: RandomGenerator,
        environment: Environment<T, P>,
        node: Node<T>,
        timeDistribution: TimeDistribution<T>
    )
    fun program(program: String?, block: context(Reaction<T>) ActionableContext.() -> Unit = { }) = program(
        incarnation.createReaction(randomGenerator, environment, node, timeDistribution, program),
        block,
    )
}
