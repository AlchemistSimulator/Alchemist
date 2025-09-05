/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.model

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.LinkingRule
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.linkingrules.NoLinks

class EnvironmentContext<T, P : Position<P>>(private val ctx: SimulationContext, env: Environment<T, P>) {
    val environment: Environment<T, P> = env
    private var _networkModel: LinkingRule<T, P> = NoLinks()
    var networkModel: LinkingRule<T, P>
        get() = _networkModel
        set(value) {
            _networkModel = value
            environment.linkingRule = value
        }

    val ctxDeploy: DeploymentsContext<T, P> = DeploymentsContext(this)

    val incarnation: Incarnation<T, P>
        get() = SupportedIncarnations.get<T, P>(ctx.incarnation.name).get()

    fun deployments(block: DeploymentsContext<T, P>.() -> Unit) {
        ctxDeploy.apply(block)
    }
}
