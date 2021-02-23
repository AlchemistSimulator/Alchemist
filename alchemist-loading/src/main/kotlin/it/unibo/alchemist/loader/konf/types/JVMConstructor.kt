/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.konf.types

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import it.unibo.alchemist.ClassPathScanner
import it.unibo.alchemist.loader.konf.EntityMapper.buildAny
import it.unibo.alchemist.loader.konf.KonfBasedLoader
import org.danilopianini.jirf.CreationResult
import org.danilopianini.jirf.Factory
import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

class OrderedParametersConstructor(
    type: String,
    private val parameters: List<*> = emptyList<Any?>()
) : JVMConstructor(type) {

    override fun <T : Any> parametersFor(target: KClass<T>): List<*> = parameters

    override fun toString(): String = "$typeName${parameters.joinToString(prefix = "(", postfix = ")")}"
}

class NamedParametersConstructor(
    type: String,
    private val parametersMap: Map<*, *> = emptyMap<Any?, Any?>()
) : JVMConstructor(type) {

    override fun <T : Any> parametersFor(target: KClass<T>): List<*> {
        val parameterNames = parametersMap.map { it.key.toString() }
        val availableParameters = target.constructors.map { it.valueParameters }
        val usableParameters = availableParameters
            .filter { constructorParameters ->
                constructorParameters.mapNotNull { it.name }.containsAll(parameterNames)
            }
        if (usableParameters.isEmpty()) {
            throw IllegalArgumentException(
                """
                    No constructor available for ${target.simpleName} with named parameters $parameterNames.
                    Available constructors have the following *named* parameters:
                """.trimIndent() +
                    availableParameters.joinToString(prefix = "- ", separator = "\n- ") {
                        it.namedParametersDescriptor()
                    }
            )
        }
        val orderedParameters = usableParameters.map { parameterList ->
                parameterList.sortedBy { it.index }.map { parametersMap[it.name] }.filterNotNull()
            }
            .distinct()
        if (orderedParameters.size > 1) {
            throw IllegalArgumentException(
                """
                    Multiple constructors available for ${target.simpleName} with named parameters $parameterNames.
                    Conflicting name parameters lists: $orderedParameters
                """
            )
        }
        return orderedParameters
    }

    private fun Collection<KParameter>.namedParametersDescriptor() = "${size}-ary constructor: " + filter { it.name != null }
        .joinToString {
            "${it.name}:${it.type}${if (it.isOptional) "<optional>" else "" }"
        }

    override fun toString(): String = "$typeName($parametersMap)"
}

sealed class JVMConstructor(val typeName: String) {

    abstract fun <T : Any> parametersFor(target: KClass<T>): List<*>

    private inline fun <reified T : Any> newInstance(jirf: Factory): T = newInstance(T::class, jirf)

    private fun <T : Any> newInstance(target: KClass<T>, jirf: Factory): T {
        /*
         * preprocess parameters:
         *
         * 1. take all constructors with at least the number of parameters passed
         * 2. align end positions (the former parameters are usually implicit)
         * 3. find the KClass of such parameter
         * 4. find the subclassess of that class, and see if any matches the provided type
         * 5. if so, build the parameter
         */
        val originalParameters = parametersFor(target)
        val compatibleConstructors by lazy {
            target.constructors.filter { it.valueParameters.size >= originalParameters.size }
        }
        val parameters = parametersFor(target).mapIndexed { index, parameter ->
            if (parameter is JVMConstructor) {
                val possibleMappings = compatibleConstructors.flatMap { constructor ->
                    val mappedIndex = constructor.valueParameters.lastIndex - originalParameters.lastIndex + index
                    val potentialType = constructor.valueParameters[mappedIndex]
                    val potentialJavaType = potentialType.type.jvmErasure.java
                    val subtypes = ClassPathScanner.subTypesOf(potentialJavaType) +
                        if (Modifier.isAbstract(potentialJavaType.modifiers)) emptyList() else listOf(potentialJavaType)
                    val compatibleSubtypes = subtypes.filter {
                        typeName == if (parameter.typeName.contains('.')) it.name else it.simpleName
                    }
                    when {
                        compatibleSubtypes.isEmpty() -> {
                            logger.warn(
                                "Constructor {} discarded as parameter #{}:{} has no compatible subtype {} (expected: {})",
                                constructor,
                                mappedIndex,
                                potentialType.type,
                                typeName
                            )
                            emptyList()
                        }
                        compatibleSubtypes.size > 1 -> {
                            throw IllegalStateException(
                                "Ambiguous mapping: $compatibleSubtypes all match the requested type $typeName for " +
                                    "parameter #$mappedIndex:$potentialType of $constructor"
                            )
                        }
                        else -> {
                            listOf(buildAny(compatibleSubtypes.first(), jirf))
                        }
                    }
                }
                when (possibleMappings.size) {
                    0 -> throw IllegalStateException("Could not build paramenter #$index defined as $parameter")
                    1 ->  possibleMappings.first()
                    else -> throw IllegalStateException(
                        "Ambiguous parameter #$index $parameter, multiple options match: $possibleMappings"
                    )
                }
            } else {
                parameter
            }
        }
        val creationResult = jirf.build(target.java, parameters)
        return creationResult.createdObject.orElseGet {
            logger.error("Could not create {}, requested as instance of {}", this, target.simpleName)
            val masterException = IllegalArgumentException("Illegal Alchemist specification")
            creationResult.exceptions.forEach { (_, exception) -> masterException.addSuppressed(exception) }
            creationResult.logErrors { message, arguments -> logger.error(message, *arguments) }
            throw masterException
        }.also {
            creationResult.logErrors { message, arguments -> logger.warn(message, *arguments) }
        }
    }

