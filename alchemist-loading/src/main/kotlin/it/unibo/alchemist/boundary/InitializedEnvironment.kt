/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position

/**
 * The result of the loading of an [environment] with all the free variables instanced,
 * also providing access to [exporters] and [monitors].
 */
interface InitializedEnvironment<T, P : Position<P>> {

    /**
     * The environment.
     */
    val environment: Environment<T, P>

    /**
     * The data exporters for this environment.
     */
    val exporters: List<Exporter<T, P>>

    /**
     * The monitors for this environment.
     */
    val monitors: List<OutputMonitor<T, P>>
}
