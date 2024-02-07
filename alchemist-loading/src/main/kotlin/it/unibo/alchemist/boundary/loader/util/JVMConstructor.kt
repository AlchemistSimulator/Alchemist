/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.loader.util

import it.unibo.alchemist.util.BugReporting
import it.unibo.alchemist.util.ClassPathScanner
import net.pearx.kasechange.splitToWords
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
    val parameters: List<*> = emptyList<Any?>(),
) : JVMConstructor(type) {

    override fun <T : Any> parametersFor(target: KClass<T>, factory: Factory): List<*> = parameters

    override fun toString(): String = "$typeName${parameters.joinToString(prefix = "(", postfix = ")")}"
}

private typealias OrderedParameters = List<KParameter>

/**
 * A [JVMConstructor] whose parameters are named
 * and hence stored in a [parametersMap]
 * (no pure Java class works with named parameters now, Kotlin-only).
 */
class NamedParametersConstructor(
    type: String,
    val parametersMap: Map<*, *> = emptyMap<Any?, Any?>(),
) : JVMConstructor(type) {

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
        val constructorsWithOrderedParameters = target.constructors.map { constructor ->
            constructor.valueParameters.filterNot { it.type.jvmErasure.java in singletons }.sortedBy { it.index }
        }
        val usableConstructors: Map<OrderedParameters, Map<String, String>> =
            constructorsWithOrderedParameters.mapNotNull { parameters ->
                if (providedNames.size <= parameters.size) {
                    // Parameter count must be compatible (as many or less parameters provided)
                    val (optional, mandatory) = parameters.partition { it.isOptional }
                    val mandatoryNames = mandatory.map { it.name }
                    val requiredOptionals by lazy { optional.take(providedNames.size - mandatory.size).map { it.name } }
                    fun verifyParameterMatch(matchMethod: (List<String>).(List<String?>) -> Boolean) =
                        providedNames.matchMethod(mandatoryNames) and { providedNames.matchMethod(requiredOptionals) }
                    // Check for exact name match
                    val exactMatch = verifyParameterMatch(List<String>::containsAll)
                    if (exactMatch) {
                        parameters to emptyMap()
                    } else {
                        // Check for similar-enough non-ambiguous matches: kebab-case, snake_case, etc.
                        // convertedNames is a map between the actual parameter name and the provided name
                        val convertedNames = providedNames.mapNotNull { providedName ->
                            parameters.filter { it.name.couldBeInterpretedAs(providedName) }
                                .takeIf { it.size == 1 }
                                ?.first()
                                ?.name
                                ?.let { it to providedName }
                        }.toMap()
                        val worksIfNamesAreReplaced = convertedNames.keys.containsAll(mandatoryNames) and {
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
        val preferredMatch = usableConstructors
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

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(NamedParametersConstructor::class.java)
    }
}

internal data class TypeSearch<out T>(val typeName: String, val targetType: Class<out T>) {

    private val packageName: String? = typeName.substringBeforeLast('.', "").takeIf { it.isNotEmpty() }
    private val isQualified get() = packageName != null

    val subTypes: List<Class<out T>> by lazy {
        val compatibleTypes = when (packageName) {
            null ->
                when {
                    targetType.packageName.startsWith("it.unibo.alchemist") ->
                        ClassPathScanner.subTypesOf(targetType, "it.unibo.alchemist")
                    else -> ClassPathScanner.subTypesOf(targetType)
                }
            else -> ClassPathScanner.subTypesOf(targetType, packageName)
        }
        compatibleTypes + listOf(targetType).takeUnless { Modifier.isAbstract(targetType.modifiers) }.orEmpty()
    }

    val perfectMatches: List<Class<out T>> by lazy {
        subtypes(ignoreCase = false)
    }

    val subOptimalMatches: List<Class<out T>> by lazy {
        subtypes(ignoreCase = true)
    }

    private fun subtypes(ignoreCase: Boolean) =
        subTypes.filter { typeName.equals(if (isQualified) it.name else it.simpleName, ignoreCase = ignoreCase) }

    companion object {
        inline fun <reified T> typeNamed(name: String) = TypeSearch(name, T::class.java)
    }
}

/**
 * A constructor for a JVM class of type [typeName].
 */
sealed class JVMConstructor(val typeName: String) {

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
                    0 -> Result.failure(
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
                    else -> Result.failure(
                        IllegalStateException(
                            "Multiple matches for $typeName as subtype of ${type.simpleName}: " +
                                "${perfectMatches.map { it.name }}. Disambiguation is required.",
                        ),
                    )
                }
            }
            1 -> runCatching { newInstance(perfectMatches.first().kotlin, factory) }
            else -> Result.failure(
                IllegalStateException("Multiple perfect matches for $typeName: ${perfectMatches.map { it.name }}"),
            )
        }
    }

    private fun CreationResult<*>.logErrors(logger: (String, Array<Any?>) -> Unit) {
        for ((constructor, exception) in exceptions) {
            val errorMessages = generateSequence<Pair<Throwable?, String?>>(
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
                }
                .toList()
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
        val parameters = originalParameters.mapIndexed { index, parameter ->
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
                            error(
                                "Ambiguous mapping: $compatibleSubtypes all match the requested type $typeName for " +
                                    "parameter #$mappedIndex:$potentialType of $constructor",
                            )
                        }
                        else -> {
                            listOf(buildAny(compatibleSubtypes.first(), jirf))
                        }
                    }
                }
                when (possibleMappings.size) {
                    0 -> error("Could not build parameter #$index defined as $parameter")
                    1 -> possibleMappings.first()
                    else -> error("Ambiguous parameter #$index $parameter, multiple options match: $possibleMappings")
                }
            } else {
                parameter
            }
        }
        val creationResult = jirf.build(target.java, parameters)
        return creationResult.createdObject
            .orElseThrow { explainedFailure(jirf, target, originalParameters, creationResult) }
            .also { creationResult.logErrors { message, arguments -> logger.info(message, *arguments) } }
    }

    companion object {
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
            val implicits = when {
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
            val exceptionsSummary = exceptions.mapIndexed { consIndex, (constructor, exception) ->
                val causalChain = generateSequence<Throwable>(exception) { it.cause }
                    .mapIndexed { index, cause ->
                        val message = cause.takeIf { it is InstancingImpossibleException }?.message
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
                val intro = "${consIndex + 1}. Constructor $constructorName failed with the following exception(s):"
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
