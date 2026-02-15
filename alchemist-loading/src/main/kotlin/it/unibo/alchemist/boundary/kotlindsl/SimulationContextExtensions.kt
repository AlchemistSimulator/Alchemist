/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

// TODO: Detekt false positive. Remove once Detekt supports context parameters.
@file:Suppress("UndocumentedPublicFunction")

package it.unibo.alchemist.boundary.kotlindsl

import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.environments.continuous2DEnvironment
import it.unibo.alchemist.model.positions.Euclidean2DPosition

/**
 * Convenience overload that installs a default [Continuous2DEnvironment] and configures it via [block].
 *
 * This function is available only when an [Incarnation] for [Euclidean2DPosition] is in scope, and it delegates to
 * [SimulationContext.environment] by creating a new environment through [continuous2DEnvironment].
 *
 * @param block the environment configuration block executed with the created environment as a context receiver.
 */
context(_: Incarnation<T, Euclidean2DPosition>)
fun <T> SimulationContext<T, Euclidean2DPosition>.environment(
    block: context(Continuous2DEnvironment<T>) EnvironmentContext<T, Euclidean2DPosition>.() -> Unit,
) = environment(continuous2DEnvironment(), block)
