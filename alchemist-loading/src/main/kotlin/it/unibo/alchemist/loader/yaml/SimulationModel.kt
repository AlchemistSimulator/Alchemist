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
import arrow.core.extensions.map.functorFilter.filter
import it.unibo.alchemist.SupportedIncarnations
import it.unibo.alchemist.loader.EnvironmentAndExports
import it.unibo.alchemist.loader.Loader
import it.unibo.alchemist.loader.displacements.Displacement
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
import it.unibo.alchemist.model.implementations.linkingrules.CombinedLinkingRule
import it.unibo.alchemist.model.implementations.linkingrules.NoLinks
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Incarnation
import it.unibo.alchemist.model.interfaces.LinkingRule
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position
import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.random.RandomGenerator
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
private typealias Seeds = Pair<RandomGenerator, RandomGenerator>

/**
 * Converts a representation of an Alchemist simulation into an executable simulation.
 */
object SimulationModel {

    val logger = LoggerFactory.getLogger(SimulationModel::class.java)

    fun fromYaml(yaml: String) = fromMap(Yaml().load(yaml))
    fun fromYaml(yaml: Reader) = fromMap(Yaml().load(yaml))
    fun fromYaml(yaml: InputStream) = fromMap(Yaml().load(yaml))
    fun fromYaml(yaml: URL) = fromMap(Yaml().load(yaml.openStream()))

