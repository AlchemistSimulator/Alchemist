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
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.PositionBasedFilter

class PropertiesContext<T, P : Position<P>> {
    val propertiesCtx: MutableList<Pair<PropertyContext.() -> Unit, PositionBasedFilter<P>?>> = mutableListOf()
    fun inside(filter: PositionBasedFilter<*>, block: PropertyContext.() -> Unit) {
        @Suppress("UNCHECKED_CAST")
        val filter = filter as PositionBasedFilter<P>
        propertiesCtx.add(block to filter)
        logger.debug("Adding property for nodes inside filter: {}", filter)
    }
    fun all(block: PropertyContext.() -> Unit) {
        propertiesCtx.add(block to null)
        logger.debug("Adding property for all nodes")
    }
    fun applyToNode(node: Node<T>, position: P) {
        propertiesCtx.forEach { (propertyCtx, filter) ->
            if (filter == null || filter.contains(position)) {
                val properties = PropertyContext(filter, node)
                    .apply(propertyCtx)
                    .properties
                properties.forEach { property ->
                    logger.debug("Applying property: {} to node: {}", property, node)
                    node.addProperty(property)
                }
            }
        }
    }
    inner class PropertyContext(val filter: PositionBasedFilter<P>?, val node: Node<T>) {
        var properties: MutableList<NodeProperty<T>> = mutableListOf()
        fun add(property: NodeProperty<T>) {
            logger.debug("Adding property: {}", property)
            properties.add(property)
        }
    }
}
