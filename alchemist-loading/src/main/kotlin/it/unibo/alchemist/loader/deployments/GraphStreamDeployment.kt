/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.deployments

import it.unibo.alchemist.loader.GraphStreamSupport
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.LinkingRule
import it.unibo.alchemist.model.interfaces.Position
import org.apache.commons.math3.random.RandomGenerator

/**
 * A deployment based on a [GraphStream](https://graphstream-project.org) graph.
 */
class GraphStreamDeployment<P>(
    private val createLinks: Boolean,
    private val graphStreamSupport: GraphStreamSupport<*, P>,
) : Deployment<P> by graphStreamSupport.deployment
    where P : Position<P> {

    /**
     * Builds a new GraphStream-based deployment, given the [nodeCount],
     * whether or not the arcs of such graph shoud be links ([createLinks]),
     * the [generatorName] (must be the name of a subclass of [org.graphstream.algorithm.generator.BaseGenerator]),
     * and its [parameters].
     */
    @JvmOverloads constructor(
        environment: Environment<*, P>,
        randomGenerator: RandomGenerator,
        nodeCount: Int,
        offsetX: Double = 0.0,
        offsetY: Double = 0.0,
        zoom: Double = 1.0,
        layoutQuality: Double = 1.0,
        createLinks: Boolean = true,
        generatorName: String,
        vararg parameters: Any,
    ) : this(
        createLinks,
        GraphStreamSupport.generateGraphStream(
            environment,
            nodeCount,
            offsetX,
            offsetY,
            zoom = zoom,
            generatorName = generatorName,
            layoutQuality = layoutQuality.coerceIn(0.0..1.0),
            uniqueId = randomGenerator.nextLong(),
            parameters = parameters,
        ),
    )

    /**
     * The [LinkingRule] associated with this [GraphStreamDeployment],
     * or null if the deployment has been created without static linking.
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T> getAssociatedLinkingRule(): LinkingRule<T, P>? =
        if (createLinks) graphStreamSupport.linkingRule as? LinkingRule<T, P> else null
}
