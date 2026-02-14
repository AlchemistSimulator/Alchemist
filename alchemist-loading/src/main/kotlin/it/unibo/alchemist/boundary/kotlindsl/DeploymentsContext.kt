/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.kotlindsl

import it.unibo.alchemist.model.Deployment
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import org.apache.commons.math3.random.RandomGenerator

/**
 * DSL scope for instantiating and configuring nodes produced by a [Deployment].
 *
 * A [Deployment] enumerates a set of positions. For each produced position, an implementation of this context is
 * expected to:
 * - create a [Node] (either through a caller-provided factory or via an [Incarnation]);
 * - enter a [DeploymentContext] to configure node contents, reactions, and properties;
 * - (typically) insert the configured node into the current [Environment].
 *
 * This interface is based on Kotlin context receivers: the relevant [RandomGenerator] and [Environment] (and, in the
 * overload that creates nodes via incarnation, also the [Incarnation]) must be available in the surrounding scope.
 */
// TODO: Detekt false positive. Remove once Detekt supports context parameters.
@Suppress("UndocumentedPublicFunction")
fun interface DeploymentsContext<T, P : Position<P>> {

    /**
     * Deploys nodes according to the provided [deployment], creating each node through [nodeFactory] and
     * configuring it through [block].
     *
     * The [block] is invoked once per deployed position, after the node has been created and while a suitable DSL scope
     * is active. Within [block], a [DeploymentContext] is provided as receiver, and the current [RandomGenerator] and
     * the created [Node] are available as context receivers.
     *
     * @param deployment the deployment strategy producing the positions where nodes should be created.
     * @param nodeFactory a factory used to create a node for each deployed position.
     * @param block an optional per-node configuration block.
     */
    context(randomGenerator: RandomGenerator)
    fun deploy(
        deployment: Deployment<P>,
        nodeFactory: (P) -> Node<T>,
        block: context(RandomGenerator, Node<T>) DeploymentContext<T, P>.() -> Unit,
    )

    /**
     * Deploys nodes according to the provided [deployment], creating each node via the current [incarnation] and
     * optionally configuring it through [block].
     *
     * Node creation is delegated to [Incarnation.createNode], using the current [randomGenerator] and [environment].
     * The optional [nodeParameter] is forwarded to the incarnation and its meaning depends on the concrete incarnation.
     *
     * @param deployment the deployment strategy producing the positions where nodes should be created.
     * @param nodeParameter an optional incarnation-specific node descriptor, possibly `null`.
     * @param block an optional per-node configuration block.
     */
    context(incarnation: Incarnation<T, P>, randomGenerator: RandomGenerator, environment: Environment<T, P>)
    fun deploy(
        deployment: Deployment<P>,
        nodeParameter: String? = null,
        block: context(RandomGenerator, Node<T>) DeploymentContext<T, P>.() -> Unit = {},
    ) = deploy(deployment, { incarnation.createNode(randomGenerator, environment, nodeParameter) }, block)
}
