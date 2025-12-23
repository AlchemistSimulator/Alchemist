/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.model

import it.unibo.alchemist.boundary.dsl.AlchemistDsl
import it.unibo.alchemist.model.Deployment
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.PositionBasedFilter
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
 * @param P The type of position, must extend [Position].
 *
 * @see [SimulationContext.deployments] for configuring deployments in a simulation
 * @see [Deployment] for the deployment interface
 * @see [DeploymentContext] for configuring individual deployments
 */
@AlchemistDsl
interface DeploymentsContext<T, P : Position<P>> {
    /**
     * The simulation context this deployments context belongs to.
     */
    val ctx: SimulationContext<T, P>

    /**
     * The random number generator for scenario generation.
     *
     * Used for random deployments and position perturbations.
     *
     * @see [RandomGenerator]
     */
    val generator: RandomGenerator

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
     * @see [Deployment]
     */
    fun deploy(deployment: Deployment<*>, block: DeploymentContext<T, P>.() -> Unit)

    /**
     * Deploys nodes using a deployment without additional configuration.
     *
     * Nodes are created at the positions defined by the deployment with default settings.
     *
     * @param deployment The deployment that defines node positions.
     * @see [Deployment]
     */
    context(environment: Environment<T, P>)
    fun deploy(
        deployment: Deployment<*>,
    )
}

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
@AlchemistDsl
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
    fun inside(filter: PositionBasedFilter<*>, block: ContentContext<T, P>.() -> Unit)

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
     * Sets a custom node factory for this deployment.
     *
     * By default, nodes are created using the incarnation's node factory.
     * This allows using custom node types.
     *
     * ```kotlin
     * nodes { MyCustomNode() }
     * ```
     *
     * @param factory The factory function for creating nodes.
     * @see [Node]
     * @see [it.unibo.alchemist.model.Incarnation.createNode]
     */
    fun nodes(factory: (DeploymentContext<T, P>) -> Node<T>)

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

/**
 * Context interface for configuring node content (molecules and concentrations).
 *
 * This context is used within [DeploymentContext] blocks to define the initial
 * content of nodes deployed at specific positions.
 *
 * @param T The type of molecule concentration.
 * @param P The type of position, must extend [Position].
 *
 * @see [DeploymentContext] for the parent context
 * @see [it.unibo.alchemist.model.Incarnation.createMolecule]
 * @see [it.unibo.alchemist.model.Incarnation.createConcentration]
 */
@AlchemistDsl
interface ContentContext<T, P : Position<P>> {
    /**
     * The optional position filter applied to this content context.
     *
     * If set, content is only applied to nodes at positions matching this filter.
     */
    val filter: PositionBasedFilter<P>?

    /**
     * The molecule name to inject into nodes.
     *
     * The molecule is created using the incarnation's molecule factory.
     *
     * @see [it.unibo.alchemist.model.Incarnation.createMolecule]
     */
    var molecule: String?

    /**
     * The concentration value for the molecule.
     *
     * The concentration is created using the incarnation's concentration factory.
     *
     * @see [it.unibo.alchemist.model.Incarnation.createConcentration]
     */
    var concentration: T?
}
