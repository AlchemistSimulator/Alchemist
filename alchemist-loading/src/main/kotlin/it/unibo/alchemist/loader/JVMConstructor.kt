/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader

import it.unibo.alchemist.ClassPathScanner
import org.danilopianini.jirf.CreationResult
import org.danilopianini.jirf.Factory
import org.danilopianini.jirf.InstancingImpossibleException
import org.slf4j.LoggerFactory
import java.lang.reflect.Constructor
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

/**
 * A [JVMConstructor] whose [parameters] are an ordered list (common case for any JVM language).
 */
class OrderedParametersConstructor(
    type: String,
    private val parameters: List<*> = emptyList<Any?>()
) : JVMConstructor(type) {

    override fun <T : Any> parametersFor(target: KClass<T>): List<*> = parameters

    override fun toString(): String = "$typeName${parameters.joinToString(prefix = "(", postfix = ")")}"
}

/**
 * A [JVMConstructor] whose parameters are named
 * and hence stored in a [parametersMap]
 * (no pure Java class works with named parameters now, Kotlin-only).
 */
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
        }.distinct()
        if (orderedParameters.size > 1) {
            throw IllegalArgumentException(
                """
                    Multiple constructors available for ${target.simpleName} with named parameters $parameterNames.
                    Conflicting name parameters lists: $orderedParameters
                """
            )
        }
        return orderedParameters.first()
    }

    private fun Collection<KParameter>.namedParametersDescriptor() = "$size-ary constructor: " +
        filter { it.name != null }.joinToString { "${it.name}:${it.type}${if (it.isOptional) "<optional>" else "" }" }

    override fun toString(): String = "$typeName($parametersMap)"
}

/**
 * A constructor for a JVM class of type [typeName].
 */
sealed class JVMConstructor(val typeName: String) {

    /**
     * provided a [target] class, extracts the parameters as an ordered list.
     */
    abstract fun <T : Any> parametersFor(target: KClass<T>): List<*>

    /**
     * Provided a JIRF [factory], builds an instance of the requested type [T] or fails gracefully,
     * returning a [Result<T>].
     */
    inline fun <reified T : Any> buildAny(factory: Factory): Result<T> = buildAny(T::class.java, factory)

    /**
     * Provided a JIRF [factory], builds an instance of the requested [type] T or fails gracefully,
     * returning a [Result<T>].
     */
    fun <T : Any> buildAny(type: Class<out T>, factory: Factory): Result<T> {
        val hasPackage = typeName.contains('.')
        val subtypes = ClassPathScanner.subTypesOf(type) +
            if (Modifier.isAbstract(type.modifiers)) emptyList() else listOf(type)
        val perfectMatches = subtypes.filter { typeName == if (hasPackage) it.name else it.simpleName }
        return when (perfectMatches.size) {
            0 -> {
                val subOptimalMatches = subtypes.filter {
                    typeName.equals(if (hasPackage) it.name else it.simpleName, ignoreCase = true)
                }
                when (subOptimalMatches.size) {
                    0 -> Result.failure(
                        IllegalStateException(
                            """
                            |No valid match for type $typeName among subtypes of ${type.simpleName}.
                            |Valid subtypes are: ${subtypes.map { it.simpleName }}
                            """.trimMargin()
                        )
                    )
                    1 -> {
                        logger.warn(
                            "{} has been selected even though it is not a perfect match for {}",
                            subOptimalMatches.first().name,
                            typeName
                        )
                        Result.success(newInstance(subOptimalMatches.first().kotlin, factory))
                    }
                    else -> Result.failure(
                        IllegalStateException(
                            "Multiple matches for $typeName as subtype of ${type.simpleName}: " +
                                "${perfectMatches.map { it.name }}. Disambiguation is required."
                        )
                    )
                }
            }
            1 -> runCatching { newInstance(perfectMatches.first().kotlin, factory) }
            else -> Result.failure(
                IllegalStateException("Multiple perfect matches for $typeName: ${perfectMatches.map { it.name }}")
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
                    constructor.shorterToString(),
                    if (errorMessages.isEmpty()) "unknown reasons" else "the following reasons:",
                )
            )
            errorMessages.reversed().forEach { logger("  - $it", emptyArray()) }
        }
    }

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
        logger.debug("Building a {} with {}", target.simpleName, originalParameters)
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
                                "Constructor {} discarded as {} is incompatible with parameter #{}:{}",
                                constructor,
                                parameter,
                                mappedIndex,
                                potentialType.type,
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
                    0 -> throw IllegalStateException("Could not build parameter #$index defined as $parameter")
                    1 -> possibleMappings.first()
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
            val implicits =
                """
                |implicitly available singleton objects:
                ${jirf.singletonObjects.map { (type, it) -> "|  * ${type.simpleName} -> $it" }.joinToString("\n") }}"
                """.trim()
            val exceptionsummary = creationResult.exceptions.asSequence().map { (constructor, exception) ->
                val causalChain = generateSequence<Throwable>(exception) { it.cause }
                    .mapIndexed { index, cause ->
                        val message = cause.takeIf { it is InstancingImpossibleException }?.message
                            ?.replace("it.unibo.alchemist.model.interfaces.", "")
                            ?.replace("it.unibo.alchemist.model.", "i.u.a.m.")
                            ?.replace("it.unibo.alchemist.", "i.u.a.")
                            ?.replace("java.lang.", "")
                            ?.replace("kotlin.", "")
                            ?: cause.message ?: "No message"
                        "|    failure message ${index + 1} of ${cause::class.simpleName}: $message"
                    }
                    .joinToString(separator = "\n")
                "|  - constructor: ${constructor.shorterToString()}\n$causalChain".trim()
            }.joinToString(separator = "\n")
            val errorMessage =
                """
                |Could not create $this, requested as instance of ${target.simpleName}.
                |Actual parameters: $parameters
                $exceptionsummary
                $implicits
                """.trimMargin().trim()
            val masterException = IllegalArgumentException("Illegal Alchemist specification: $errorMessage")
            creationResult.exceptions.forEach { (_, exception) -> masterException.addSuppressed(exception) }
            throw masterException
        }.also {
            creationResult.logErrors { message, arguments -> logger.info(message, *arguments) }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(JVMConstructor::class.java)
        private fun Constructor<*>.shorterToString() =
            declaringClass.simpleName + parameterTypes.joinToString(prefix = "(", postfix = ")") { it.simpleName }
    }
}