    inline fun <reified T : Any> buildAny(factory: Factory): T = buildAny(T::class.java, factory)

    fun <T : Any> buildAny(type: Class<out T>, factory: Factory): T {
        val hasPackage = typeName.contains('.')
        val subtypes = ClassPathScanner.subTypesOf(type) +
            if (Modifier.isAbstract(type.modifiers)) emptyList() else listOf(type)
        val perfectMatches = subtypes.filter { typeName == if (hasPackage) it.name else it.simpleName }
        when (perfectMatches.size) {
            0 -> KonfBasedLoader.logger.warn("No perfect match for type {} in {}", typeName, subtypes.map { it.name })
            1 -> return newInstance(perfectMatches.first().kotlin, factory)
            else -> throw IllegalStateException(
                "Multiple perfect matches for $typeName: ${perfectMatches.map { it.name }}"
            )
        }
        val subOptimalMatches = subtypes.filter {
            typeName.equals(if (hasPackage) it.name else it.simpleName, ignoreCase = true)
        }
        return when (subOptimalMatches.size) {
            0 -> throw IllegalStateException(
                """
            No valid match for type $typeName among subtypes of ${type.simpleName}.
            Valid subtypes are: $subtypes
            """.trimMargin()
            )
            1 -> newInstance(subOptimalMatches.first().kotlin, factory)
            else ->  throw IllegalStateException(
                "Multiple matches for $typeName as subtype of ${type.simpleName}: ${perfectMatches.map { it.name }}. " +
                    "Disambiguation is required."
            )
        }
    }


    private fun CreationResult<*>.logErrors(logger: (String, Array<Any?>) -> Unit) {
        for ((constructor, exception) in exceptions) {
            val errorMessages = generateSequence<Pair<Throwable?, String?>>(
                exception to exception.message
            ) { (outer, _) -> outer?.cause to outer?.cause?.message }
                .takeWhile { it.first != null }
                .filter { !it.second.isNullOrBlank() }
                .map { "${it.first!!::class.simpleName}: ${it.second}" }
                .toList()
            logger(
                "Constructor {} failed for {} ",
                arrayOf(
                    constructor,
                    if (errorMessages.isEmpty()) "unknown reasons" else "the following reasons:",
                )
            )
            errorMessages.reversed().forEach { logger("  - $it", emptyArray()) }
        }
    }

    companion object {

        private val logger = LoggerFactory.getLogger(JVMConstructor::class.java)

        @JsonCreator
        @JvmStatic
        fun create(
            @JsonProperty("type") type: Any,
            @JsonProperty("parameters") parameters: Iterable<*>?
        ): JVMConstructor =
            if (type is String) {
                when (parameters) {
                    is List<*> -> OrderedParametersConstructor(type, parameters)
                    is Map<*, *> -> NamedParametersConstructor(type, parameters)
                    null -> OrderedParametersConstructor(type, emptyList<Any>())
                    else -> throw IllegalArgumentException("TODO THIS ERROR")
                }
            } else {
                throw IllegalStateException()
            }

        fun create(type: String) = create(type, null)
    }
}
