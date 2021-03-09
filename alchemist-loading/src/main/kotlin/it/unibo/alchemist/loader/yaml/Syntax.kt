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

internal interface SyntaxElement {
    val validKeys: List<String> get() = this::class.declaredMemberProperties
        .filter { it.returnType == String::class.createType() }
        .map { if (it.isConst) it.getter.call() else it.getter.call(this) }
        .map { it.toString() }

    fun verifyKeysForElement(descriptor: Map<*, *>) {
        val publicKeys = descriptor.keys.asSequence()
            .filterNotNull()
            .map { it.toString() }
            .filterNot { it.startsWith("_") }
            .toSet()
        val unkownKeys = publicKeys - DocumentRoot.validKeys
        require(unkownKeys.isEmpty()) {
            "There are unknown ${this::class.simpleName} keys: $unkownKeys. " +
                "Allowed root keys: ${
                DocumentRoot.validKeys.joinToString(prefix = "\n\t- ", separator = "\n\t- ", postfix = "\n")
                }" +
                "If you need private keys (e.g. for internal use), prefix them with underscore (_)"
        }
    }
}

internal object DocumentRoot : SyntaxElement {
    object JavaType : SyntaxElement {
        val type by OwnName()
        val parameters by OwnName()
    }
    object DependentVariable : SyntaxElement {
        val language by OwnName()
        val formula by OwnName()
    }
    object Displacement : SyntaxElement {
        val nodes by OwnName()
        val programs by OwnName()
    }
    object Export : SyntaxElement {
        val time by OwnName()
        val molecule by OwnName()
        val property by OwnName()
        val aggregators by OwnName()
        const val valueFilter = "value-filter"
    }
    object Seeds : SyntaxElement {
        val scenario by OwnName()
        val simulation by OwnName()
    }
    val displacements by OwnName()
    val environment by OwnName()
    val export by OwnName()
    val incarnation by OwnName()
    const val linkingRule = "network-model"
    const val remoteDependencies = "remote-dependencies"
    val seeds by OwnName()
    val variables by OwnName()
}

internal class OwnName {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String = property.name.toLowerCase()
}
