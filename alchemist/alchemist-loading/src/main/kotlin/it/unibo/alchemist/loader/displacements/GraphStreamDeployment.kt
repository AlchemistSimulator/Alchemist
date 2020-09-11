/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.displacements

import it.unibo.alchemist.loader.GraphStreamSupport
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.LinkingRule
import it.unibo.alchemist.model.interfaces.Position
import org.apache.commons.math3.random.RandomGenerator

/**
 * A deployment based on a [GraphStream](https://graphstream-project.org/) graph.
 */
class GraphStreamDeployment<T, P>(
    private val createLinks: Boolean,
    graphStreamSupport: GraphStreamSupport<T, P>,
) : Displacement<P> by graphStreamSupport.displacement
    where P : Position<out P> {

    /**
     * Builds a new GraphStream-based deployment, given the [nodeCount],
     * whether or not the arcs of such graph shoud be links ([createLinks]),
     * the [generatorName] (must be the name of a subclass of BaseGenerator),
     * and its [parameters].
     */
    @JvmOverloads constructor(
        environment: Environment<T, P>,
        randomGenerator: RandomGenerator,
        nodeCount: Int,
        createLinks: Boolean = true,
        generatorName: String,
        vararg parameters: Any
    ) : this(
        createLinks,
        GraphStreamSupport.generateGraphStream(
            environment,
            nodeCount,
            generatorName,
            uniqueId = randomGenerator.nextLong(),
            parameters = parameters
        )
    )

    val associatedLinkingRule: LinkingRule<T, P>? = if (createLinks) graphStreamSupport.linkingRule else null

}