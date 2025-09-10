/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.model

import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Deployment
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.PositionBasedFilter
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.TimeDistribution
import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.random.RandomGenerator

class DeploymentsContext<T, P : Position<P>>(ctx: EnvironmentContext<T, P>) {
    var generator: RandomGenerator = MersenneTwister(10)
    val environment: Environment<*, *> = ctx.environment as Environment<*, *>
    val env = ctx.environment
    private val inc = ctx.incarnation

    fun deploy(deployment: Deployment<*>, block: DeploymentContext.() -> Unit) {
        @Suppress("UNCHECKED_CAST")
        addNodes(deployment as Deployment<P>)
        DeploymentContext().apply(block)
    }
    fun deploy(deployment: Deployment<*>) {
        @Suppress("UNCHECKED_CAST")
        this.deploy(deployment) {}
    }
    private fun addNodes(deployment: Deployment<P>) {
        deployment.forEach { position ->
            val node = inc.createNode(
                generator,
                env,
                null,
            )
            env.addNode(node, position)
        }
    }

    inner class DeploymentContext {
        fun all(block: ContentContext.() -> Unit) {
            val c = ContentContext().apply(block)
            applyToNodes(env.nodes, c)
        }
        fun inside(filter: PositionBasedFilter<*>, block: ContentContext.() -> Unit) {
            @Suppress("UNCHECKED_CAST")
            val filter = filter as PositionBasedFilter<P>
            val c = ContentContext().apply(block)
            applyToNodes(
                env.nodes.filter { node ->
                    filter.contains(env.getPosition(node))
                },
                c,
            )
        }
        fun programs(block: ProgramsContext.() -> Unit) {
            ProgramsContext().apply(block)
        }
        private fun applyToNodes(nodes: Collection<Node<T>>, content: ContentContext) {
            nodes.forEach { node ->
                val mol = inc.createMolecule(
                    content.molecule
                        ?: error("Molecule not specified"),
                )
                val conc = inc.createConcentration(content.concentration)
                node.setConcentration(mol, conc)
            }
        }

        inner class ContentContext {
            var molecule: String? = null
            var concentration: T? = null
        }
        inner class ProgramsContext {
            val programs: MutableList<ProgramContext.() -> Unit> = mutableListOf()
            fun all(block: ProgramContext.() -> Unit) {
                programs.add(block)
                applyToNodes(env.nodes, block)
            }
            fun inside(filter: PositionBasedFilter<P>, block: ProgramContext.() -> Unit) {
                programs.add(block)
                applyToNodes(
                    env.nodes.filter { node ->
                        filter.contains(env.getPosition(node))
                    },
                    block,
                )
            }

            private fun applyToNodes(nodes: Collection<Node<T>>, program: ProgramContext.() -> Unit) {
                nodes.forEach { node ->
                    val c = ProgramContext(node).apply(program)
                    val timeDistribution = c.timeDistribution
                        ?: inc.createTimeDistribution(
                            generator,
                            env,
                            node,
                            null,
                        )
                    val r = when {
                        c.reaction != null -> {
                            // User provided a custom reaction object
                            c.reaction!!
                        }
                        else -> {
                            // Create a basic reaction with custom actions/conditions
                            inc.createReaction(
                                generator,
                                env,
                                node,
                                timeDistribution,
                                c.program,
                            )
                        }
                    }
                    r.actions = r.actions + c.actions.map { it() }
                    r.conditions = r.conditions + c.conditions.map { it() }
                    node.addReaction(r)
                }
            }
            inner class ProgramContext(val node: Node<T>) {
                var program: String? = null
                var actions: Collection<() -> Action<T>> = emptyList()
                var conditions: Collection<() -> Condition<T>> = emptyList()
                var timeDistribution: TimeDistribution<T>? = null
                var reaction: Reaction<T>? = null
                fun timeDistribution(td: String) {
                    timeDistribution = inc.createTimeDistribution(
                        generator,
                        env,
                        node,
                        td,
                    )
                }
                fun addAction(block: () -> Action<T>) {
                    actions = actions + block
                }
                fun addCondition(block: () -> Condition<T>) {
                    conditions = conditions + block
                }

                @Suppress("UNCHECKED_CAST")
                operator fun <T> TimeDistribution<*>.unaryPlus(): TimeDistribution<T> = this as TimeDistribution<T>
            }
        }
    }
}
