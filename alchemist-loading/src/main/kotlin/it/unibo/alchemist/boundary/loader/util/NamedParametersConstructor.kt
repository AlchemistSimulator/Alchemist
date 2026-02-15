/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.loader.util

import kotlin.collections.get
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure
import net.pearx.kasechange.splitToWords
import org.danilopianini.jirf.Factory
import org.slf4j.LoggerFactory

/**
 * A [JVMConstructor] whose parameters are named
 * and hence stored in a [parametersMap]
 * (no pure Java class works with named parameters now, Kotlin-only).
 */
internal class NamedParametersConstructor(type: String, val parametersMap: Map<*, *> = emptyMap<Any?, Any?>()) :
    JVMConstructor(type) {
    private fun List<OrderedParameters>.description() = joinToString(prefix = "\n- ", separator = "\n- ") {
        it.namedParametersDescriptor()
    }

    private inline infix fun Boolean.and(then: () -> Boolean): Boolean = if (this) then() else false

    private fun List<String>.allLowerCase() = map { it.lowercase() }

    private fun String?.couldBeInterpretedAs(name: String?): Boolean =
        equals(name, ignoreCase = true) || this?.splitToWords()?.allLowerCase() == name?.splitToWords()?.allLowerCase()

    override fun <T : Any> parametersFor(target: KClass<T>, factory: Factory): List<*> {
        val providedNames = parametersMap.map { it.key.toString() }
        val singletons = factory.singletonObjects.keys
        val constructorsWithOrderedParameters =
            target.constructors.map { constructor ->
                constructor.valueParameters.filterNot { it.type.jvmErasure.java in singletons }.sortedBy { it.index }
            }
        val usableConstructors: Map<OrderedParameters, Map<String, String>> =
            constructorsWithOrderedParameters
                .mapNotNull { parameters ->
                    if (providedNames.size <= parameters.size) {
                        // Parameter count must be compatible (as many or less parameters provided)
                        val (optional, mandatory) = parameters.partition { it.isOptional }
                        val mandatoryNames = mandatory.map { it.name }
                        val requiredOptionals by lazy {
                            optional
                                .take(
                                    providedNames.size - mandatory.size,
                                ).map { it.name }
                        }

                        fun verifyParameterMatch(matchMethod: (List<String>).(List<String?>) -> Boolean) =
                            providedNames.matchMethod(mandatoryNames) and
                                { providedNames.matchMethod(requiredOptionals) }
                        // Check for exact name match
                        val exactMatch = verifyParameterMatch(List<String>::containsAll)
                        if (exactMatch) {
                            parameters to emptyMap()
                        } else {
                            // Check for similar-enough non-ambiguous matches: kebab-case, snake_case, etc.
                            // convertedNames is a map between the actual parameter name and the provided name
                            val convertedNames =
                                providedNames
                                    .mapNotNull { providedName ->
                                        parameters
                                            .filter { it.name.couldBeInterpretedAs(providedName) }
                                            .takeIf { it.size == 1 }
                                            ?.first()
                                            ?.name
                                            ?.let { it to providedName }
                                    }.toMap()
                            val worksIfNamesAreReplaced =
                                convertedNames.keys.containsAll(mandatoryNames) and {
                                    convertedNames.keys.containsAll(requiredOptionals)
                                }
                            if (worksIfNamesAreReplaced) {
                                parameters to convertedNames.filter { it.key != it.value }
                            } else {
                                null
                            }
                        }
                    } else {
                        null
                    }
                }.toMap()
        // If at least one constructor is a perfect match, discard the ones requiring name replacement.
        val preferredMatch =
            usableConstructors
                .filterValues { replacements -> replacements.isEmpty() }
                .takeIf { it.isNotEmpty() }
                ?: usableConstructors
        require(preferredMatch.isNotEmpty()) {
            """
            No constructor available for ${target.simpleName} with named parameters $providedNames.
            Note: Due to the way Kotlin's @JvmOverloads works, all the optional parameters that precede the ones
            §of interest must be provided.
            Available constructors have the following *named* parameters:
            """.trimIndent().replace(Regex("\\R§"), " ") +
                constructorsWithOrderedParameters.description()
        }
        require(preferredMatch.size == 1) {
            """
            |Ambiguous constructors resolution for ${target.simpleName} with named parameters $providedNames.
            |${ usableConstructors.keys.joinToString("\n|") { "Match: ${it.namedParametersDescriptor()}" } }
            |Available constructors have the following *named* parameters:
            """.trimMargin() + constructorsWithOrderedParameters.description()
        }
        val (selectedConstructor, replacements) = preferredMatch.toList().first()
        if (replacements.isNotEmpty()) {
            logger.warn(
                "Alchemist had to replace some parameter names to match the constructor signature or {}: {}",
                target.simpleName,
                replacements,
            )
        }
        return selectedConstructor
            .filter { parametersMap.containsKey(replacements.getOrDefault(it.name, it.name)) }
            .map { parametersMap[replacements.getOrDefault(it.name, it.name)] }
    }

    private fun Collection<KParameter>.namedParametersDescriptor() = "$size-ary constructor: " +
        filter { it.name != null }.joinToString {
            "${it.name}:${it.type.jvmErasure.simpleName}${if (it.isOptional) "<optional>" else "" }"
        }

    override fun toString(): String = "$typeName($parametersMap)"

    private companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(NamedParametersConstructor::class.java)
    }
}
