/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.loader.util

import it.unibo.alchemist.util.BugReporting
import it.unibo.alchemist.util.ClassPathScanner
import java.lang.reflect.Constructor
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure
import org.danilopianini.jirf.CreationResult
import org.danilopianini.jirf.Factory
import org.danilopianini.jirf.InstancingImpossibleException
import org.slf4j.LoggerFactory

/**
 * A constructor for a JVM class of type [typeName].
 */
internal sealed class JVMConstructor(val typeName: String) {
    /**
     * provided a [target] class, extracts the parameters as an ordered list.
     */
    protected abstract fun <T : Any> parametersFor(target: KClass<T>, factory: Factory): List<*>

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
        val typeSearch = TypeSearch(typeName, type)
        val perfectMatches = typeSearch.perfectMatches
        return when (perfectMatches.size) {
            0 -> {
                val subOptimalMatches = typeSearch.subOptimalMatches
                when (subOptimalMatches.size) {
                    0 ->
                        Result.failure(
                            IllegalStateException(
                                """
                            |No valid match for type $typeName among subtypes of ${type.simpleName}.
                            |Valid subtypes are: ${typeSearch.subTypes.map { it.simpleName }}
                                """.trimMargin(),
                            ),
                        )
                    1 -> {
                        logger.warn(
                            "{} has been selected even though it is not a perfect match for {}",
                            subOptimalMatches.first().name,
                            typeName,
                        )
                        Result.success(newInstance(subOptimalMatches.first().kotlin, factory))
                    }
                    else ->
                        Result.failure(
                            IllegalStateException(
                                "Multiple matches for $typeName as subtype of ${type.simpleName}: " +
                                    "${perfectMatches.map { it.name }}. Disambiguation is required.",
                            ),
                        )
                }
            }
            1 -> runCatching { newInstance(perfectMatches.first().kotlin, factory) }
            else ->
                Result.failure(
                    IllegalStateException("Multiple perfect matches for $typeName: ${perfectMatches.map { it.name }}"),
                )
        }
    }

    private fun CreationResult<*>.logErrors(logger: (String, Array<Any?>) -> Unit) {
        for ((constructor, exception) in exceptions) {
            val errorMessages =
                generateSequence<Pair<Throwable?, String?>>(
                    exception to exception.message,
                ) { (outer, _) -> outer?.cause to outer?.cause?.message }
                    .takeWhile { it.first != null }
                    .filter { !it.second.isNullOrBlank() }
                    .map { (first, second) ->
                        checkNotNull(first) {
                            BugReporting.reportBug(
                                "Bug in ${JVMConstructor::class.qualifiedName}",
                                mapOf(
                                    "first" to first,
                                    "second" to second,
                                    "constructor" to constructor,
                                    "creation result" to this,
                                ),
                            )
                        }
                        "${first::class.simpleName}: $second"
                    }.toList()
            logger(
                "Constructor {} failed for {} ",
                arrayOf(
                    constructor.shorterToString(),
                    if (errorMessages.isEmpty()) "unknown reasons" else "the following reasons:",
                ),
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
        val originalParameters = parametersFor(target, jirf)
        logger.debug("Building a {} with {}", target.simpleName, originalParameters)
        val compatibleConstructors by lazy {
            target.constructors.filter { it.valueParameters.size >= originalParameters.size }
        }
        val parameters =
            originalParameters.mapIndexed { index, parameter ->
                if (parameter is JVMConstructor) {
                    val possibleMappings =
                        compatibleConstructors
                            .flatMap { constructor ->
                                val mappedIndex =
                                    constructor.valueParameters.lastIndex - originalParameters.lastIndex + index
                                val potentialType = constructor.valueParameters[mappedIndex]
                                val potentialJavaType = potentialType.type.jvmErasure.java
                                val subtypes =
                                    ClassPathScanner.subTypesOf(potentialJavaType) +
                                        when {
                                            Modifier.isAbstract(potentialJavaType.modifiers) -> emptyList()
                                            else -> listOf(potentialJavaType)
                                        }
                                val compatibleSubtypes =
                                    subtypes.filter { subtype ->
                                        val subtypeName =
                                            if (parameter.typeName.contains('.')) subtype.name else subtype.simpleName
                                        parameter.typeName == subtypeName
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
                                        error(
                                            "Ambiguous mapping: $compatibleSubtypes all match" +
                                                "the requested type $typeName" +
                                                "for parameter #$mappedIndex:$potentialType of $constructor",
                                        )
                                    }
                                    else -> {
                                        val maybeParameter = parameter.buildAny(compatibleSubtypes.first(), jirf)
                                        listOf(maybeParameter.getOrThrow())
                                    }
                                }
                                // remove duplicates
                            }.toSet()
                    /*
                     * possibleMappings contains the possible instances that can be used as parameter.
                     * If none has been produced, no way has been found to build the parameter.
                     * If one has been produced, it is used.
                     * If more than one has been produced, different constructors may have produced different objects.
                     * Typically, the latter case is due to a bad implementation of equals() and hashCode()
                     * in the parameter class, so that two objects created with the same specification are not equal.
                     *
                     * The last case is reported as an error, as objects built with
                     * the same procedure and parameters should
                     * be equal in general. However, this may change in the future.
                     */
                    when (possibleMappings.size) {
                        0 -> error("Could not build parameter #$index defined as $parameter")
                        1 -> possibleMappings.first()
                        else ->
                            error(
                                """
                                Parameter #$index '$parameter' produced ${possibleMappings.size} different instances:
                                $possibleMappings
                                A likely cause is that ${parameter.typeName} does not implement equals() and hashCode() properly
                                """.trimIndent(),
                            )
                    }
                } else {
                    parameter
                }
            }
        val creationResult = jirf.build(target.java, parameters)
        return creationResult.createdObject
            .orElseThrow { explainedFailure(jirf, target, originalParameters, creationResult) }
            .also { creationResult.logErrors { message, arguments -> logger.warn(message, *arguments) } }
    }

    private companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(JVMConstructor::class.java)

        private fun Constructor<*>.shorterToString() =
            declaringClass.simpleName + parameterTypes.joinToString(prefix = "(", postfix = ")") { it.simpleName }

        private fun <T : Any> explainedFailure(
            jirf: Factory,
            target: KClass<T>,
            parameters: List<*>,
            creationResult: CreationResult<T>,
        ): Nothing {
            val implicits =
                when {
                    jirf.singletonObjects.isEmpty() -> "|No singleton objects available in the context"
                    else ->
                        """
                    |Implicitly available singleton objects in the context: ${
                            jirf.singletonObjects
                                .map { (type, it) -> "|  * $it # associated to type ${type.simpleName}" }
                                .joinToString(prefix = "\n", separator = ";\n", postfix = ".")
                        }
                    """.trim()
                }
            val exceptions = creationResult.exceptions.asSequence()
            val exceptionsSummary =
                exceptions
                    .mapIndexed { consIndex, (constructor, exception) ->
                        val causalChain =
                            generateSequence<Throwable>(exception) { it.cause }
                                .mapIndexed { index, cause ->
                                    val message =
                                        cause
                                            .takeIf { it is InstancingImpossibleException }
                                            ?.message
                                            ?.replace("it.unibo.alchemist.model.interfaces.", "")
                                            ?.replace("it.unibo.alchemist.boundary.", "i.u.a.b.")
                                            ?.replace("it.unibo.alchemist.model.", "i.u.a.m.")
                                            ?.replace("it.unibo.alchemist.", "i.u.a.")
                                            ?.replace("java.lang.", "")
                                            ?.replace("kotlin.", "")
                                            ?: cause.message ?: "No message"
                                    "|    failure message ${index + 1} of ${cause::class.simpleName}: $message"
                                }
                        val constructorName = constructor.shorterToString()
                        val intro = "${consIndex + 1}. Constructor $constructorName failed with exception(s):"
                        val causalChainString = causalChain.joinToString(separator = "\n")
                        "| $intro\n$causalChainString".trim()
                    }.joinToString(separator = "\n")
            val errorMessage =
                """
                |Could not create $this, requested as instance of ${target.simpleName}.
                |Actual parameters: $parameters
                $implicits
                |Failure list analysis:
                $exceptionsSummary
                """.trimMargin().trim()
            val masterException = IllegalArgumentException("Illegal Alchemist specification: $errorMessage")
            creationResult.exceptions.forEach { (_, exception) -> masterException.addSuppressed(exception) }
            throw masterException
        }
    }
}
