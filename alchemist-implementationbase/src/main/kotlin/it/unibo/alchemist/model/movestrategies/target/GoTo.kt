/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.movestrategies.target

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.movestrategies.TargetSelectionStrategy

/**
 * TargetSelectionStrategy that has the objective to go towards a fixed destination in the Environment.
 *
 * @param <T> Concentration type
 * @param <P> position type
 */
data class GoTo<T, P : Position<P>>(
    private val destination: P,
) : TargetSelectionStrategy<T, P> {

    /**
     * @param environment: the environment executing the simulation,
     * @param destination: an indefinite number of [Number] values that indicates the coordinates of the destination.
     * @returns a [TargetSelectionStrategy] which aim to go towards the fixed destination in the environment.
     */
    constructor(
        environment: Environment<T, P>,
        vararg destination: Number,
    ) : this(environment.makePosition(*destination))

    override fun getTarget(): P = destination

    override fun toString() = "${GoTo::class.simpleName}:$destination"
}
