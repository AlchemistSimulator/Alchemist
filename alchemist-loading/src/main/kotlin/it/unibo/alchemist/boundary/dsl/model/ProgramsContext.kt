/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.model

import it.unibo.alchemist.boundary.loader.LoadingSystemLogger.logger
import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.PositionBasedFilter
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.TimeDistribution

class ProgramsContext<T, P : Position<P>>(val ctx: DeploymentsContext<T, P>) {
    inner class ProgramEntry(
        val filter: PositionBasedFilter<P>?,
        val program: ProgramsContext<T, P>.ProgramContext.() -> Unit,
    )
    val programs: MutableList<ProgramEntry> = mutableListOf()
    fun all(block: ProgramContext.() -> Unit) {
        logger.debug("Adding program for all nodes")
        programs.add(ProgramEntry(null, block))
    }
    fun inside(filter: PositionBasedFilter<P>, block: ProgramContext.() -> Unit) {
        logger.debug("Adding program for nodes inside filter: {}", filter)
        programs.add(ProgramEntry(filter, block))
    }

    fun applyToNodes(node: Node<T>, position: P, program: ProgramContext.() -> Unit, filter: PositionBasedFilter<P>?) {
        logger.debug("Applying program to node at position: {}", position)
        val c = ProgramContext(node).apply(program)
        if (filter != null && !filter.contains(position)) {
            return
        }
        logger.debug("Creating time distribution for program")
        val timeDistribution = c.timeDistribution
            ?: ctx.ctx.incarnation.createTimeDistribution(
                ctx.ctx.simulationGenerator,
                ctx.ctx.environment,
                node,
                null,
            )
        logger.debug("Creating reaction for program")
        val r = when {
            c.reaction != null -> { // User provided a custom reaction object
                c.reaction!!
            }
            else -> { // Create a basic reaction with custom actions/conditions
                ctx.ctx.incarnation.createReaction(
                    ctx.ctx.simulationGenerator,
                    ctx.ctx.environment,
                    node,
                    timeDistribution,
                    c.program,
                )
            }
        }
        logger.debug("Adding actions to reaction")
        r.actions = r.actions + c.actions.map { it() }
        logger.debug("Adding conditions to reaction")
        r.conditions = r.conditions + c.conditions.map { it() }

        logger.debug("Adding condition to reaction")
        node.addReaction(r)
    }
    inner class ProgramContext(val node: Node<T>) {
        var program: String? = null
        var actions: Collection<() -> Action<T>> = emptyList()
        var conditions: Collection<() -> Condition<T>> = emptyList()
        var timeDistribution: TimeDistribution<T>? = null
        var reaction: Reaction<T>? = null
        fun timeDistribution(td: String) {
            timeDistribution = ctx.ctx.incarnation.createTimeDistribution(
                ctx.ctx.simulationGenerator,
                ctx.ctx.environment,
                node,
                td,
            )
        }
        fun addAction(block: () -> Action<T>) {
            actions = actions + block
        }
        fun addCondition(block: () -> Condition<T>) {
            conditions = conditions + block
        }

        @Suppress("UNCHECKED_CAST")
        operator fun <T> TimeDistribution<*>.unaryPlus(): TimeDistribution<T> = this as TimeDistribution<T>
    }
}
