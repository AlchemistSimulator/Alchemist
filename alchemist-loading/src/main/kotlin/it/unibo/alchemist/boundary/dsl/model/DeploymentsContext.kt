/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.model

import it.unibo.alchemist.model.Deployment
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import org.apache.commons.math3.random.RandomGenerator

/**
 * Context interface for managing node deployments in a simulation.
 *
 * Deployments define where nodes are placed in the environment and can be configured
 * with content (molecules and concentrations), programs (reactions), and properties.
 *
 * ## Usage Example
 *
 * ```kotlin
 * simulation(incarnation) {
 *     deployments {
 *         deploy(grid(-5.0, -5.0, 5.0, 5.0, 0.25, 0.25)) {
 *             all {
 *                 molecule = "token"
 *                 concentration = 1.0
 *             }
 *             programs {
 *                 all {
 *                     program = "{token} --> {firing}"
 *                 }
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * @param T The type of molecule concentration.
 * @param P The type of position, must extend [it.unibo.alchemist.model.Position].
 *
 * @see [SimulationContext.deployments] for configuring deployments in a simulation
 * @see [it.unibo.alchemist.model.Deployment] for the deployment interface
 * @see [DeploymentContext] for configuring individual deployments
 */
@Suppress("UndocumentedPublicFunction") // Detekt false positive with context parameters
interface DeploymentsContext<T, P : Position<P>> {
    /**
     * Deploys nodes using a deployment with a configuration block.
     *
     * The configuration block allows setting content, programs, properties, and custom node factories.
     *
     * ```kotlin
     * deploy(grid(-5.0, -5.0, 5.0, 5.0, 0.25, 0.25)) {
     *     all { molecule = "token" }
     * }
     * ```
     *
     * @param deployment The deployment that defines node positions.
     * @param block The configuration block for the deployment.
     * @see [it.unibo.alchemist.model.Deployment]
     */
    // TODO: fix the doc
    context(_: Incarnation<T, P>, environment: Environment<T, P>)
    fun deploy(
        deployment: Deployment<P>,
        nodeFactory: context(RandomGenerator, Environment<T, P>) () -> Node<T> = {
            contextOf<Environment<T, P>>().incarnation.createNode(
                contextOf<RandomGenerator>(),
                contextOf<Environment<T, P>>(),
                null,
            )
        },
        block: context(Environment<T, P>, Node<T>) DeploymentContext<T, P>.() -> Unit = { },
    )
}
