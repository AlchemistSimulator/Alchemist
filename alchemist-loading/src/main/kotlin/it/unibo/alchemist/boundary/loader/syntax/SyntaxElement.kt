/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.loader.syntax

import com.google.gson.GsonBuilder
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties

internal interface SyntaxElement {

    val validKeys: List<String> get() = this::class.declaredMemberProperties
        .filter { it.returnType == String::class.createType() }
        .map { if (it.isConst) it.getter.call() else it.getter.call(this) }
        .map { it.toString() }

    val validDescriptors: Set<ValidDescriptor>

    val guide get() = "Possible configurations are:" +
        validDescriptors.foldIndexed("") { index, previous, element ->
            "$previous\n## Option ${index + 1}:\n$element"
        }

    /**
     * Validates a candidate [descriptor] for a SyntaxElement.
     * If at least one of its [validDescriptors] mandatory keys match,
     * then such a structure is mandated, and an exception is thrown if the syntax is incorrect;
     * otherwise, `true` is returned.
     *
     * If none of the [validDescriptors] match, the function returns `false`.
     */
    fun validateDescriptor(descriptor: Map<*, *>): Boolean {
        val publicKeys = descriptor.keys.asSequence()
            .filterNotNull()
            .map { it.toString() }
            .filterNot { it.startsWith("_") }
            .toSet()
        val problematicSegment by lazy {
            "Problematic segment:\n|" +
                GsonBuilder().setPrettyPrinting().create().toJson(descriptor.mapValues { "..." })
        }
        for (validDescriptor in validDescriptors) {
            val forbidden = validDescriptor.forbiddenKeys.filter { descriptor.containsKey(it) }
            val typeName = this::class.simpleName
            require(forbidden.isEmpty()) {
                """
                |Forbidden keys for $typeName detected: $forbidden.
                |$guide
                |$problematicSegment
                """.trimMargin()
            }
            if (validDescriptor.mandatoryKeys.all { descriptor.containsKey(it) }) {
                val unkownKeys = publicKeys - validDescriptor.mandatoryKeys - validDescriptor.optionalKeys
                require(unkownKeys.isEmpty()) {
                    val matched = descriptor.keys.intersect(validDescriptor.mandatoryKeys)
                    """
                    |Unknown keys $unkownKeys for the provided $typeName descriptor:
                    |$problematicSegment
                    |$typeName syntax was assigned becaused the following mandatory key were detected: $matched.$guide
                    |If you need private keys (e.g. for internal use), prefix them with underscore (_)
                    """.trimMargin()
                }
                return true
            }
        }
        return false
    }

    data class ValidDescriptor(
        val mandatoryKeys: Set<String>,
        val optionalKeys: Set<String> = setOf(),
        val forbiddenKeys: Set<String> = setOf(),
    ) {
        override fun toString(): String {
            fun Set<String>.lines() = joinToString(prefix = "\n  - ", separator = "\n  - ")
            fun Set<String>.describe(name: String) = if (this.isEmpty()) "" else "\n$name keys: ${this.lines()}"
            return mandatoryKeys.describe("required").drop(1) +
                optionalKeys.describe("optional") +
                forbiddenKeys.describe("forbidden")
        }
    }
}
