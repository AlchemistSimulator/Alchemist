/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.kotlindsl

import it.unibo.alchemist.boundary.Loader
import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.positions.Euclidean2DPosition

/**
 * Creates an Alchemist [Loader] using the Kotlin DSL.
 *
 * The returned [Loader] encapsulates the scenario definition provided in [block] and can later be queried
 * (optionally with variable bindings) to obtain a fully configured simulation instance.
 *
 * The [block] is executed with the provided [incarnation] available as a context receiver, and with a
 * [SimulationContext] receiver, enabling the definition of the environment, deployments, exporters/monitors,
 * launcher, and scenario variables.
 *
 * @param T the concentration type used by the simulation.
 * @param P the position type used by the environment.
 * @param I the specific incarnation type.
 * @param incarnation the incarnation used to create domain-specific objects (molecules, nodes, reactions, etc.).
 * @param block the DSL block defining the scenario.
 * @return a [Loader] that can build the simulation described by [block].
 */
fun <T, P : Position<P>, I : Incarnation<T, P>> simulation(
    incarnation: I,
    block: context(I) SimulationContext<T, P>.() -> Unit,
): Loader = DSLLoader(incarnation, block)

/**
 * Convenience overload of [simulation] for scenarios running in a 2D Euclidean space.
 *
 * This function only constrains the position type to [Euclidean2DPosition] and otherwise delegates to [simulation].
 *
 * @param T the concentration type used by the simulation.
 * @param I the specific incarnation type.
 * @param incarnation the incarnation used to create domain-specific objects.
 * @param block the DSL block defining the scenario.
 * @return a [Loader] that can build the simulation described by [block].
 */
fun <T, I : Incarnation<T, Euclidean2DPosition>> simulation2D(
    incarnation: I,
    block: context(I) SimulationContext<T, Euclidean2DPosition>.() -> Unit,
) = simulation(incarnation, block)

/**
 * Convenience overload of [simulation] for scenarios running on geographical coordinates.
 *
 * This function only constrains the position type to [GeoPosition] and otherwise delegates to [simulation].
 *
 * @param T the concentration type used by the simulation.
 * @param I the specific incarnation type.
 * @param incarnation the incarnation used to create domain-specific objects.
 * @param block the DSL block defining the scenario.
 * @return a [Loader] that can build the simulation described by [block].
 */
fun <T, I : Incarnation<T, GeoPosition>> simulationOnMap(
    incarnation: I,
    block: context(I) SimulationContext<T, GeoPosition>.() -> Unit,
) = simulation(incarnation, block)
