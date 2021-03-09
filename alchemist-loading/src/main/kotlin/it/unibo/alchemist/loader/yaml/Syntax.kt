/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.yaml

import kotlin.reflect.KProperty
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties

internal object Syntax {
    object JavaType {
        val type by OwnName()
        val parameters by OwnName()
    }
    object DependentVariable {
        val language by OwnName()
        val formula by OwnName()
    }
    object Export {
        val time by OwnName()
        val molecule by OwnName()
        val property by OwnName()
        val aggregators by OwnName()
        const val valueFilter = "value-filter"
    }
    val environment by OwnName()
    val export by OwnName()
    val incarnation by OwnName()
    const val remoteDependencies = "remote-dependencies"
    val variables by OwnName()
    val rootKeys: List<String> =
        Syntax::class.declaredMemberProperties
            .filter { it.returnType == String::class.createType() }
            .map { if (it.isConst) it.getter.call() else it.getter.call(Syntax) }
            .map { it.toString() }
}

internal class OwnName {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String = property.name.toLowerCase()
}
