/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.yaml

import arrow.core.Either
import it.unibo.alchemist.SupportedIncarnations
import it.unibo.alchemist.loader.EnvironmentAndExports
import it.unibo.alchemist.loader.Loader
import it.unibo.alchemist.loader.export.Extractor
import it.unibo.alchemist.loader.export.FilteringPolicy
import it.unibo.alchemist.loader.export.MoleculeReader
import it.unibo.alchemist.loader.export.Time
import it.unibo.alchemist.loader.export.filters.CommonFilters
import it.unibo.alchemist.loader.variables.Constant
import it.unibo.alchemist.loader.variables.DependentVariable
import it.unibo.alchemist.loader.variables.JSR223Variable
import it.unibo.alchemist.loader.variables.Variable
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Incarnation
import it.unibo.alchemist.model.interfaces.Position
import org.danilopianini.jirf.Factory
import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.Yaml
import java.io.InputStream
import java.io.Reader
import java.net.URL
import kotlin.reflect.KClass
import kotlin.reflect.cast
import kotlin.reflect.full.isSubclassOf

/**
 * Contains the model-to-model translation between the Alchemist YAML specification and the
 * loading system.
 */
object SimulationModel {

    fun fromYaml(yaml: String) = fromMap(Yaml().load(yaml))
    fun fromYaml(yaml: Reader) = fromMap(Yaml().load(yaml))
    fun fromYaml(yaml: InputStream) = fromMap(Yaml().load(yaml))
    fun fromYaml(yaml: URL) = fromMap(Yaml().load(yaml.openStream()))

