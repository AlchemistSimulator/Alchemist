/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary

import it.unibo.alchemist.core.EngineConfiguration
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position

/**
 * Tuple-like implementation of [InitializedEnvironment].
 */
data class EnvironmentWithConfiguration<T, P : Position<P>>(
    override val environment: Environment<T, P>,
    override val exporters: List<Exporter<T, P>>,
    override val engineConfiguration: EngineConfiguration,
) : InitializedEnvironment<T, P>
