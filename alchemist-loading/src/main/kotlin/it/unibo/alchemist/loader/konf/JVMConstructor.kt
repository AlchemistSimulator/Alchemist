/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.konf

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import it.unibo.alchemist.loader.IllegalAlchemistYAMLException
import org.danilopianini.jirf.Factory
import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.valueParameters

class OrderedParametersConstructor(
    type: String,
    val parameters: List<*> = emptyList<Any?>()
) : JVMConstructor(type) {

    override fun <T : Any> parametersFor(target: KClass<T>): List<*> = parameters

    override fun toString(): String = "$typeName${parameters.joinToString(prefix = "(", postfix = ")")}"
}

class NamedParametersConstructor(
    type: String,
    val parametersMap: Map<*, *> = emptyMap<Any?, Any?>()
) : JVMConstructor(type) {

    override fun <T : Any> parametersFor(target: KClass<T>): List<*> {
        val parameterNames = parametersMap.map { it.key.toString() }
        val availableParameters = target.constructors.map { it.valueParameters }
        val usableParameters = availableParameters
            .filter { constructorParameters ->
                constructorParameters
                    .map { it.name }
                    .filterNotNull()
                    .containsAll(parameterNames)
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

    fun Collection<KParameter>.namedParametersDescriptor() = "${size}-ary constructor: " +
            filter { it.name != null }
                .joinToString {
                    "${it.name}:${it.type}${if (it.isOptional) "<optional>" else "" }"
                }

    override fun toString(): String = "$typeName($parametersMap)"
}

sealed class JVMConstructor(val typeName: String) {

    abstract fun <T : Any> parametersFor(target: KClass<T>): List<*>

    inline fun <reified T : Any> newInstance(jirf: Factory): T = newInstance(T::class, jirf)

    fun <T : Any> newInstance(target: KClass<T>, jirf: Factory): T =
        jirf.build(target.java, parametersFor(target)).let { creationResult ->
            creationResult.createdObject.orElseGet {
                logger.error("Could not create {}, requested as instance of {}", this, target.simpleName)
                val masterException = IllegalArgumentException("Illegal Alchemist specification")
                for ((constructor, exception) in creationResult.exceptions) {
                    masterException.addSuppressed(exception)
                    val errorMessages = generateSequence<Pair<Throwable?, String?>>(
                        exception to exception.message
                    ) { (outer, _) -> outer?.cause to outer?.cause?.message }
                        .takeWhile { it.first != null }
                        .filter { !it.second.isNullOrBlank() }
                        .map { "${it.first!!::class.simpleName}: ${it.second}" }
                        .toList()
                    logger.error(
                        "Constructor {} failed for {} ",
                        constructor,
                        if (errorMessages.isEmpty()) "unknown reasons" else "the following reasons:"
                    )
                    errorMessages.reversed().forEach { logger.error("  - $it") }
                }
                throw masterException
            }
        }

    companion object {

        val logger = LoggerFactory.getLogger(JVMConstructor::class.java)

        @JsonCreator
        @JvmStatic
        fun createConstructor(
            @JsonProperty("type") type: String,
            @JsonProperty("parameters", defaultValue = "[]") parameters: Iterable<*>?
        ) = when (parameters) {
            is List<*> -> OrderedParametersConstructor(type, parameters)
            is Map<*, *> -> NamedParametersConstructor(type, parameters)
            null -> OrderedParametersConstructor(type, emptyList<Any>())
            else -> throw IllegalArgumentException("TODO THIS ERROR")
        }

        fun createConstructor(type: String) = createConstructor(type, null)
    }
}
