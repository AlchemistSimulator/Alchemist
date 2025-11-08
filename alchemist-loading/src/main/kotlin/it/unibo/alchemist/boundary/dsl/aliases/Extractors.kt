/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.aliases

import it.unibo.alchemist.boundary.extractors.Time

/**
 * Helper to disambiguate Time() in scripts: resolves to the extractor, not the model type.
 */
fun Time(precision: Int? = null): Time = Time(precision)
