package it.unibo.alchemist.boundary.dsl.model

import it.unibo.alchemist.boundary.dsl.util.LoadingSystemLogger.logger
import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Condition
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
class ProgramsContext<T, P : Position<P>>(val ctx: DeploymentsContext<T, P>) {
    /**
     * Entry representing a program with its filter.
     *
     * @param filter Optional position filter.
     * @param program The program configuration block.
     */
    inner class ProgramEntry(
        val filter: PositionBasedFilter<P>?,
        val program: ProgramsContext<T, P>.ProgramContext.() -> Unit,
    )

    /**
     * List of program entries.
     */
    val programs: MutableList<ProgramEntry> = mutableListOf()

    /**
     * Configures a program for all nodes.
     *
     * @param block The program configuration block.
     */
    fun all(block: ProgramContext.() -> Unit) {
        logger.debug("Adding program for all nodes")
        programs.add(ProgramEntry(null, block))
    }

    /**
     * Configures a program for nodes inside a filter.
     *
     * @param filter The position filter.
     * @param block The program configuration block.
     */
    fun inside(filter: PositionBasedFilter<P>, block: ProgramContext.() -> Unit) {
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
    fun applyToNodes(node: Node<T>, position: P, program: ProgramContext.() -> Unit, filter: PositionBasedFilter<P>?) {
        logger.debug("Applying program to node at position: {}", position)
        val c = ProgramContext(node, this).apply(program)
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
        val r = c.reaction ?: run {
            // Create a basic reaction with custom actions/conditions
            ctx.ctx.incarnation.createReaction(
                ctx.ctx.simulationGenerator,
                ctx.ctx.environment,
                node,
                timeDistribution,
                c.program,
            )
        }
        logger.debug("Adding actions to reaction")
        r.actions += c.actions.map { it() }
        logger.debug("Adding conditions to reaction")
        r.conditions += c.conditions.map { it() }

        logger.debug("Adding condition to reaction")
        node.addReaction(r)
    }

    /**
     * Context for configuring a single program (reaction).
     *
     * @param node The node this program is associated with.
     * @param ctx The programs context.
     */
    open inner class ProgramContext(val node: Node<T>, val ctx: ProgramsContext<T, P>) {
        /**
         * The program name.
         */
        var program: String? = null

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
        var timeDistribution: TimeDistribution<T>? = null

        /**
         * Optional custom reaction instance.
         */
        var reaction: Reaction<T>? = null

        /**
         * Sets the time distribution using a string specification.
         *
         * @param td The time distribution specification.
         */
        fun timeDistribution(td: String) {
            timeDistribution = ctx.ctx.ctx.incarnation.createTimeDistribution(
                ctx.ctx.ctx.simulationGenerator,
                ctx.ctx.ctx.environment,
                node,
                td,
            )
        }

        /**
         * Adds an action to the program.
         *
         * @param block The action factory.
         */
        fun addAction(block: () -> Action<T>) {
            actions += block
        }

        /**
         * Adds a condition to the program.
         *
         * @param block The condition factory.
         */
        fun addCondition(block: () -> Condition<T>) {
            conditions += block
        }

        /**
         * Unary plus operator for type casting time distributions.
         *
         * @param T The target type.
         * @return The cast time distribution.
         */
        @Suppress("UNCHECKED_CAST")
        operator fun <T> TimeDistribution<*>.unaryPlus(): TimeDistribution<T> = this as TimeDistribution<T>
    }
}
