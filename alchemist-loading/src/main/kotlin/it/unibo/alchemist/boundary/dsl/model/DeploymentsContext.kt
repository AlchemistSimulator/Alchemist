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
import it.unibo.alchemist.model.Position
import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.random.RandomGenerator

class DeploymentsContext<T, P : Position<P>>(private val ctx: EnvironmentContext<T, P>) {
    var deployments: MutableList<Deployment<P>> = mutableListOf()
    var generator: RandomGenerator = MersenneTwister(0)

    fun deploy(deployment: Deployment<*>) {
        @Suppress("UNCHECKED_CAST")
        deployments.add(deployment as Deployment<P>)
    }
}
