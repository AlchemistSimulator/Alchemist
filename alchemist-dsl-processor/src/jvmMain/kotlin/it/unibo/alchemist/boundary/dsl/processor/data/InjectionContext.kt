/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.processor.data

import it.unibo.alchemist.boundary.dsl.processor.ContextType

/**
 * Represents the injected values and annotation-driven flags available during generation.
 *
 * @property indices Mapping of injection types to constructor indexes.
 * @property paramNames Local names allocated to injected context parameters.
 * @property paramTypes Types assigned to injected context parameters.
 * @property annotationValues Extracted values from the `@BuildDsl` annotation.
 * @property contextType Chosen context enum describing the current execution environment.
 * @property hasContextParams Whether the helper defines context receivers.
 * @property contextParamName Name of the context parameter used by accessors.
 */
data class InjectionContext(
    val indices: Map<InjectionType, Int>,
    val paramNames: Map<InjectionType, String>,
    val paramTypes: Map<InjectionType, String>,
    val annotationValues: Map<String, Any?>,
    val contextType: ContextType,
    val hasContextParams: Boolean = false,
    val contextParamName: String = "ctx",
)
