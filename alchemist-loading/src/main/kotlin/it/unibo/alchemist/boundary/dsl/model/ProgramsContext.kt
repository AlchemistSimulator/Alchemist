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
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.PositionBasedFilter
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.TimeDistribution

/**
 * Context interface for configuring programs (reactions) in a deployment.
 *
 * Programs define the behavior of nodes through reactions that execute actions
 * when conditions are met. Programs can be applied to all nodes or filtered by position.
 *
 * ## Usage Example
 *
 * ```kotlin
 * deployments {
 *     deploy(deployment) {
 *         programs {
 *             all {
 *                 timeDistribution("1")
 *                 program = "{token} --> {firing}"
 *             }
 *             inside(RectangleFilter(-1.0, -1.0, 2.0, 2.0)) {
 *                 program = "{firing} --> +{token}"
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * @param T The type of molecule concentration.
 * @param P The type of position, must extend [Position].
 *
 * @see [DeploymentContext.programs] for configuring programs in a deployment
 * @see [Reaction] for the reaction interface
 * @see [TimeDistribution] for time distribution configuration
 */
@AlchemistDsl
// TODO: remove when detekt false positive is fixed
@Suppress("UndocumentedPublicFunction") // Detekt false positive with context parameters
interface ProgramsContext<T, P : Position<P>> {
    /**
     * The deployment context this programs context belongs to.
     */
    val ctx: DeploymentContext<T, P>

    /**
     * Configures a program for all nodes in the deployment.
     *
     * @param block The program configuration block.
     */
    context(_: Environment<T, P>, _: Node<T>)
    fun all(block: context(Environment<T, P>, Node<T>) ProgramContext<T, P>.() -> Unit)

    /**
     * Configures a program for nodes inside a position filter.
     *
     * Only nodes whose positions match the filter will receive the configured program.
     *
     * @param filter The position filter to apply.
     * @param block The program configuration block.
     * @see [PositionBasedFilter]
     */
    fun inside(
        filter: PositionBasedFilter<P>,
        block: context(Environment<T, P>, Node<T>) ProgramContext<T, P>.() -> Unit,
    )
}
