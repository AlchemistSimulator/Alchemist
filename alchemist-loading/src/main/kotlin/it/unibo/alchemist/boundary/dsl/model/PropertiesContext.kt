package it.unibo.alchemist.boundary.dsl.model

import it.unibo.alchemist.boundary.loader.LoadingSystemLogger.logger
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
class PropertiesContext<T, P : Position<P>> {
    /**
     * List of property contexts with their associated filters.
     */
    val propertiesCtx: MutableList<Pair<PropertyContext.() -> Unit, PositionBasedFilter<P>?>> = mutableListOf()

    /**
     * Configures properties for nodes inside a position filter.
     *
     * @param filter The position filter.
     * @param block The property configuration block.
     */
    fun inside(filter: PositionBasedFilter<*>, block: PropertyContext.() -> Unit) {
        @Suppress("UNCHECKED_CAST")
        val typedFilter = filter as PositionBasedFilter<P>
        propertiesCtx.add(block to typedFilter)
        logger.debug("Adding property for nodes inside filter: {}", typedFilter)
    }

    /**
     * Configures properties for all nodes.
     *
     * @param block The property configuration block.
     */
    fun all(block: PropertyContext.() -> Unit) {
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

    /**
     * Context for configuring properties for a specific node.
     *
     * @param filter Optional position filter.
     * @param node The node to configure properties for.
     */
    inner class PropertyContext(val filter: PositionBasedFilter<P>?, val node: Node<T>) {
        /**
         * List of properties to add to the node.
         */
        val properties: MutableList<NodeProperty<T>> = mutableListOf()

        /**
         * Adds a property to the node.
         *
         * @param property The property to add.
         */
        fun add(property: NodeProperty<T>) {
            logger.debug("Adding property: {}", property)
            properties.add(property)
        }
    }
}
