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
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.PositionBasedFilter

/**
 * Context for managing node properties in a simulation.
 *
 * @param T The type of molecule concentration.
 * @param P The type of position.
 */
class PropertiesContextImpl<T, P : Position<P>>(override val ctx: DeploymentContext<T, P>) : PropertiesContext<T, P> {
    /**
     * List of property contexts with their associated filters.
     */
    val propertiesCtx: MutableList<
        Pair<
            context(Environment<T, P>, Node<T>)
            PropertyContext<T, P>.() -> Unit,
            PositionBasedFilter<P>?,
            >,
        > = mutableListOf()

    override fun inside(
        filter: PositionBasedFilter<P>,
        block: context(Environment<T, P>, Node<T>) PropertyContext<T, P>.() -> Unit,
    ) {
        propertiesCtx.add(block to filter)
        logger.debug("Adding property for nodes inside filter: {}", filter)
    }

    override fun all(block: context(Environment<T, P>, Node<T>) PropertyContext<T, P>.() -> Unit) {
        propertiesCtx.add(block to null)
        logger.debug("Adding property for all nodes")
    }

    /**
     * Applies configured properties to a node at a specific position.
     *
     * @param node The node to apply properties to.
     * @param position The position of the node.
     */
    fun applyToNode(node: Node<T>, position: P) {
        propertiesCtx.forEach { (propertyCtx, filter) ->
            if (filter == null || filter.contains(position)) {
                val properties = PropertyContextImpl(filter, node)
                    .apply(propertyCtx)
                    .properties
                properties.forEach { property ->
                    logger.debug("Applying property: {} to node: {}", property, node)
                    node.addProperty(property)
                }
            }
        }
    }

    /**
     * Context for configuring properties for a specific node.
     *
     * @param filter Optional position filter.
     * @param node The node to configure properties for.
     */
    inner class PropertyContextImpl(override val filter: PositionBasedFilter<P>?, override val node: Node<T>) :
        PropertyContext<T, P> {
        override val ctx: PropertiesContext<T, P> = this@PropertiesContextImpl

        /**
         * List of properties to add to the node.
         */
        val properties: MutableList<NodeProperty<T>> = mutableListOf()

        override operator fun NodeProperty<T>.unaryPlus() {
            properties.add(this)
        }
    }
}
