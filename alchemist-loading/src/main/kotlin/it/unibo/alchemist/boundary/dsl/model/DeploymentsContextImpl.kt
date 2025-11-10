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
open class DeploymentsContextImpl<T, P : Position<P>>(override val ctx: SimulationContext<T, P>) :
    DeploymentsContext<T, P> {
    /**
     * The environment instance.
     */
    override val envAsAny: Environment<*, *> = ctx.environment as Environment<*, *>

    /**
     * The environment instance.
     */
    override val env = ctx.environment

    /**
     * The scenario generator.
     */
    override val generator = ctx.scenarioGenerator
    private val inc = ctx.incarnation

    override fun deploy(deployment: Deployment<*>, block: DeploymentContext<T, P>.() -> Unit) {
        logger.debug("Deploying deployment: {}", deployment)
        @Suppress("UNCHECKED_CAST")
        val d = DeploymentContextImpl(deployment as Deployment<P>).apply(block)
        // populate
        populateDeployment(d)
    }

    override fun deploy(deployment: Deployment<*>) {
        @Suppress("UNCHECKED_CAST")
        this.deploy(deployment) {}
    }
    private fun populateDeployment(deploymentContext: DeploymentContextImpl) {
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
    inner class DeploymentContextImpl(val deployment: Deployment<P>) : DeploymentContext<T, P> {
        override val ctx: DeploymentsContext<T, P> = this@DeploymentsContextImpl

        /**
         * The list of content contexts for this deployment.
         */
        val contents: MutableList<ContentContextImpl> = mutableListOf()

        /**
         * Optional factory for creating custom nodes.
         */
        var nodeFactory: (() -> Node<T>)? = null

        /**
         * The properties context for this deployment.
         */
        var propertiesContext: PropertiesContextImpl<T, P> = PropertiesContextImpl(this@DeploymentContextImpl)

        /**
         * The programs context for this deployment.
         */
        val programsContext: ProgramsContextImpl<T, P> = ProgramsContextImpl(this@DeploymentContextImpl)
        init {
            logger.debug("Visiting deployment: {}", deployment)
        }

        override fun all(block: ContentContext<T, P>.() -> Unit) {
            logger.debug("Adding content for all positions")
            val c = ContentContextImpl().apply(block)
            contents.add(c)
        }

        override fun inside(filter: PositionBasedFilter<*>, block: ContentContext<T, P>.() -> Unit) {
            @Suppress("UNCHECKED_CAST")
            val typedFilter = filter as PositionBasedFilter<P>
            logger.debug("Adding content for positions inside filter: {}", typedFilter)
            val c = ContentContextImpl(typedFilter).apply(block)
            contents.add(c)
        }

        override fun programs(block: ProgramsContext<T, P>.() -> Unit) {
            programsContext.apply(block)
        }

        override fun nodes(factory: () -> Node<T>) {
            nodeFactory = factory
        }

        override fun properties(block: PropertiesContext<T, P>.() -> Unit) {
            propertiesContext.apply(block)
        }

        /**
         * Applies content to nodes at a specific position.
         *
         * @param node The node to apply content to.
         * @param position The position of the node.
         * @param content The content context to apply.
         */
        fun applyToNodes(node: Node<T>, position: P, content: ContentContextImpl) {
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
        inner class ContentContextImpl(override val filter: PositionBasedFilter<P>? = null) : ContentContext<T, P> {
            /**
             * The molecule name.
             */
            override var molecule: String? = null

            /**
             * The concentration value.
             */
            override var concentration: T? = null
        }
    }
}
