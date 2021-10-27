/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader

import it.unibo.alchemist.loader.export.Extractor
import it.unibo.alchemist.loader.export.GenericExporter
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position

/**
 * Pair-like implementation of [InitializedEnvironment].
 */
data class EnvironmentAndExports<T, P : Position<P>>(
    override val environment: Environment<T, P>,

    override val  exporters: Set<GenericExporter>
   // override val dataExtractors: List<Extractor>,
) : InitializedEnvironment<T, P>
