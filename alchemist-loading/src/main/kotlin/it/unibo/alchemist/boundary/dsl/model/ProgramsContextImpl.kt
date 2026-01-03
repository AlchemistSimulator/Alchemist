/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.model

import it.unibo.alchemist.boundary.dsl.util.LoadingSystemLogger.logger
import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.PositionBasedFilter
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.TimeDistribution

/**
 * Context for managing programs (reactions) in a simulation.
 *
 * @param T The type of molecule concentration.
 * @param P The type of position.
 * @param ctx The deployments context.
 */
// TODO: remove when detekt false positive is fixed
@Suppress("UndocumentedPublicFunction") // Detekt false positive with context parameters
class ProgramsContextImpl<T, P : Position<P>>(override val ctx: DeploymentContext<T, P>) : ProgramsContext<T, P> {
    /**
     * Entry representing a program with its filter.
     *
     * @param filter Optional position filter.
     * @param program The program configuration block.
     */
    inner class ProgramEntry(
        val filter: PositionBasedFilter<P>?,
        val program: context(Environment<T, P>, Node<T>) ProgramsContextImpl<T, P>.ProgramContextImpl.() -> Unit,
    )

    /**
     * List of program entries.
     */
    val programs: MutableList<ProgramEntry> = mutableListOf()

    context(_: Environment<T, P>, _: Node<T>)
    override fun all(block: context(Environment<T, P>, Node<T>) ProgramContext<T, P>.() -> Unit) {
        logger.debug("Adding program for all nodes")
        programs.add(ProgramEntry(null, block))
    }

    override fun inside(
        filter: PositionBasedFilter<P>,
        block: context(Environment<T, P>, Node<T>) ProgramContext<T, P>.() -> Unit,
    ) {
        logger.debug("Adding program for nodes inside filter: {}", filter)
        programs.add(ProgramEntry(filter, block))
    }

    /**
     * Applies a program to nodes at a specific position.
     *
     * @param node The node to apply the program to.
     * @param position The position of the node.
     * @param program The program configuration block.
     * @param filter Optional position filter.
     */
    fun applyToNodes(
        node: Node<T>,
        position: P,
        program: context(Environment<T, P>, Node<T>) ProgramContextImpl.() -> Unit,
        filter: PositionBasedFilter<P>?,
    ): Pair<PositionBasedFilter<P>?, Actionable<T>> {
        context(ctx.ctx.ctx.environment, node) {
            logger.debug("Applying program to node at position: {}", position)
            val c = ProgramContextImpl(node).apply { program() }
            val context = ctx.ctx.ctx
            logger.debug("Creating time distribution for program")
            val timeDistribution = c.timeDistribution
                ?: context.incarnation.createTimeDistribution(
                    context.simulationGenerator,
                    context.environment,
                    node,
                    null,
                )
            logger.debug("Creating reaction for program")
            val r = c.reaction
                ?: // Create a basic reaction with custom actions/conditions
                context.incarnation.createReaction(
                    context.simulationGenerator,
                    context.environment,
                    node,
                    timeDistribution,
                    c.program,
                )
            logger.debug("Adding actions to reaction")
            r.actions += c.actions.map { it() }
            logger.debug("Adding conditions to reaction")
            r.conditions += c.conditions.map { it() }
            logger.debug("Adding reaction to node")
            if (filter == null || filter.contains(position)) {
                node.addReaction(r)
            }
            return filter to r
        }
    }

    /**
     * Context for configuring a single program (reaction).
     *
     * @param node The node this program is associated with.
     * @param ctx The programs' context.
     */
    open inner class ProgramContextImpl(override val node: Node<T>) : ProgramContext<T, P> {

        override val ctx: ProgramsContext<T, P> = this@ProgramsContextImpl
        private val context = ctx.ctx.ctx.ctx

        /**
         * The program name.
         */
        override var program: String? = null

        /**
         * Collection of action factories.
         */
        var actions: Collection<() -> Action<T>> = emptyList()

        /**
         * Collection of condition factories.
         */
        var conditions: Collection<() -> Condition<T>> = emptyList()

        /**
         * The time distribution for the reaction.
         */
        override var timeDistribution: TimeDistribution<T>? = null

        /**
         * Optional custom reaction instance.
         */
        override var reaction: Reaction<T>? = null

        override fun timeDistribution(td: String) {
            timeDistribution = context.incarnation.createTimeDistribution(
                context.simulationGenerator,
                context.environment,
                node,
                td,
            )
        }

        override fun addAction(block: () -> Action<T>) {
            actions += block
        }

        override fun addAction(action: String) {
            actions += {
                context.incarnation
                    .createAction(
                        context.simulationGenerator,
                        context.environment,
                        node,
                        timeDistribution,
                        reaction,
                        action,
                    )
            }
        }

        override fun addCondition(block: () -> Condition<T>) {
            conditions += block
        }

        override fun addCondition(condition: String) {
            conditions += {
                context.incarnation.createCondition(
                    context.simulationGenerator,
                    context.environment,
                    node,
                    timeDistribution,
                    reaction,
                    condition,
                )
            }
        }
    }
}