    fun fromMap(root: Map<String, Any>): Loader {
        val unkownKeys = root.keys.filterNot { it.startsWith("_") } - Syntax.rootKeys
        require(unkownKeys.isEmpty()) {
            "There are unknown root keys: $unkownKeys. Allowed root keys: ${Syntax.rootKeys}" +
                "If you need private keys (e.g. for internal use), prefix them with underscore (_)"
        }
        val context = Context()
        var previousSize: Int? = null
        var injectedRoot = root
        while (context.constants.size != previousSize) {
            previousSize = context.constants.size
            val resolvedConstants = visitMultipleNamed(
                context,
                injectedRoot[Syntax.variables],
                failOnError = false
            ) { name, element ->
                visitConstant(context, element)
                    ?.also { context.pushLookupEntry(name, element as Map<*, *>, it.value) }
            }
            context.constants += resolvedConstants.mapValues { (_, v) -> v.value }
            injectedRoot = inject(context, injectedRoot)
            logger.debug("Constants {}", context.constants)
            logger.debug("Lookup {}", context.elementLookup)
            logger.debug("New map {}", injectedRoot)
        }
        val variables = visitMultipleNamed(context, injectedRoot[Syntax.variables]) { name, element ->
            visitAnyAndBuild<Variable<*>>(context, element)
                ?.also { context.pushLookupEntry(name, element as Map<*, *>, it) }
        }
        logger.debug("Variables: {}", variables)
        val dependentVariables = visitMultipleNamed(context, injectedRoot[Syntax.variables]) { name, element ->
            visitDependentVariable(context, element)
                ?.also { context.pushLookupEntry(name, element as Map<*, *>, it) }
        }
        logger.debug("Dependent variables: {}", dependentVariables)
        val remoteDependencies = visitMultipleOrdered(context, injectedRoot[Syntax.remoteDependencies]) {
            visitAnyAndBuild<String>(context, it)
        }
        return object : Loader {

            override fun getDependentVariables(): Map<String, DependentVariable<*>> = dependentVariables

            override fun getVariables(): Map<String, Variable<*>> = variables

            override fun <T : Any?, P : Position<P>> getWith(values: Map<String, *>): EnvironmentAndExports<T, P> {
                val localContext = Context(namedLookup = context.namedLookup)
                var localRoot = injectedRoot
                /*
                 * Variables actual instancing
                 */
                val variableValues = this.variables.mapValues { (name, previous) ->
                    if (values.containsKey(name)) values[name] else previous.default
                }
                val knownValues: MutableMap<String, Any?> = constants.toMutableMap()
                knownValues.putAll(variableValues)
                var previousToVisitSize: Int? = null
                var toVisit = dependentVariables.toMutableMap()
                val failures = mutableListOf<Throwable>()
                while (toVisit.isNotEmpty() && toVisit.size != previousToVisitSize) {
                    failures.clear()
                    val iterator = toVisit.iterator()
                    val (name, variable) = iterator.next()
                    val interpretation = runCatching { variable.getWith(knownValues) }
                    when {
                        interpretation.isSuccess -> {
                            knownValues[name] = interpretation.getOrNull()
                            iterator.remove()
                        }
                        else -> failures.add(interpretation.exceptionOrNull() ?: TODO())
                    }
                }
                failures.forEach { throw it }
                knownValues.forEach { (name, value) ->
                    localContext.pushLookupEntry(name, context.lookupElementByName(name), value)
                }
                localRoot = inject(context, localRoot)
                /*
                 * Simulation environment
                 */
                val environment: Environment<T, P> = visitEnvironment(localContext, localRoot[Syntax.environment])
                val incarnation = SupportedIncarnations.get<T, P>(
                    visitString(localContext, localRoot[Syntax.incarnation])
                ).orElseThrow {
                    IllegalArgumentException(
                        "Invalid incarnation descriptor: ${localRoot[Syntax.incarnation]}. " +
                            "Valid incarnations are ${SupportedIncarnations.getAvailableIncarnations()}"
                    )
                }
                localContext.factory.registerSingleton(Incarnation::class.java, incarnation)
                val exports = visitMultipleOrdered(localContext, root[Syntax.export]) { visitExports(localContext, it) }
                return EnvironmentAndExports(environment, exports)
            }

            override fun getConstants(): Map<String, Any?> = context.constants.toMap()

            override fun getRemoteDependencies(): List<String> = remoteDependencies
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun inject(context: Context, root: Map<String, Any>): Map<String, Any> =
        replaceKnownRecursively(context, root) as Map<String, Any>

    private fun replaceKnownRecursively(context: Context, root: Any?): Any? =
        when (root) {
            is Map<*, *> ->
                context.lookup<Any>(root)?.getOrNull()
                    ?: root.entries.map {
                        replaceKnownRecursively(context, it.key) to replaceKnownRecursively(context, it.value)
                    }.toMap()
            is Iterable<*> -> root.map { replaceKnownRecursively(context, it) }
            else -> root
        }

    private fun visitAny(context: Context, root: Any?): Any? =
        when (root) {
            is Iterable<*> -> root.map { visitAny(context, it) }
            is Map<*, *> ->
                context.lookup<Any>(root)?.getOrNull()
                    ?: visitJVMConstructor(context, root)
                    ?: root
            else -> root
        }

    private inline fun <reified T : Any> visitAnyAndBuild(context: Context, root: Any): T? =
        visitAnyAndBuildCatching<T>(context, root).getOrNull()

    private inline fun <reified T : Any> visitAnyAndBuildCatching(context: Context, root: Any): Result<T?> =
        when (root) {
            is T -> Result.success(root)
            is Map<*, *> ->
                context.lookup(root)
                    ?: visitJVMConstructor(context, root)?.buildAny(context.factory)
                    ?: Result.success(null)
            else -> {
                logger.debug("Unable to build a {} with {}, attempting a JIRF conversion ", root, T::class.simpleName)
                context.factory.convert(T::class.java, root)
                    .map { Result.success(it) }
                    .orElseGet {
                        Result.failure(
                            IllegalArgumentException(
                                "Unable to convert $root into a ${T::class.simpleName}"
                            )
                        )
                    }
            }
        }

    private fun visitConstant(context: Context, root: Any): Constant<*>? =
        visitDependentVariable(context, root)
            ?.runCatching { getWith(context.constants) }
            ?.also { result ->
                result.exceptionOrNull()?.let { error ->
                    logger.info("Unable to resolve constant from {} with context {}: {}", root, context, error.message)
                }
            }
            ?.getOrNull()
            ?.let { Constant(it) }

    private fun visitDependentVariable(context: Context, root: Any): DependentVariable<*>? =
        if (root is Map<*, *>) {
            if (root.containsKey("formula")) {
                val formula = root["formula"]
                if (formula is String) {
                    val language = root["language"]?.toString()?.toLowerCase() ?: "groovy"
                    JSR223Variable<Any>(language, formula)
                } else {
                    Constant(formula)
                }
            } else {
                visitJVMConstructor(context, root)
                    ?.buildAny<DependentVariable<Any>>(context.factory)
                    ?.getOrNull()
            }
        } else {
            null
        }

    @Suppress("UNCHECKED_CAST")
    private fun <T, P : Position<P>> visitEnvironment(context: Context, root: Any?): Environment<T, P> =
        if (root == null) {
            logger.info("No environment specified, defaulting to {}", Continuous2DEnvironment::class.simpleName)
            Continuous2DEnvironment<T>() as Environment<T, P>
        } else {
            requireNotNull(visitAnyAndBuildCatching<Environment<T, P>>(context, root).getOrThrow()) {
                "Could not create an environment from: $root"
            }
        }

    private fun visitExports(context: Context, root: Any?): Extractor? =
        when (root) {
            root is String && root.equals(Syntax.Export.time, ignoreCase = true) -> Time()
            root is Map<*, *> && root.containsKey(Syntax.Export.molecule) -> {
                root as Map<*, *>
                val molecule = visitString(context, root[Syntax.Export.molecule])
                val propertyDescriptor = root[Syntax.Export.property]
                val property = visitStringOptionally(context, propertyDescriptor).also {
                    if (it == null && propertyDescriptor != null) {
                        logger.warn(
                            "Ignored property {}:{}, cannot be converted to String.",
                            propertyDescriptor,
                            propertyDescriptor::class.simpleName,
                        )
                    }
                }
                val incarnation = context.factory.build(Incarnation::class.java)?.createdObjectOrThrowException
                val filter: FilteringPolicy = visitStringOptionally(context, root[Syntax.Export.valueFilter])
                    ?.let { CommonFilters.fromString(it) }
                    ?: CommonFilters.NOFILTER.filteringPolicy
                val aggregators: List<String> = visitMultipleOrdered(context, root[Syntax.Export.aggregators]) {
                    visitString(context, it)
                }
                MoleculeReader(molecule, property, incarnation, filter, aggregators)
            }
            is Map<*, *> -> visitAnyAndBuild(context, root)
            else -> null
        }

    private fun visitJVMConstructor(context: Context, root: Map<*, *>): JVMConstructor? =
        if (root.containsKey("type")) {
            val type: String = visitString(context, root[Syntax.JavaType.type])
            when (val parameters = visitParameters(context, root[Syntax.JavaType.parameters])) {
                is Either.Left -> OrderedParametersConstructor(type, parameters.a)
                is Either.Right -> NamedParametersConstructor(type, parameters.b)
            }
        } else {
            null
        }

    private fun visitParameters(context: Context, root: Any?): Either<List<*>, Map<String, *>> = when (root) {
        null -> Either.left(emptyList<Any>())
        is Iterable<*> -> Either.left(root.map { visitAny(context, it) })
        is Map<*, *> -> Either.right(
            root.map { visitString(context, it.key) to visitAny(context, it.value) }.toMap()
        )
        else -> Either.left(listOf(visitAny(context, root)))
    }

    private fun visitStringOptionally(context: Context, root: Any?): String? =
        when (root) {
            null -> throw IllegalStateException("null value provided where String was required")
            is CharSequence -> root.toString()
            is Map<*, *> -> context.lookup<CharSequence>(root)?.getOrNull()?.toString()
            else -> null
        }

    private fun visitString(context: Context, root: Any?): String =
        visitStringOptionally(context, root) ?: throw IllegalStateException("Unable to obtain a String from $root")

    private fun <T : Any> visitMultipleOrdered(
        context: Context,
        root: Any?,
        visitSingle: (Any) -> T?
    ): List<T> = root?.let {
        visitSingle(root)?.let { single -> listOf(single) }
            ?: when (root) {
                is Iterable<*> -> root.flatMap { element ->
                    requireNotNull(element) {
                        "Illegal null element in $root"
                    }
                    visitMultipleOrdered(context, element, visitSingle)
                }
                is Map<*, *> -> visitMultipleOrdered(context, root.values, visitSingle)
                else -> null
            }
    } ?: emptyList()

    private fun <T : Any> visitMultipleNamed(
        context: Context,
        root: Any?,
        failOnError: Boolean = false,
        visitSingle: (String, Any) -> T?
    ): Map<String, T> =
        when (root) {
            is Map<*, *> -> visitMultipleNamedFromMap(context, root, failOnError, visitSingle)
            is Iterable<*> ->
                root.flatMap { visitMultipleNamed(context, it, failOnError, visitSingle).toList() }.toMap()
            else ->
                emptyMap<String, T>().takeUnless { failOnError }
                    ?: throw IllegalArgumentException("Unable to build a named object from $root")
        }

    private fun <T : Any> visitMultipleNamedFromMap(
        context: Context,
        root: Map<*, *>,
        failOnError: Boolean = false,
        visitSingle: (String, Any) -> T?
    ): Map<String, T> =
        root.flatMap { (key, value) ->
            requireNotNull(value) {
                "Illegal null element found in $root. Current context: $context."
            }
            visitSingle(key.toString(), value)?.let { listOf(key.toString() to it) }
                ?: visitMultipleNamed(context, value, failOnError, visitSingle).toList()
        }.toMap()

    val logger = LoggerFactory.getLogger(SimulationModel::class.java)

    private data class Context(
        val constants: MutableMap<String, Any?> = mutableMapOf(),
        val elementLookup: MutableMap<Map<*, *>, Any?> = mutableMapOf(),
        val namedLookup: MutableMap<String, Map<*, *>> = mutableMapOf(),
        val factory: Factory = ObjectFactory.makeBaseFactory(),
    ) {

        fun pushLookupEntry(name: String, element: Map<*, *>, value: Any?) {
            if (namedLookup.containsKey(name)) {
                val previous = elementLookup[element]
                val differentButSameClass = value != previous &&
                    value != null && previous != null &&
                    value::class == previous::class
                if (differentButSameClass) {
                    throw IllegalStateException(
                        "Tried to substitute value for $name: $element from $value to $previous"
                    )
                } else {
                    logger.info("Value substitution for {}: {} to {}", element, previous, value)
                }
            }
            namedLookup[name] = element
            elementLookup[element] = value
        }

        inline fun <reified T : Any> lookup(element: Map<*, *>): Result<T?>? = lookup(T::class, element)

        fun <T : Any> lookup(destinationType: KClass<T>, element: Map<*, *>): Result<T?>? =
            if (elementLookup.containsKey(element)) {
                val result = elementLookup[element]
                if (result == null) {
                    Result.success(null)
                } else {
                    if (result::class.isSubclassOf(destinationType)) {
                        Result.success(destinationType.cast(result))
                    } else {
                        Result.failure(
                            IllegalStateException(
                                "A request for type ${destinationType.qualifiedName} has been fullfilled " +
                                    "by the context based on $element, but the result does not match the expected type"
                            )
                        )
                    }
                }
            } else {
                null
            }

        fun lookupElementByName(name: String): Map<*, *> =
            namedLookup[name] ?: throw IllegalArgumentException("No element named $name")
    }
}
