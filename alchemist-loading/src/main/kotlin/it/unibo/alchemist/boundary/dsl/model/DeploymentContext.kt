/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.model

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.PositionBasedFilter

/**
 * Context interface for configuring a single deployment.
 *
 * This context allows configuring content (molecules and concentrations), programs (reactions),
 * properties, and custom node factories for nodes deployed at positions defined by the deployment.
 *
 * @param T The type of molecule concentration.
 * @param P The type of position, must extend [Position].
 *
 * @see [DeploymentsContext] for the parent context
 * @see [ContentContext] for configuring node content
 * @see [ProgramsContext] for configuring node programs
 * @see [PropertiesContext] for configuring node properties
 */
// TODO: remove when detekt false positive is fixed
@Suppress("UndocumentedPublicFunction") // Detekt false positive with context parameters
interface DeploymentContext<T, P : Position<P>> {
    /**
     * The deployments context this deployment context belongs to.
     */
    val ctx: DeploymentsContext<T, P>

    /**
     * Configures content (molecules and concentrations) for all positions in the deployment.
     *
     * All nodes deployed at positions defined by this deployment will receive the configured content.
     *
     * ```kotlin
     * all {
     *     molecule = "token"
     *     concentration = 1.0
     * }
     * ```
     *
     * @param block The content configuration block.
     * @see [ContentContext]
     */
    context(_: Node<T>)
    fun all(block: ContentContext<T, P>.() -> Unit)

    /**
     * Configures content for positions inside a filter.
     *
     * Only nodes deployed at positions matching the filter will receive the configured content.
     *
     * ```kotlin
     * inside(RectangleFilter(-1.0, -1.0, 2.0, 2.0)) {
     *     molecule = "specialToken"
     * }
     * ```
     *
     * @param filter The position filter to apply.
     * @param block The content configuration block.
     * @see [PositionBasedFilter]
     * @see [ContentContext]
     */
    context(_: Node<T>)
    fun inside(filter: PositionBasedFilter<*>, block: context(Node<T>) ContentContext<T, P>.() -> Unit)

    /**
     * Configures programs (reactions) for this deployment.
     *
     * Programs define the behavior of nodes through reactions.
     *
     * ```kotlin
     * programs {
     *     all {
     *         program = "{token} --> {firing}"
     *     }
     * }
     * ```
     *
     * @param block The programs configuration block.
     * @see [ProgramsContext]
     */
    fun programs(block: ProgramsContext<T, P>.() -> Unit)

    /**
     * Configures properties for this deployment.
     *
     * Properties can be assigned to nodes based on their position.
     *
     * ```kotlin
     * properties {
     *     inside(RectangleFilter(-3.0, -3.0, 2.0, 2.0)) {
     *         add(MyNodeProperty())
     *     }
     * }
     * ```
     *
     * @param block The properties configuration block.
     * @see [PropertiesContext]
     */
    fun properties(block: PropertiesContext<T, P>.() -> Unit)
}
