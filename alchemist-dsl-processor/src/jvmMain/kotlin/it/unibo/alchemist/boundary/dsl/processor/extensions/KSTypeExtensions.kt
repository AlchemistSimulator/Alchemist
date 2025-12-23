/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.processor.extensions

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSType
import it.unibo.alchemist.boundary.dsl.processor.DslBuilderProcessor

context(resolver: Resolver)
internal fun KSType.isInjectable() = DslBuilderProcessor.injectableTypes().any { it.isAssignableFrom(this) }
