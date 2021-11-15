/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader

import it.unibo.alchemist.loader.export.GenericExporter
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position

/**
 * The result of the loading of an [environment] with all the free variables instanced,
 * also providing access to [exporters].
 */
interface InitializedEnvironment<T, P : Position<P>> {

    /**
     * The environment.
     */
    val environment: Environment<T, P>

    /**
     * The data exporters for this environment.
     */
    val exporters: List<GenericExporter<T, P>>
}
