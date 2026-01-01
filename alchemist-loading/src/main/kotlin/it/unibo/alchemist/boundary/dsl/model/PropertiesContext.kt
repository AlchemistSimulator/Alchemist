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
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.PositionBasedFilter

/**
 * Context interface for configuring node properties in a deployment.
 *
 * Properties can be assigned to nodes based on their position using filters,
 * or applied to all nodes in the deployment.
 *
 * ## Usage Example
 *
 * ```kotlin
 * deployments {
 *     deploy(deployment) {
 *         properties {
 *             inside(RectangleFilter(-3.0, -3.0, 2.0, 2.0)) {
 *                 add(MyNodeProperty())
 *             }
 *             all {
 *                 add(CommonProperty())
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * @param T The type of molecule concentration.
 * @param P The type of position, must extend [Position].
 *
 * @see [DeploymentContext.properties] for configuring properties in a deployment
 * @see [NodeProperty] for the property interface
 * @see [PositionBasedFilter] for position filtering
 */
// TODO: remove when detekt false positive is fixed
@Suppress("UndocumentedPublicFunction") // Detekt false positive with context parameters
@AlchemistDsl
interface PropertiesContext<T, P : Position<P>> {
    /**
     * The deployment context this properties context belongs to.
     */
    val ctx: DeploymentContext<T, P>

    /**
     * Configures properties for nodes inside a position filter.
     *
     * Only nodes whose positions match the filter will receive the configured properties.
     *
     * @param filter The position filter to apply.
     * @param block The property configuration block.
     * @see [PositionBasedFilter]
     */
    context(environment: Environment<T, P>)
    fun inside(
        filter: PositionBasedFilter<P>,
        block: context(Environment<T, P>, Node<T>) PropertyContext<T, P>.() -> Unit,
    )

    /**
     * Configures properties for all nodes in the deployment.
     *
     * @param block The property configuration block.
     */
    fun all(block: context(Environment<T, P>, Node<T>) PropertyContext<T, P>.() -> Unit)
}

/**
 * Context interface for configuring properties for a specific node.
 *
 * This context is used within [PropertiesContext] blocks to add properties to nodes.
 *
 * @param T The type of molecule concentration.
 * @param P The type of position, must extend [Position].
 *
 * @see [PropertiesContext] for the parent context
 * @see [NodeProperty] for the property interface
 */
@AlchemistDsl
interface PropertyContext<T, P : Position<P>> {
    /**
     * The properties context this property context belongs to.
     */
    val ctx: PropertiesContext<T, P>

    /**
     * The optional position filter applied to this property context.
     */
    val filter: PositionBasedFilter<P>?

    /**
     * The node this property context is configuring.
     */
    val node: Node<T>

    /**
     * Adds a property to the node.
     *
     * @see [NodeProperty]
     */
    operator fun NodeProperty<T>.unaryPlus()
}
