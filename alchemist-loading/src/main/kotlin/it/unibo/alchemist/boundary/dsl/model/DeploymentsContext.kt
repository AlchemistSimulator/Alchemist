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
import it.unibo.alchemist.model.Deployment
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.PositionBasedFilter
import it.unibo.alchemist.model.linkingrules.CombinedLinkingRule
import it.unibo.alchemist.model.linkingrules.NoLinks

class DeploymentsContext<T, P : Position<P>>(val ctx: SimulationContext<T, P>) {
    val environment: Environment<*, *> = ctx.environment as Environment<*, *>
    val env = ctx.environment
    var generator = ctx.scenarioGenerator
    private val inc = ctx.incarnation

    // deployment -> node -> molecule -> concentration -> program( timedistribution -> reaction + actions + conditions)
    fun deploy(deployment: Deployment<*>, block: DeploymentContext.() -> Unit) {
        logger.debug("Deploying deployment: {}", deployment)
        @Suppress("UNCHECKED_CAST")
        val d = DeploymentContext(deployment as Deployment<P>).apply(block)
        // populate
        populateDeployment(d)
    }
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
            val node = if (deploymentContext.nodeFactory == null) {
                inc.createNode(
                    ctx.simulationGenerator,
                    env,
                    null,
                )
            } else {
                deploymentContext.nodeFactory!!.invoke()
            }
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

    inner class DeploymentContext(val deployment: Deployment<P>) {
        val contents: MutableList<ContentContext> = mutableListOf()
        var nodeFactory: (() -> Node<T>)? = null
        var propertiesContext: PropertiesContext<T, P> = PropertiesContext()
        val programsContext: ProgramsContext<T, P> = ProgramsContext(this@DeploymentsContext)
        init {
            logger.debug("Visiting deployment: {}", deployment)
        }
        fun all(block: ContentContext.() -> Unit) {
            logger.debug("Adding content for all positions")
            val c = ContentContext().apply(block)
            contents.add(c)
        }
        fun inside(filter: PositionBasedFilter<*>, block: ContentContext.() -> Unit) {
            @Suppress("UNCHECKED_CAST")
            val filter = filter as PositionBasedFilter<P>
            logger.debug("Adding content for positions inside filter: {}", filter)
            val c = ContentContext(filter).apply(block)
            contents.add(c)
        }
        fun programs(block: ProgramsContext<T, P>.() -> Unit) {
            programsContext.apply(block)
        }
        fun nodes(factory: () -> Node<T>) {
            nodeFactory = factory
        }
        fun properties(block: PropertiesContext<T, P>.() -> Unit) {
            propertiesContext.apply(block)
        }

        // meant to be called from outer class
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

        inner class ContentContext(val filter: PositionBasedFilter<P>? = null) {
            var molecule: String? = null
            var concentration: T? = null
        }
    }
}
