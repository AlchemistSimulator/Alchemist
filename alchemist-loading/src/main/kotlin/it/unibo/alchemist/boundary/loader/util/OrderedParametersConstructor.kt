/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.loader.util

import kotlin.reflect.KClass
import org.danilopianini.jirf.Factory

/**
 * A [JVMConstructor] whose [parameters] are an ordered list (common case for any JVM language).
 */
internal class OrderedParametersConstructor(type: String, val parameters: List<*> = emptyList<Any?>()) :
    JVMConstructor(type) {
    override fun <T : Any> parametersFor(target: KClass<T>, factory: Factory): List<*> = parameters

    override fun toString(): String = "$typeName${parameters.joinToString(prefix = "(", postfix = ")")}"
}
