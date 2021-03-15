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
import it.unibo.alchemist.loader.displacements.Displacement
import it.unibo.alchemist.loader.export.Extractor
import it.unibo.alchemist.loader.export.FilteringPolicy
import it.unibo.alchemist.loader.export.MoleculeReader
import it.unibo.alchemist.loader.export.Time
import it.unibo.alchemist.loader.export.filters.CommonFilters
import it.unibo.alchemist.loader.shapes.Shape
import it.unibo.alchemist.loader.variables.Constant
import it.unibo.alchemist.loader.variables.DependentVariable
import it.unibo.alchemist.loader.variables.JSR223Variable
import it.unibo.alchemist.loader.variables.Variable
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.implementations.linkingrules.CombinedLinkingRule
import it.unibo.alchemist.model.implementations.linkingrules.NoLinks
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Action
import it.unibo.alchemist.model.interfaces.Condition
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Incarnation
import it.unibo.alchemist.model.interfaces.LinkingRule
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.TimeDistribution
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
private typealias ReactionComponentFunction<T, P, R> =
    (RandomGenerator, Environment<T, P>, Node<T>, TimeDistribution<T>, Reaction<T>, String?) -> R

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
        require(DocumentRoot.validateDescriptor(root)) {
            "Invalid simulation descriptor: $root.\n" + DocumentRoot.validDescriptors.first()
        }
        val context = Context()
        var previousSize: Int? = null
        var injectedRoot = root
        while (context.constants.size != previousSize) {
            previousSize = context.constants.size
            visitNamedRecursively(
                context = context,
                root = injectedRoot[DocumentRoot.variables] ?: emptyMap<String, Any>(),
                syntax = null // Prevent clashes with variables
            ) { name, element -> visitConstant(name, context, element) }
            injectedRoot = inject(context, injectedRoot)
            logger.debug("{} constants: {}", context.constants.size, context.constants)
        }
        logger.info("{} constants: {}", context.constants.size, context.constants)
        val variables: Map<String, Variable<*>> =
            visitNamedRecursively(
                context = context,
                root = injectedRoot[DocumentRoot.variables] ?: emptyMap<String, Any>(),
                syntax = null, // Prevent clashes with dependent variables
            ) { name, element ->
                visitAnyAndBuildCatching<Variable<*>>(context, element)
                    ?.onFailure { logger.debug("Not a valid variable: {} from {}: {}", name, element, it.message) }
                    ?.takeIf { it.isSuccess }
                    ?.also { context.registerVariable(name, element as Map<*, *>) }
            }
        logger.info("Variables: {}", variables)
        injectedRoot = inject(context, injectedRoot)
        val dependentVariables: Map<String, DependentVariable<*>> =
            visitNamedRecursively(
                context = context,
                root = injectedRoot[DocumentRoot.variables] ?: emptyMap<String, Any>(),
                syntax = DocumentRoot.DependentVariable,
            ) { name, element -> visitDependentVariableRegistering(name, context, element) }
        logger.info("Dependent variables: {}", dependentVariables)
        val remoteDependencies =
            visitRecursively(
                context,
                injectedRoot[DocumentRoot.remoteDependencies] ?: emptyMap<String, Any>(),
            ) { visitAnyAndBuildCatching<String>(context, it) }
        logger.info("Remote dependencies: {}", remoteDependencies)
        return object : Loader {

            override fun getDependentVariables(): Map<String, DependentVariable<*>> = dependentVariables

            override fun getVariables(): Map<String, Variable<*>> = variables

            override fun <T : Any?, P : Position<P>> getWith(values: Map<String, *>): EnvironmentAndExports<T, P> {
                val localContext = context.child()
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
                    runCatching { variable.getWith(knownValues) }
                        .onSuccess { iterator.remove() }
                        .onSuccess { result -> knownValues[name] = result }
                        .onFailure { exception -> failures.add(exception) }
                }
                failures.forEach { throw it }
                knownValues.forEach { (name, value) ->
                    localContext.fixVariableValue(name, value)
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
                    localRoot[DocumentRoot.incarnation].toString()
                ).orElseThrow {
                    IllegalArgumentException(
                        "Invalid incarnation descriptor: ${localRoot[DocumentRoot.incarnation]}. " +
                            "Valid incarnations are ${SupportedIncarnations.getAvailableIncarnations()}"
                    )
                }
                localContext.factory.registerSingleton(Incarnation::class.java, incarnation)
                val environment: Environment<T, P> =
                    visitEnvironment(incarnation, localContext, localRoot[DocumentRoot.environment])
                logger.info("Created environment: {}", environment)
                localContext.factory.registerSingleton(Environment::class.java, environment)
                // LAYERS TODO
                // LINKING RULE
                val linkingRule = visitLinkingRule<P, T>(
                    localContext,
                    localRoot[DocumentRoot.linkingRule] ?: emptyMap<String, Any>()
                )
                environment.linkingRule = linkingRule
                localContext.factory.registerSingleton(LinkingRule::class.java, linkingRule)
                // DISPLACEMENTS
                setCurrentRandomGenerator(scenarioRNG)
                val displacementsSource = localRoot[DocumentRoot.displacements] ?: emptyList<Any>().also {
                    logger.warn("There are no displacements in the specification, the environment won't have any node")
                }
                val displacementDescriptors: List<Pair<Displacement<P>, Map<*, *>>> = visitRecursively(
                    localContext,
                    displacementsSource,
                    syntax = DocumentRoot.Displacement
                ) { element ->
                    (element as? Map<*, *>)?.let {
                        visitAnyAndBuildCatching<Displacement<P>>(localContext, element) ?.map { it to element }
                    }
                }
                logger.debug("Displacement descriptors: {}", displacementDescriptors)
                setCurrentRandomGenerator(simulationRNG)
                for ((displacement, descriptor) in displacementDescriptors) {
                    logger.debug("Processing displacement: {} with descriptor: {}", displacement, descriptor)
                    val nodeDescriptor = descriptor[DocumentRoot.Displacement.nodes]
                    if (descriptor.containsKey(DocumentRoot.Displacement.nodes)) {
                        requireNotNull(nodeDescriptor) { "Invalid node type descriptor: $nodeDescriptor" }
                        if (nodeDescriptor is Map<*, *>) {
                            DocumentRoot.JavaType.validateDescriptor(nodeDescriptor)
                        }
                    }
                    // CONTENTS
                    val contents = visitContents(incarnation, localContext, descriptor)
                    // PROGRAMS
                    val programKey = DocumentRoot.Displacement.programs
                    displacement.stream().forEach { position ->
                        // special management of GraphStream-based displacement TODO
                        val node = visitNode(simulationRNG, incarnation, environment, localContext, nodeDescriptor)
                        localContext.factory.registerSingleton(Node::class.java, node)
                        contents.forEach { (shapes, molecule, concentrationMaker) ->
                            if (shapes.any { position in it }) {
                                node.setConcentration(molecule, concentrationMaker())
                            }
                        }
                        val programs = visitRecursively<Reaction<T>>(
                            localContext,
                            descriptor[programKey],
                            DocumentRoot.Displacement.Program
                        ) { program ->
                            requireNotNull(program) {
                                "null is not a valid program ${DocumentRoot.Displacement.Program.guide}"
                            }
                            (program as? Map<*, *>)?.let {
                                visitProgram(simulationRNG, incarnation, environment, node, localContext, it)
                            }
                        }
                        programs.forEach { node.addReaction(it) }
                        environment.addNode(node, position)
                        logger.debug("Added node {} at {}", node.id, position)
                        localContext.factory.deregisterSingleton(node)
                    }
                }
                // EXPORTS
                val exports = visitRecursively(localContext, root[DocumentRoot.export] ?: emptyList<Any>()) {
                    visitExports(incarnation, localContext, it)
                }
                return EnvironmentAndExports(environment, exports)
            }

            override fun getConstants(): Map<String, Any?> = context.constants.toMap()

            override fun getRemoteDependencies(): List<String> = remoteDependencies
        }
    }

    private fun <P : Position<P>, T : Any?> visitLinkingRule(localContext: Context, root: Any?): LinkingRule<T, P> {
        val linkingRules = visitRecursively(localContext, root, DocumentRoot.JavaType) { element ->
            visitAnyAndBuildCatching<LinkingRule<T, P>>(localContext, element)
        }
        return when {
            linkingRules.isEmpty() -> NoLinks()
            linkingRules.size == 1 -> linkingRules.first()
            else -> CombinedLinkingRule(linkingRules)
        }
    }

    private inline fun <reified T> cantBuildWith(root: Any?, syntax: SyntaxElement? = null): Nothing =
        cantBuildWith(T::class, root, syntax)

    private fun cantBuildWith(clazz: KClass<*>, root: Any?, syntax: SyntaxElement? = null): Nothing =
        cantBuildWith(clazz.simpleName ?: "unknown type", root, syntax)

    private fun cantBuildWith(name: String, root: Any?, syntax: SyntaxElement? = null): Nothing {
        val type = root?.let { it::class.simpleName }
        val guide = syntax?.guide?.let { " A guide follows.\n$it" } ?: ""
        throw IllegalArgumentException(
            "Invalid $name specification: $root:$type.$guide"
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun inject(context: Context, root: Map<String, Any>): Map<String, Any> =
        (replaceKnownRecursively(context, root) as Map<String, Any>).also { logger.debug("New model: {}", it) }

    private fun makeDefaultRandomGenerator(seed: Long) = MersenneTwister(seed)

    private fun replaceKnownRecursively(context: Context, root: Any?): Any? =
        when (root) {
            is Map<*, *> -> {
                when (val lookup = context.lookup<Any>(root)) {
                    null -> root.entries.map {
                        replaceKnownRecursively(context, it.key) to replaceKnownRecursively(context, it.value)
                    }.toMap()
                    else -> lookup.getOrThrow()
                }
            }
            is Iterable<*> -> root.map { replaceKnownRecursively(context, it) }
            else -> root
        }

    private fun visitParameter(context: Context, root: Any?): Any? =
        when (root) {
            is Iterable<*> -> root.map { visitParameter(context, it) }
            is Map<*, *> ->
                context.lookup<Any>(root)?.getOrNull()
                    ?: visitJVMConstructor(context, root)
                    ?: root
            else -> root
        }

    private inline fun <reified T : Any> visitAnyAndBuildCatching(context: Context, root: Any?): Result<T>? =
        when (root) {
            is T -> Result.success(root)
            is Map<*, *> ->
                visitJVMConstructor(context, root)?.buildAny(context.factory)
            else -> {
                logger.debug("Unable to build a {} with {}, attempting a JIRF conversion ", root, T::class.simpleName)
                context.factory.convert(T::class.java, root)
                    .map { Result.success(it) }
                    .orElseGet {
                        Result.failure(
                            IllegalArgumentException("Unable to convert $root into a ${T::class.simpleName}")
                        )
                    }
            }
        }

    private fun visitConstant(name: String, context: Context, root: Any?): Result<Constant<*>>? {
        val constant: Constant<*>? = when (root) {
            is Map<*, *> -> {
                visitDependentVariable(name, context, root)
                    ?.mapCatching { it.getWith(context.constants) }
                    ?.onFailure { logger.debug("Evaluation failed at {}, context {}:\n{}", root, context, it.message) }
                    ?.onSuccess { context.registerConstant(name, root, it) }
                    ?.map { Constant(it) }
                    ?.getOrNull()
            }
//            is Iterable<*> -> null
//            else -> Constant(root)
            else -> null
        }
        if (constant != null) {
            logger.debug("Variable {}, evaluated from {} as constant, returned {}", name, root, constant.value)
            require(!context.constants.containsKey(name) || context.constants[name] == constant.value) {
                """
                Inconsistent definition of variables named $name:
                  - previous evaluation: ${context.constants[name]}
                  - current value: $constant
                Item originating this issue: $root
                Context at time of failure: $context
                """.trimIndent()
            }
        }
        return constant?.let { Result.success(it) }
    }

    private fun <T, P : Position<P>> visitContents(
        incarnation: Incarnation<T, P>,
        context: Context,
        root: Map<*, *>
    ): List<Triple<List<Shape<P>>, Molecule, () -> T>> {
        logger.debug("Visiting contents: {}", root)
        val allContents = root[DocumentRoot.Displacement.contents] ?: emptyList<Any>()
        return visitRecursively(context, allContents) { element ->
            logger.debug("Visiting as content: {}", element)
            val moleculeKey = DocumentRoot.Displacement.Contents.molecule
            (element as? Map<*, *>)
                ?.takeIf { element.containsKey(moleculeKey) }
                ?.let {
                    logger.debug("Found content descriptor: {}", it)
                    val shapesKey = DocumentRoot.Displacement.Contents.shapes
                    val shapes = visitRecursively(context, element[shapesKey]) { shape ->
                        visitAnyAndBuildCatching<Shape<P>>(context, shape)
                    }
                    logger.debug("Shapes: {}", shapes)
                    val molecule = incarnation.createMolecule(element[moleculeKey]?.toString())
                    logger.debug("Molecule: {}", molecule)
                    val concentrationKey = DocumentRoot.Displacement.Contents.concentration
                    val concentrationMaker: () -> T = {
                        element[concentrationKey]?.toString().let { incarnation.createConcentration(it) }
                    }
                    Result.success(Triple(shapes, molecule, concentrationMaker))
                }
        }
    }

    private fun visitDependentVariable(name: String, context: Context, root: Any?): Result<DependentVariable<*>>? =
        (root as? Map<*, *>)?.let {
            if (root.containsKey(DocumentRoot.DependentVariable.formula)) {
                val formula = root[DocumentRoot.DependentVariable.formula]
                if (formula is String) {
                    val language = root[DocumentRoot.DependentVariable.language]?.toString()?.toLowerCase() ?: "groovy"
                    Result.success(JSR223Variable<Any>(language, formula))
                } else {
                    Result.success(Constant(formula))
                }
            } else {
                visitJVMConstructor(context, root)
                    ?.buildAny<DependentVariable<Any>>(context.factory)
                    ?.onFailure { logger.debug("Unable to build a dependent variable named {} from {}", name, root) }
            }
        }

    private fun visitDependentVariableRegistering(name: String, context: Context, root: Any?) =
        visitDependentVariable(name, context, root)
            ?.onSuccess { if (root is Map<*, *>) context.registerVariable(name, root) }

    @Suppress("UNCHECKED_CAST")
    private fun <T, P : Position<P>> visitEnvironment(
        incarnation: Incarnation<*, *>,
        context: Context,
        root: Any?
    ): Environment<T, P> =
        if (root == null) {
            logger.warn("No environment specified, defaulting to {}", Continuous2DEnvironment::class.simpleName)
            Continuous2DEnvironment(incarnation as Incarnation<T, Euclidean2DPosition>) as Environment<T, P>
        } else {
            visitAnyAndBuildCatching<Environment<T, P>>(context, root)?.getOrThrow()
                ?: cantBuildWith<Environment<T, P>>(root, DocumentRoot.JavaType)
        }

    private fun visitExports(incarnation: Incarnation<*, *>, context: Context, root: Any?): Result<Extractor>? =
        when {
            root is String && root.equals(DocumentRoot.Export.time, ignoreCase = true) -> Result.success(Time())
            root is Map<*, *> && DocumentRoot.Export.validateDescriptor(root) -> {
                val molecule = root[DocumentRoot.Export.molecule]?.toString()
                if (molecule == null) {
                    visitAnyAndBuildCatching<Extractor>(context, root)
                } else {
                    val property = root[DocumentRoot.Export.property]?.toString()
                    val filter: FilteringPolicy = root[DocumentRoot.Export.valueFilter]
                        ?.let { CommonFilters.fromString(it.toString()) }
                        ?: CommonFilters.NOFILTER.filteringPolicy
                    val aggregators: List<String> = visitRecursively(
                        context,
                        root[DocumentRoot.Export.aggregators] ?: emptyList<Any>()
                    ) {
                        require(it is CharSequence) {
                            "Invalid aggregator $it:${it?.let { it::class.simpleName }}. Must be a String."
                        }
                        Result.success(it.toString())
                    }
                    Result.success(MoleculeReader(molecule, property, incarnation, filter, aggregators))
                }
            }
            else -> null
        }

    private fun visitJVMConstructor(context: Context, root: Map<*, *>): JVMConstructor? =
        if (root.containsKey(DocumentRoot.JavaType.type)) {
            val type: String = root[DocumentRoot.JavaType.type].toString()
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
            is Map<*, *> -> visitAnyAndBuildCatching<Node<T>>(context, root)?.getOrThrow()
            else -> null
        } ?: cantBuildWith<Node<T>>(root)

    private fun visitParameters(context: Context, root: Any?): Either<List<*>, Map<String, *>> = when (root) {
        null -> Either.left(emptyList<Any>())
        is Iterable<*> -> Either.left(root.map { visitParameter(context, it) })
        is Map<*, *> -> Either.right(root.map { it.key.toString() to visitParameter(context, it.value) }.toMap())
        else -> Either.left(listOf(visitParameter(context, root)))
    }

    private fun visitRandomGenerator(context: Context, root: Any): RandomGenerator =
        when (root) {
            is Map<*, *> -> visitAnyAndBuildCatching<RandomGenerator>(context, root)
            else -> visitAnyAndBuildCatching<Long>(context, root) ?.map { makeDefaultRandomGenerator(it) }
        }?.onFailure { logger.error("Unable to build a random generator: {}", it.message) }
            ?.getOrThrow()
            ?: cantBuildWith<RandomGenerator>(root)

    private fun <T, P : Position<P>> visitProgram(
        simulationRNG: RandomGenerator,
        incarnation: Incarnation<T, P>,
        environment: Environment<T, P>,
        node: Node<T>,
        context: Context,
        program: Map<*, *>
    ): Result<Reaction<T>>? = if (DocumentRoot.Displacement.Program.validateDescriptor(program)) {
        val timeDistribution: TimeDistribution<T> = visitTimeDistribution(
            incarnation,
            simulationRNG,
            environment,
            node,
            context,
            program[DocumentRoot.Displacement.Program.timeDistribution]
        )
        context.factory.registerSingleton(TimeDistribution::class.java, timeDistribution)
        val reaction: Reaction<T> =
            visitReaction(simulationRNG, incarnation, environment, node, timeDistribution, context, program)
        context.factory.registerSingleton(Reaction::class.java, reaction)
        fun <R> create(parameter: Any?, makeWith: ReactionComponentFunction<T, P, R>): Result<R> = kotlin.runCatching {
            makeWith(simulationRNG, environment, node, timeDistribution, reaction, parameter?.toString())
        }
        val conditions = visitRecursively<Condition<T>>(
            context,
            program[DocumentRoot.Displacement.Program.conditions] ?: emptyList<Any>(),
            DocumentRoot.JavaType,
        ) {
            when (it) {
                is CharSequence? -> create<Condition<T>>(it, incarnation::createCondition)
                else -> visitAnyAndBuildCatching<Condition<T>>(context, it)
            }
        }
        reaction.conditions = conditions
        val actions = visitRecursively<Action<T>>(
            context,
            program[DocumentRoot.Displacement.Program.actions] ?: emptyList<Any>(),
            DocumentRoot.JavaType,
        ) {
            when (it) {
                is CharSequence? -> create<Action<T>>(it, incarnation::createAction)
                else -> visitAnyAndBuildCatching<Action<T>>(context, it)
            }
        }
        reaction.actions = actions
        context.factory.deregisterSingleton(reaction)
        context.factory.deregisterSingleton(timeDistribution)
        Result.success(reaction)
    } else {
        null
    }

    private fun <P : Position<P>, T> visitReaction(
        simulationRNG: RandomGenerator,
        incarnation: Incarnation<T, P>,
        environment: Environment<T, P>,
        node: Node<T>,
        timeDistribution: TimeDistribution<T>,
        context: Context,
        root: Map<*, *>
    ) = if (root.containsKey(DocumentRoot.Displacement.Program.program)) {
        val programDescriptor = root[DocumentRoot.Displacement.Program.program]?.toString()
        incarnation.createReaction(simulationRNG, environment, node, timeDistribution, programDescriptor)
    } else {
        visitAnyAndBuildCatching<Reaction<T>>(context, root)?.getOrThrow()
            ?: cantBuildWith<Reaction<T>>(root)
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

    private fun <P : Position<P>, T> visitTimeDistribution(
        incarnation: Incarnation<T, P>,
        simulationRNG: RandomGenerator,
        environment: Environment<T, P>,
        node: Node<T>,
        context: Context,
        root: Any?,
    ) = when (root) {
        is Map<*, *> -> visitAnyAndBuildCatching<TimeDistribution<T>>(context, root)?.getOrThrow()
            ?: cantBuildWith<TimeDistribution<T>>(root)
        else -> incarnation.createTimeDistribution(simulationRNG, environment, node, root?.toString())
    }

    private inline fun <reified T : Any> visitRecursively(
        context: Context,
        root: Any?,
        syntax: SyntaxElement? = null,
        noinline visitSingle: (Any?) -> Result<T>?
    ): List<T> = visitRecursively(T::class, context, root, syntax, visitSingle)

    private fun <T : Any> visitRecursively(
        evidence: KClass<T>,
        context: Context,
        root: Any?,
        syntax: SyntaxElement? = null,
        /**
         * Meaning:
         *  - Result.success: the target entity has been constructed correctly;
         *  - Result.failure: the target entity could not get built and an exception was generated;
         *  - null: the target entity could not get built, but there was no exception.
         */
        visitSingle: (Any?) -> Result<T>?
    ): List<T> = when (root) {
        is Iterable<*> -> root.flatMap { visitRecursively(evidence, context, it, syntax, visitSingle) }
        is Map<*, *> -> {
            logger.debug("Trying to build a {} using syntax {} from {}", evidence.simpleName, root, syntax)
            fun recurse() = visitRecursively(evidence, context, root.values, syntax, visitSingle)
            fun fail(): Nothing = cantBuildWith(evidence, root, syntax)
            fun result() = visitSingle(root)?.map { listOf(it) }
            when (syntax) {
                null -> result()?.getOrNull() ?: recurse()
                else -> {
                    if (syntax.validateDescriptor(root)) result()?.getOrThrow() ?: fail()
                    else recurse()
                }
            }
        }
        else -> visitSingle(root)?.map { listOf(it) }?.getOrThrow() ?: cantBuildWith(evidence, root, syntax)
    }

    private inline fun <reified T : Any> visitNamedRecursively(
        context: Context,
        root: Any?,
        syntax: SyntaxElement? = null,
        noinline visitSingle: (String, Any?) -> Result<T>?
    ): Map<String, T> = visitNamedRecursively(T::class, context, root, syntax, visitSingle)

    private fun <T : Any> visitNamedRecursively(
        evidence: KClass<T>,
        context: Context,
        root: Any?,
        syntax: SyntaxElement? = null,
        visitSingle: (String, Any?) -> Result<T>?
    ): Map<String, T> {
        logger.debug("Visiting: {} searching for {}", root, evidence.simpleName)
        return when (root) {
            is Map<*, *> -> visitNamedRecursivelyFromMap(evidence, context, root, syntax, visitSingle)
            is Iterable<*> ->
                root.flatMap { visitNamedRecursively(evidence, context, it, syntax, visitSingle).toList() }.toMap()
            else -> emptyMap()
        }
    }

    private fun <T : Any> visitNamedRecursivelyFromMap(
        evidence: KClass<T>,
        context: Context,
        root: Map<*, *>,
        syntax: SyntaxElement? = null,
        /*
         * Meaning:
         *  - Result.success: the target entity has been constructed correctly
         *  - Result.failure: the target entity could not get built and an exception was generated
         *  - null: the target entity could not get built, but there was no exception
         */
        visitSingle: (String, Any?) -> Result<T>?
    ): Map<String, T> =
        root.flatMap { (key, value) ->
            logger.debug("Visiting: {} searching for {}", root, evidence.simpleName)
            fun recurse(): List<Pair<String, T>> =
                visitNamedRecursively(evidence, context, value, syntax, visitSingle).toList()
            fun fail(): Nothing = cantBuildWith(evidence, value)
            fun result() = visitSingle(key.toString(), value)?.map { listOf(key.toString() to it) }
            when (value) {
                is Map<*, *> -> {
                    when {
                        syntax == null -> result()?.getOrNull() ?: recurse()
                        syntax.validateDescriptor(value) -> result()?.getOrThrow() ?: fail()
                        else -> recurse()
                    }
                }
                is Iterable<*> -> result()?.getOrNull() ?: recurse()
                else -> result()?.getOrThrow() ?: emptyList()
            }
        }.toMap()

    private data class Context constructor(
        private val namedLookup: MutableMap<String, Map<*, *>>,
        private val elementLookup: MutableMap<Map<*, *>, Any?> = mutableMapOf(),
        val factory: Factory = ObjectFactory.makeBaseFactory(),
    ) {

        private var backingConstants: MutableMap<String, Any?> = mutableMapOf()

        val constants: Map<String, Any?> get() = backingConstants

        constructor() : this(namedLookup = mutableMapOf())

        fun child(): Context = Context(namedLookup = this.namedLookup)

        fun registerConstant(name: String, representation: Map<*, *>, value: Any?) {
            logger.debug("Injecting constant {} with value {} represented by {}", name, value, representation)
            if (constants.containsKey(name)) {
                val previous = elementLookup[representation]
                require(value == previous) {
                    """
                    Inconsistent definition of constant named $name:
                      - previous evaluation: ${elementLookup[representation]}
                      - current value: $value
                    Item originating this issue: $representation
                    Context at time of failure: $this
                    """.trimIndent()
                }
            }
            namedLookup[name] = representation
            elementLookup[representation] = value
            backingConstants[name] = value
        }

        fun registerVariable(name: String, representation: Map<*, *>) {
            logger.debug("Injecting variable {} represented by {}", name, representation)
            namedLookup[name] = representation
            elementLookup[representation] = PlaceHolderForVariables
        }

        fun fixVariableValue(name: String, value: Any?) {
            val key = requireNotNull(namedLookup[name]) {
                """
                    There is no known "$name" object in the lookup table, although there should be by construction.
                    Known object names: ${namedLookup.keys}
                    This sounds like a bug in Alchemist.
                """.trimIndent()
            }
            elementLookup[key] = value
        }

        /**
         * Returns null if the element is not a resolvable entity.
         * Othewise, returns a Result with the resolution result.
         * In case types do not match, a Result failure is returned.
         */
        inline fun <reified T : Any> lookup(element: Map<*, *>): Result<T?>? =
            if (elementLookup.containsKey(element)) {
                val result = elementLookup[element]
                if (result == null) {
                    Result.success(null)
                } else {
                    if (result::class.isSubclassOf(T::class)) {
                        Result.success(T::class.cast(result))
                    } else {
                        Result.failure(
                            IllegalStateException(
                                "A request for type ${T::class.qualifiedName} has been fullfilled " +
                                    "by the context based on $element, but the result does not match the expected type"
                            )
                        )
                    }
                }
            } else {
                null
            }
    }

    private object PlaceHolderForVariables {
        override fun toString() = this::class.simpleName!!
    }
}
