/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.model

import it.unibo.alchemist.boundary.dsl.util.LoadingSystemLogger.logger
import it.unibo.alchemist.model.Deployment
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.PositionBasedFilter
import it.unibo.alchemist.model.linkingrules.CombinedLinkingRule
import it.unibo.alchemist.model.linkingrules.NoLinks

/**
 * Context for managing deployments in a simulation.
 *
 * @param T The type of molecule concentration.
 * @param P The type of position.
 * @param ctx The simulation context.
 */
open class DeploymentsContext<T, P : Position<P>>(val ctx: SimulationContext<T, P>) {
    /**
     * The environment instance.
     */
    val environment: Environment<*, *> = ctx.environment as Environment<*, *>

    /**
     * The environment instance.
     */
    val env = ctx.environment

    /**
     * The scenario generator.
     */
    var generator = ctx.scenarioGenerator
    private val inc = ctx.incarnation

    /**
     * Deploys a deployment with a configuration block.
     *
     * @param deployment The deployment to configure.
     * @param block The configuration block.
     */
    fun deploy(deployment: Deployment<*>, block: DeploymentContext.() -> Unit) {
        logger.debug("Deploying deployment: {}", deployment)
        @Suppress("UNCHECKED_CAST")
        val d = DeploymentContext(deployment as Deployment<P>).apply(block)
        // populate
        populateDeployment(d)
    }

    /**
     * Deploys a deployment without additional configuration.
     *
     * @param deployment The deployment to deploy.
     */
    fun deploy(deployment: Deployment<*>) {
        @Suppress("UNCHECKED_CAST")
        this.deploy(deployment) {}
    }
    private fun populateDeployment(deploymentContext: DeploymentContext) {
        val deployment = deploymentContext.deployment
        // Additional linking rules
        deployment.getAssociatedLinkingRule<T>()?.let { newLinkingRule ->
            val composedLinkingRule =
                when (val linkingRule = ctx.environment.linkingRule) {
                    is NoLinks -> newLinkingRule
                    is CombinedLinkingRule -> CombinedLinkingRule(linkingRule.subRules + listOf(newLinkingRule))
                    else -> CombinedLinkingRule(listOf(linkingRule, newLinkingRule))
                }
            ctx.environment.linkingRule = composedLinkingRule
        }
        deployment.stream().forEach { position ->
            logger.debug("visiting position: {} for deployment: {}", position, deployment)
            logger.debug("creaing node for deployment: {}", deployment)
            val node = deploymentContext.nodeFactory?.invoke()
                ?: inc.createNode(
                    ctx.simulationGenerator,
                    env,
                    null,
                )
            // load properties
            deploymentContext.propertiesContext.applyToNode(node, position)
            // load contents
            val contents = deploymentContext.contents
            for (content in contents) {
                deploymentContext.applyToNodes(node, position, content)
            }
            // load programs
            val programs = deploymentContext.programsContext.programs
            for (programEntry in programs) {
                deploymentContext.programsContext.applyToNodes(
                    node,
                    position,
                    programEntry.program,
                    programEntry.filter,
                )
            }
            logger.debug("Adding node to environment at position: {}", position)
            env.addNode(node, position)
        }
    }

    /**
     * Context for configuring a single deployment.
     *
     * @param deployment The deployment being configured.
     */
    inner class DeploymentContext(val deployment: Deployment<P>) {
        /**
         * The list of content contexts for this deployment.
         */
        val contents: MutableList<ContentContext> = mutableListOf()

        /**
         * Optional factory for creating custom nodes.
         */
        var nodeFactory: (() -> Node<T>)? = null

        /**
         * The properties context for this deployment.
         */
        var propertiesContext: PropertiesContext<T, P> = PropertiesContext(this@DeploymentsContext)

        /**
         * The programs context for this deployment.
         */
        val programsContext: ProgramsContext<T, P> = ProgramsContext(this@DeploymentsContext)
        init {
            logger.debug("Visiting deployment: {}", deployment)
        }

        /**
         * Configures content for all positions in the deployment.
         *
         * @param block The content configuration block.
         */
        fun all(block: ContentContext.() -> Unit) {
            logger.debug("Adding content for all positions")
            val c = ContentContext().apply(block)
            contents.add(c)
        }

        /**
         * Configures content for positions inside a filter.
         *
         * @param filter The position filter.
         * @param block The content configuration block.
         */
        fun inside(filter: PositionBasedFilter<*>, block: ContentContext.() -> Unit) {
            @Suppress("UNCHECKED_CAST")
            val typedFilter = filter as PositionBasedFilter<P>
            logger.debug("Adding content for positions inside filter: {}", typedFilter)
            val c = ContentContext(typedFilter).apply(block)
            contents.add(c)
        }

        /**
         * Configures programs for this deployment.
         *
         * @param block The programs configuration block.
         */
        fun programs(block: ProgramsContext<T, P>.() -> Unit) {
            programsContext.apply(block)
        }

        /**
         * Sets a custom node factory for this deployment.
         *
         * @param factory The factory function for creating nodes.
         */
        fun nodes(factory: () -> Node<T>) {
            nodeFactory = factory
        }

        /**
         * Configures properties for this deployment.
         *
         * @param block The properties configuration block.
         */
        fun properties(block: PropertiesContext<T, P>.() -> Unit) {
            propertiesContext.apply(block)
        }

        /**
         * Applies content to nodes at a specific position.
         *
         * @param node The node to apply content to.
         * @param position The position of the node.
         * @param content The content context to apply.
         */
        fun applyToNodes(node: Node<T>, position: P, content: ContentContext) {
            logger.debug("Applying node to nodes for position: {}, deployment {}", position, deployment)
            if (content.filter == null || content.filter.contains(position)) {
                logger.debug("Creating molecule for node at position: {}", position)
                val mol = inc.createMolecule(
                    content.molecule
                        ?: error("Molecule not specified"),
                )
                logger.debug("Creating concentration for molecule: {}", mol)
                val conc = inc.createConcentration(content.concentration)
                logger.debug("Setting concentration for molecule: {} to node at position: {}", mol, position)
                node.setConcentration(mol, conc)
            }
        }

        /**
         * Context for configuring content (molecules and concentrations) for nodes.
         *
         * @param filter Optional position filter for applying content.
         */
        inner class ContentContext(val filter: PositionBasedFilter<P>? = null) {
            /**
             * The molecule name.
             */
            var molecule: String? = null

            /**
             * The concentration value.
             */
            var concentration: T? = null
        }
    }
}
