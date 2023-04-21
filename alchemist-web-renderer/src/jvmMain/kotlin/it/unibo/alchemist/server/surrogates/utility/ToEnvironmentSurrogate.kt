/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.server.surrogates.utility

import it.unibo.alchemist.common.model.surrogate.EnvironmentSurrogate
import it.unibo.alchemist.common.model.surrogate.PositionSurrogate
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.server.surrogates.utility.ToPositionSurrogate.toSuitablePositionSurrogate

/**
 * A function that maps an [Environment] to its surrogate class ([EnvironmentSurrogate]).
 * @param <T> the original type of the concentration.
 * @param <P> the original type of the position.
 * @param <TS> the surrogate type of the concentration.
 * @param <PS> the surrogate type of the position.
 * @param toConcentrationSurrogate the mapping function from <T> to <TS>.
 * @param toPositionSurrogate the mapping function from <P> to <PS>.
 */
fun <T, P, TS, PS> Environment<T, P>.toEnvironmentSurrogate(
    toConcentrationSurrogate: (T) -> TS,
    toPositionSurrogate: (P) -> PS,
): EnvironmentSurrogate<TS, PS>
    where TS : Any, P : Position<out P>, PS : PositionSurrogate =
    EnvironmentSurrogate(
        dimensions,
        nodes.map {
            it.toNodeSurrogate<T, P, TS, PS>(this, toConcentrationSurrogate, toPositionSurrogate)
        },
    )

/**
 * A function that maps an [Environment] to its surrogate class ([EnvironmentSurrogate]). Use the
 * [toSuitablePositionSurrogate] strategy for [PositionSurrogate] mapping.
 * @param <T> the original type of the concentration.
 * @param <P> the original type of the position.
 * @param <TS> the surrogate type of the concentration.
 * @param toConcentrationSurrogate the mapping function from <T> to <TS>.
 */
fun <T, P, TS> Environment<T, P>.toEnvironmentSurrogate(
    toConcentrationSurrogate: (T) -> TS,
): EnvironmentSurrogate<TS, PositionSurrogate>
    where TS : Any, P : Position<out P> =
    toEnvironmentSurrogate(
        toConcentrationSurrogate,
        toSuitablePositionSurrogate(this.dimensions),
    )