    fun fromMap(root: Map<String, Any>): Loader {
        DocumentRoot.verifyKeysForElement(root)
        val context = Context()
        var previousSize: Int? = null
        var injectedRoot = root
        while (context.constants.size != previousSize) {
            previousSize = context.constants.size
            val resolvedConstants = visitMultipleNamed(
                context,
                injectedRoot[DocumentRoot.variables],
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
        val variables = visitMultipleNamed(context, injectedRoot[DocumentRoot.variables]) { name, element ->
            visitAnyAndBuild<Variable<*>>(context, element)
                ?.also { context.pushLookupEntry(name, element as Map<*, *>, it) }
        }
        logger.debug("Variables: {}", variables)
        val dependentVariables = visitMultipleNamed(context, injectedRoot[DocumentRoot.variables]) { name, element ->
            visitDependentVariable(context, element)
                ?.also { context.pushLookupEntry(name, element as Map<*, *>, it) }
        }
        logger.debug("Dependent variables: {}", dependentVariables)
        val remoteDependencies = visitMultipleOrdered(context, injectedRoot[DocumentRoot.remoteDependencies]) {
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
                val (scenarioRNG, simulationRNG) =
                    visitSeeds(localContext, localRoot[DocumentRoot.seeds])
                fun setCurrentRandomGenerator(randomGenerator: RandomGenerator) =
                    localContext.factory.registerSingleton(RandomGenerator::class.java, randomGenerator)
                setCurrentRandomGenerator(simulationRNG)
                val incarnation = SupportedIncarnations.get<T, P>(
                    visitString(localContext, localRoot[DocumentRoot.incarnation])
                ).orElseThrow {
                    IllegalArgumentException(
                        "Invalid incarnation descriptor: ${localRoot[DocumentRoot.incarnation]}. " +
                            "Valid incarnations are ${SupportedIncarnations.getAvailableIncarnations()}"
                    )
                }
                localContext.factory.registerSingleton(Incarnation::class.java, incarnation)
                val environment: Environment<T, P> = visitEnvironment(localContext, localRoot[DocumentRoot.environment])
                localContext.factory.registerSingleton(Environment::class.java, environment)
                // Environment population
                // LAYERS
                // LINKING RULE
                val linkingRules = visitMultipleOrdered(localContext, localRoot[DocumentRoot.linkingRule]) { element ->
                    when (element) {
                        is Map<*, *> -> visitAnyAndBuildCatching<LinkingRule<T, P>>(localContext, element).getOrThrow()
                        else -> null
                    }
                }
                val linkingRule: LinkingRule<T, P> = when {
                    linkingRules.isEmpty() -> NoLinks()
                    linkingRules.size == 1 -> linkingRules.first()
                    else -> CombinedLinkingRule(linkingRules)
                }
                environment.linkingRule = linkingRule
                localContext.factory.registerSingleton(LinkingRule::class.java, linkingRule)
                // DISPLACEMENTS
                setCurrentRandomGenerator(scenarioRNG)
                val displacementDescriptors: List<Pair<Displacement<P>, Map<*, *>>> = visitMultipleOrdered(
                    localContext,
                    localRoot[DocumentRoot.displacements]
                ) { element ->
                    (element as? Map<*, *>)?.let {
                        visitAnyAndBuild<Displacement<P>>(localContext, element)?.let { displacement ->
                            DocumentRoot.Displacement.verifyKeysForElement(
                                it.filterKeys { it !in DocumentRoot.JavaType.validKeys }
                            )
                            displacement to element
                        }
                    }
                }
                setCurrentRandomGenerator(simulationRNG)
                for ((displacement, descriptor) in displacementDescriptors) {
                    val nodeDescriptor = descriptor[DocumentRoot.Displacement.nodes]
                    if (descriptor.containsKey(DocumentRoot.Displacement.nodes)) {
                        requireNotNull(nodeDescriptor) {
                            "Invalid node type descriptor: $nodeDescriptor"
                        }
                        if (nodeDescriptor is Map<*, *>) {
                            DocumentRoot.JavaType.verifyKeysForElement(nodeDescriptor)
                        }
                    }
                    // special management of GraphStream-based displacement TODO
                    displacement.stream().forEach { position ->
                        val node = visitNode(simulationRNG, incarnation, environment, localContext, nodeDescriptor)
                        localContext.factory.registerSingleton(Node::class.java, node)
                        // CONTENTS
                        // PROGRAMS
                        environment.addNode(node, position)
                    }
                }
                // EXPORTS
                val exports = visitMultipleOrdered(localContext, root[DocumentRoot.export]) {
                    visitExports(localContext, it)
                }
                return EnvironmentAndExports(environment, exports)
            }

            override fun getConstants(): Map<String, Any?> = context.constants.toMap()

            override fun getRemoteDependencies(): List<String> = remoteDependencies
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun inject(context: Context, root: Map<String, Any>): Map<String, Any> =
        replaceKnownRecursively(context, root) as Map<String, Any>

    private fun makeDefaultRandomGenerator(seed: Long) = MersenneTwister(seed)

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
            if (root.containsKey(DocumentRoot.DependentVariable.formula)) {
                val formula = root[DocumentRoot.DependentVariable.formula]
                if (formula is String) {
                    val language = root[DocumentRoot.DependentVariable.language]?.toString()?.toLowerCase() ?: "groovy"
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
            Continuous2DEnvironment(
                context.factory.build(Incarnation::class.java)
                    .createdObjectOrThrowException as Incarnation<T, Euclidean2DPosition>
            ) as Environment<T, P>
        } else {
            requireNotNull(visitAnyAndBuildCatching<Environment<T, P>>(context, root).getOrThrow()) {
                "Could not create an environment from: $root"
            }
        }

    private fun visitExports(context: Context, root: Any?): Extractor? =
        when (root) {
            root is String && root.equals(DocumentRoot.Export.time, ignoreCase = true) -> Time()
            root is Map<*, *> && root.containsKey(DocumentRoot.Export.molecule) -> {
                root as Map<*, *>
                val molecule = visitString(context, root[DocumentRoot.Export.molecule])
                val propertyDescriptor = root[DocumentRoot.Export.property]
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
                val filter: FilteringPolicy = visitStringOptionally(context, root[DocumentRoot.Export.valueFilter])
                    ?.let { CommonFilters.fromString(it) }
                    ?: CommonFilters.NOFILTER.filteringPolicy
                val aggregators: List<String> = visitMultipleOrdered(context, root[DocumentRoot.Export.aggregators]) {
                    visitString(context, it)
                }
                MoleculeReader(molecule, property, incarnation, filter, aggregators)
            }
            is Map<*, *> -> visitAnyAndBuild(context, root)
            else -> null
        }

    private fun visitJVMConstructor(context: Context, root: Map<*, *>): JVMConstructor? =
        if (root.containsKey("type")) {
            val type: String = visitString(context, root[DocumentRoot.JavaType.type])
            when (val parameters = visitParameters(context, root[DocumentRoot.JavaType.parameters])) {
                is Either.Left -> OrderedParametersConstructor(type, parameters.a)
                is Either.Right -> NamedParametersConstructor(type, parameters.b)
            }
        } else {
            null
        }

    private fun <T, P : Position<P>> visitNode(
        randomGenerator: RandomGenerator,
        incarnation: Incarnation<T, P>,
        environment: Environment<T, P>,
        context: Context,
        root: Any?
    ): Node<T> =
        when (root) {
            is CharSequence? -> incarnation.createNode(randomGenerator, environment, root?.toString())
            is Map<*, *> -> visitAnyAndBuildCatching<Node<T>>(context, root).getOrThrow()
            else -> null
        } ?: throw IllegalArgumentException("Invalid node descriptor: $root")

    private fun visitParameters(context: Context, root: Any?): Either<List<*>, Map<String, *>> = when (root) {
        null -> Either.left(emptyList<Any>())
        is Iterable<*> -> Either.left(root.map { visitAny(context, it) })
        is Map<*, *> -> Either.right(
            root.map { visitString(context, it.key) to visitAny(context, it.value) }.toMap()
        )
        else -> Either.left(listOf(visitAny(context, root)))
    }

    private fun visitRandomGenerator(context: Context, root: Any): RandomGenerator {
        fun failure(cause: Throwable? = null): Nothing {
            val message = "Invalid random generator descriptor: $root"
            throw cause?.let { IllegalArgumentException(message, cause) } ?: IllegalArgumentException(message)
        }
        return visitAnyAndBuild<Long>(context, root)
            ?.let { makeDefaultRandomGenerator(it) }
            ?: visitAnyAndBuildCatching<RandomGenerator>(context, root).getOrElse { failure(it) }
            ?: failure()
    }

    private fun visitSeeds(context: Context, root: Any?): Seeds =
        when (root) {
            null -> makeDefaultRandomGenerator(0) to makeDefaultRandomGenerator(0)
                .also {
                    logger.warn(
                        "No seeds specified, defaulting to 0 for both {} and {}",
                        DocumentRoot.Seeds.scenario,
                        DocumentRoot.Seeds.scenario
                    )
                }
            is Map<*, *> -> {
                val stringKeys = root.keys.filterIsInstance<String>()
                require(stringKeys.size == root.keys.size) {
                    "Illegal seeds sub-keys: ${root.keys - stringKeys}"
                }
                val validKeys = DocumentRoot.Seeds.validKeys
                val nonPrivateKeys = stringKeys.filterNot { it.startsWith("_") }
                require(nonPrivateKeys.all { it in validKeys }) {
                    "Illegal seeds sub-keys: ${nonPrivateKeys - validKeys}"
                }
                fun valueOf(element: String): Any =
                    if (root.containsKey(element)) {
                        root[element] ?: throw IllegalArgumentException(
                            "Invalid random generator descriptor $root has a null value associated to $element"
                        )
                    } else {
                        0
                    }
                visitRandomGenerator(context, valueOf(DocumentRoot.Seeds.scenario)) to
                    visitRandomGenerator(context, valueOf(DocumentRoot.Seeds.simulation))
            }
            else -> throw IllegalArgumentException(
                "Not a valid ${DocumentRoot.seeds} section: $root. Expected " +
                    DocumentRoot.Seeds.validKeys.map { it to "<a number>" }
            )
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
