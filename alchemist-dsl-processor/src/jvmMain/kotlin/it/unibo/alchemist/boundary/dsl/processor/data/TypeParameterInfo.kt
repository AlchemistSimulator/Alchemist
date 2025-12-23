/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.processor.data

/**
 * Captures the type parameter names and bounds needed inside the generated helper.
 *
 * @property names Generated type parameter identifiers.
 * @property bounds Bounds for each generated type parameter.
 * @property classTypeParamNames Original class-level type parameter names.
 * @property classTypeParamBounds Original class-level type parameter bounds.
 */
internal data class TypeParameterInfo(
    val names: List<String>,
    val bounds: List<String>,
    val classTypeParamNames: List<String> = names,
    val classTypeParamBounds: List<String> = bounds,
)
