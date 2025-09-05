/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.model

import it.unibo.alchemist.model.Deployment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.random.RandomGenerator

class DeploymentsContext<T, P : Position<P>>(private val ctx: EnvironmentContext<T, P>) {
    var deployments: MutableList<Deployment<P>> = mutableListOf()
    var generator: RandomGenerator = MersenneTwister(10)

    fun deploy(deployment: Deployment<*>, block: ContentsContext.() -> Unit) {
        @Suppress("UNCHECKED_CAST")
        deployments.add(deployment as Deployment<P>)
        addNodes(deployment)
        ContentsContext().apply(block)
    }
    private fun addNodes(deployment: Deployment<P>) {
        deployment.forEach { position ->
            val node = ctx.incarnation.createNode(
                generator,
                ctx.environment,
                null,
            )
            ctx.environment.addNode(node, position)
        }
    }

    fun deploy(deployment: Deployment<*>) {
        @Suppress("UNCHECKED_CAST")
        this.deploy(deployment) {}
    }
    inner class ContentsContext {
        fun all(block: ContentContext<T>.() -> Unit) {
            val c = ContentContext<T>().apply(block)
            ctx.environment.nodes.forEach { node ->
                applyToNode(node, c)
            }
        }
        private fun applyToNode(node: Node<T>, content: ContentContext<T>) {
            val mol = ctx.incarnation.createMolecule(
                content.molecule
                    ?: error("Molecule not specified"),
            )
            val conc = ctx.incarnation.createConcentration(content.concentration)
            node.setConcentration(mol, conc)
        }

        inner class ContentContext<T> {
            var molecule: String? = null
            var concentration: T? = null
        }
    }
}
