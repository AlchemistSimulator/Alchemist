/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.processor.data

import com.google.devtools.ksp.symbol.KSValueParameter

/**
 * Describes the constructor parameters before and after injection filtering.
 *
 * @property allParameters All parameters declared on the constructor.
 * @property remainingParams Parameters the caller still needs to provide.
 * @property paramsToSkip Indexes of injected parameters inside the constructor.
 * @property paramNames Names assigned to the remaining parameters.
 * @property paramTypes Types of the remaining parameters.
 */
data class ConstructorInfo(
    val allParameters: List<KSValueParameter>,
    val remainingParams: List<KSValueParameter>,
    val paramsToSkip: Set<Int>,
    val paramNames: List<String>,
    val paramTypes: List<String>,
)
