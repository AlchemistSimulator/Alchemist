/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader

import arrow.core.Either
import it.unibo.alchemist.SupportedIncarnations
import it.unibo.alchemist.loader.DocumentRoot.JavaType
import it.unibo.alchemist.loader.deployments.Deployment
import it.unibo.alchemist.loader.export.Extractor
import it.unibo.alchemist.loader.export.FilteringPolicy
import it.unibo.alchemist.loader.export.MoleculeReader
import it.unibo.alchemist.loader.export.Time
import it.unibo.alchemist.loader.export.filters.CommonFilters
import it.unibo.alchemist.loader.shapes.Shape
import it.unibo.alchemist.loader.variables.Constant
import it.unibo.alchemist.loader.variables.DependentVariable
import it.unibo.alchemist.loader.variables.JSR223Variable
import it.unibo.alchemist.loader.variables.LinearVariable
import it.unibo.alchemist.loader.variables.Variable
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.implementations.linkingrules.CombinedLinkingRule
import it.unibo.alchemist.model.implementations.linkingrules.NoLinks
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Action
import it.unibo.alchemist.model.interfaces.Condition
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Incarnation
import it.unibo.alchemist.model.interfaces.Layer
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
import kotlin.reflect.KClass
import it.unibo.alchemist.loader.DocumentRoot.Deployment.Program as ProgramSyntax
import it.unibo.alchemist.loader.DocumentRoot.Layer as LayerSyntax

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

    private val logger = LoggerFactory.getLogger(SimulationModel::class.java)

    /**
     * Converts an alchemist model defined as a Map into a loadable simulation environment and relative exports.
     */
    fun fromMap(root: Map<String, *>): Loader {
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
                (element as? Map<*, *>)?.takeIf { DocumentRoot.Variable.validateDescriptor(element) }?.let {
                    if (element.containsKey(JavaType.type)) {
                        visitBuilding<Variable<*>>(context, element)
                            ?.onFailure { logger.debug("Invalid variable: {} from {}: {}", name, element, it.message) }
                            ?.takeIf { it.isSuccess }
                    } else {
                        fun Any?.toDouble(): Double = visitBuilding<Double>(context, this)
                            ?.getOrThrow()
                            ?: cantBuildWith<Double>(this)
                        runCatching {
                            LinearVariable(
                                element[DocumentRoot.Variable.default].toDouble(),
                                element[DocumentRoot.Variable.min].toDouble(),
                                element[DocumentRoot.Variable.max].toDouble(),
                                element[DocumentRoot.Variable.step].toDouble(),
                            )
                        }
                    }
                }?.also { context.registerVariable(name, element) }
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
        injectedRoot = inject(context, injectedRoot)
        val remoteDependencies =
            visitRecursively(
                context,
                injectedRoot[DocumentRoot.remoteDependencies] ?: emptyMap<String, Any>(),
            ) { visitBuilding<String>(context, it) }
        logger.info("Remote dependencies: {}", remoteDependencies)
        return object : LoadingSystem(context, injectedRoot) {
            override fun getDependentVariables() = dependentVariables
            override fun getVariables() = variables
            override fun getConstants() = context.constants
            override fun getRemoteDependencies() = remoteDependencies
        }
    }

    private fun <P : Position<P>, T : Any?> visitIncarnation(root: Any?) =
        SupportedIncarnations.get<T, P>(root.toString()).orElseThrow {
            IllegalArgumentException(
                "Invalid incarnation descriptor: $root. " +
                    "Valid incarnations are ${SupportedIncarnations.getAvailableIncarnations()}"
            )
        }

    private fun <P : Position<P>, T : Any?> visitLinkingRule(localContext: Context, root: Any?): LinkingRule<T, P> {
        val linkingRules = visitRecursively(localContext, root, JavaType) { element ->
            visitBuilding<LinkingRule<T, P>>(localContext, element)
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
    private fun inject(context: Context, root: Map<String, *>): Map<String, Any> =
        (replaceKnownRecursively(context, root) as Map<String, Any>).also { logger.debug("New model: {}", it) }

    private fun makeDefaultRandomGenerator(seed: Long) = MersenneTwister(seed)

    private fun replaceKnownRecursively(context: Context, root: Any?): Any? =
        when (root) {
            is PlaceHolderForVariables -> context.lookup(root).also { logger.debug("Set {} = {}", root.name, it) }
            is Map<*, *> -> {
                when (val lookup = context.lookup(root)) {
                    null -> root.entries.map {
                        replaceKnownRecursively(context, it.key) to replaceKnownRecursively(context, it.value)
                    }.toMap()
                    else -> lookup
                }
            }
            is Iterable<*> -> root.map { replaceKnownRecursively(context, it) }
            else -> root.also { logger.debug("Could not replace nor iterate over {}", root) }
        }

    private fun visitParameter(context: Context, root: Any?): Any? =
        when (root) {
            is Iterable<*> -> root.map { visitParameter(context, it) }
            is Map<*, *> -> context.lookup(root) ?: visitJVMConstructor(context, root) ?: root
            else -> root
        }

    private inline fun <reified T : Any> visitBuilding(context: Context, root: Any?): Result<T>? =
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
        val allContents = root[DocumentRoot.Deployment.contents] ?: emptyList<Any>()
        return visitRecursively(context, allContents) { element ->
            logger.debug("Visiting as content: {}", element)
            val moleculeKey = DocumentRoot.Deployment.Contents.molecule
            (element as? Map<*, *>)
                ?.takeIf { element.containsKey(moleculeKey) }
                ?.let {
                    logger.debug("Found content descriptor: {}", it)
                    val shapesKey = DocumentRoot.Deployment.Contents.shapes
                    val shapes = visitRecursively(context, element[shapesKey] ?: emptyList<Any>()) { shape ->
                        visitBuilding<Shape<P>>(context, shape)
                    }
                    logger.debug("Shapes: {}", shapes)
                    val moleculeElement = element[moleculeKey]
                    require(moleculeElement !is Map<*, *> && moleculeElement !is Iterable<*>) {
                        "molecule $moleculeElement:${moleculeElement!!::class.java.simpleName} is not a scalar value." +
                            "This might be caused by a missing quotation of a String."
                    }
                    val molecule = incarnation.createMolecule(moleculeElement?.toString())
                    logger.debug("Molecule: {}", molecule)
                    val concentrationKey = DocumentRoot.Deployment.Contents.concentration
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
                    val language = root[DocumentRoot.DependentVariable.language]?.toString()?.lowercase() ?: "groovy"
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
            visitBuilding<Environment<T, P>>(context, root)?.getOrThrow()
                ?: cantBuildWith<Environment<T, P>>(root, JavaType)
        }

    private fun visitExports(incarnation: Incarnation<*, *>, context: Context, root: Any?): Result<Extractor>? =
        when {
            root is String && root.equals(DocumentRoot.Export.time, ignoreCase = true) -> Result.success(Time())
            root is Map<*, *> && DocumentRoot.Export.validateDescriptor(root) -> {
                val molecule = root[DocumentRoot.Export.molecule]?.toString()
                if (molecule == null) {
                    visitBuilding<Extractor>(context, root)
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
        if (root.containsKey(JavaType.type)) {
            val type: String = root[JavaType.type].toString()
            when (val parameters = visitParameters(context, root[JavaType.parameters])) {
                is Either.Left -> OrderedParametersConstructor(type, parameters.value)
                is Either.Right -> NamedParametersConstructor(type, parameters.value)
            }
        } else {
            null
        }

    private fun <T, P : Position<P>> visitLayers(incarnation: Incarnation<T, P>, context: Context, root: Any?) =
        visitRecursively(context, root ?: emptyList<Any>(), LayerSyntax) { origin ->
            (origin as? Map<*, *>)?.let {
                visitBuilding<Layer<T, P>>(context, origin)
                    ?.map { incarnation.createMolecule(origin[LayerSyntax.molecule]?.toString()) to it }
            }
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
            is Map<*, *> -> visitBuilding<Node<T>>(context, root)?.getOrThrow()
            else -> null
        } ?: cantBuildWith<Node<T>>(root)

    private fun visitParameters(context: Context, root: Any?): Either<List<*>, Map<String, *>> = when (root) {
        null -> Either.Left(emptyList<Any>())
        is Iterable<*> -> Either.Left(root.map { visitParameter(context, it) })
        is Map<*, *> -> Either.Right(root.map { it.key.toString() to visitParameter(context, it.value) }.toMap())
        else -> Either.Left(listOf(visitParameter(context, root)))
    }

    private fun visitRandomGenerator(context: Context, root: Any): RandomGenerator =
        when (root) {
            is Map<*, *> -> visitBuilding<RandomGenerator>(context, root)
            else -> visitBuilding<Long>(context, root) ?.map { makeDefaultRandomGenerator(it) }
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
    ): Result<Reaction<T>>? = if (ProgramSyntax.validateDescriptor(program)) {
        val timeDistribution: TimeDistribution<T> = visitTimeDistribution(
            incarnation,
            simulationRNG,
            environment,
            node,
            context,
            program[ProgramSyntax.timeDistribution]
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
            program[ProgramSyntax.conditions] ?: emptyList<Any>(),
            JavaType,
        ) {
            when (it) {
                is CharSequence? -> create<Condition<T>>(it, incarnation::createCondition)
                else -> visitBuilding<Condition<T>>(context, it)
            }
        }
        if (conditions.isNotEmpty()) {
            reaction.conditions = reaction.conditions + conditions
        }
        val actions = visitRecursively<Action<T>>(
            context,
            program[ProgramSyntax.actions] ?: emptyList<Any>(),
            JavaType,
        ) {
            when (it) {
                is CharSequence? -> create<Action<T>>(it, incarnation::createAction)
                else -> visitBuilding<Action<T>>(context, it)
            }
        }
        if (actions.isNotEmpty()) {
            reaction.actions = reaction.actions + actions
        }
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
    ) = if (root.containsKey(ProgramSyntax.program)) {
        val programDescriptor = root[ProgramSyntax.program]?.toString()
        incarnation.createReaction(simulationRNG, environment, node, timeDistribution, programDescriptor)
    } else {
        visitBuilding<Reaction<T>>(context, root)?.getOrThrow()
            ?: cantBuildWith<Reaction<T>>(root)
    }

    private fun visitSeeds(context: Context, root: Any?): Seeds =
        when (root) {
            null -> makeDefaultRandomGenerator(0) to makeDefaultRandomGenerator(0)
                .also {
                    logger.warn(
                        "No seeds specified, defaulting to 0 for both {} and {}",
                        DocumentRoot.Seeds.scenario,
                        DocumentRoot.Seeds.simulation
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
        is Map<*, *> -> visitBuilding<TimeDistribution<T>>(context, root)?.getOrThrow()
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
        private val fixedVariables = mutableSetOf<String>()

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
            elementLookup[representation] = PlaceHolderForVariables(name)
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
            fixedVariables += name
            logger.debug("Contextually set {} = {}. Currently fixed: {}.", name, value, fixedVariables)
        }

        /**
         * Returns null if the element is not a resolvable entity.
         * Othewise, returns a Result with the resolution result.
         */
        fun lookup(representation: Map<*, *>): Any? =
            if (elementLookup.containsKey(representation)) elementLookup[representation] else null

        /**
         * Returns null if the element is not a resolvable entity.
         * Othewise, returns a Result with the resolution result.
         */
        fun lookup(placeholder: PlaceHolderForVariables): Any? =
            placeholder.takeUnless { placeholder.name in fixedVariables }
                ?: requireNotNull(namedLookup[placeholder.name]) {
                    "Bug in Alchemist: unresolvable variable ${placeholder.name}"
                }.let { lookup(it) }
    }

    private data class PlaceHolderForVariables(val name: String)

    private abstract class LoadingSystem(originalContext: Context, private val originalRoot: Map<String, *>) : Loader {

        private val context: Context = originalContext.child()

        override fun <T : Any?, P : Position<P>> getWith(values: Map<String, *>): EnvironmentAndExports<T, P> {
            val unknownVariableNames = values.keys - variables.keys
            require(unknownVariableNames.isEmpty()) {
                "Unknown variables provided: $unknownVariableNames." +
                    " Valid names: ${variables.keys}. Provided: ${values.keys}"
            }
            var root = originalRoot
            // VARIABLE REIFICATION
            val variableValues = this.variables.mapValues { (name, previous) ->
                if (values.containsKey(name)) values[name] else previous.default
            }
            val knownValues: Map<String, Any?> = computeAllKnownValues(constants + variableValues)
            logger.debug("Known values: {}", knownValues)
            knownValues.forEach { (name, value) -> context.fixVariableValue(name, value) }
            root = inject(context, root)
            logger.debug("Complete simulation model: {}", root)
            // SEEDS
            val (scenarioRNG, simulationRNG) = visitSeeds(context, root[DocumentRoot.seeds])
            setCurrentRandomGenerator(simulationRNG)
            // INCARNATION
            val incarnation = visitIncarnation<P, T>(root[DocumentRoot.incarnation])
            registerSingleton<Incarnation<T, P>>(incarnation)
            registerImplicit<String, Molecule>(incarnation::createMolecule)
            registerImplicit<String, Any?>(incarnation::createConcentration)
            // ENVIRONMENT
            val environment: Environment<T, P> = visitEnvironment(incarnation, context, root[DocumentRoot.environment])
            logger.info("Created environment: {}", environment)
            registerSingleton<Environment<T, P>>(environment)
            // LAYERS
            val layers: List<Pair<Molecule, Layer<T, P>>> = visitLayers(incarnation, context, root[DocumentRoot.layers])
            layers.groupBy { it.first }
                .mapValues { (_, pair) -> pair.map { it.second } }
                .forEach { (molecule, layers) ->
                    require(layers.size == 1) {
                        "Inconsistent layer definition for molecule $molecule: $layers." +
                            "There must be a single layer per molecule"
                    }
                    val layer = layers.first()
                    environment.addLayer(molecule, layer)
                    logger.debug("Pushed layer {} -> {}", molecule, layer)
                }
            // LINKING RULE
            val linkingRule = visitLinkingRule<P, T>(context, root.getOrEmptyMap(DocumentRoot.linkingRule))
            environment.linkingRule = linkingRule
            registerSingleton<LinkingRule<T, P>>(linkingRule)
            // DISPLACEMENTS
            setCurrentRandomGenerator(scenarioRNG)
            val displacementsSource = root.getOrEmpty(DocumentRoot.deployments)
            val deploymentDescriptors: List<Deployment<P>> =
                visitRecursively(context, displacementsSource, syntax = DocumentRoot.Deployment) { element ->
                    (element as? Map<*, *>)?.let {
                        setCurrentRandomGenerator(scenarioRNG)
                        visitBuilding<Deployment<P>>(context, element)?.onSuccess {
                            setCurrentRandomGenerator(simulationRNG)
                            populateDisplacement(simulationRNG, incarnation, environment, it, element)
                        }
                    }
                }
            if (deploymentDescriptors.isEmpty()) {
                logger.warn("There are no displacements in the specification, the environment won't have any node")
            } else {
                logger.debug("Deployment descriptors: {}", deploymentDescriptors)
            }
            // EXPORTS
            val exports = visitRecursively(context, root.getOrEmpty(DocumentRoot.export)) {
                visitExports(incarnation, context, it)
            }
            return EnvironmentAndExports(environment, exports)
        }

        private fun <T, P : Position<P>> populateDisplacement(
            simulationRNG: RandomGenerator,
            incarnation: Incarnation<T, P>,
            environment: Environment<T, P>,
            deployment: Deployment<P>,
            descriptor: Map<*, *>,
        ) {
            logger.debug("Processing deployment: {} with descriptor: {}", deployment, descriptor)
            val nodeDescriptor = descriptor[DocumentRoot.Deployment.nodes]
            if (descriptor.containsKey(DocumentRoot.Deployment.nodes)) {
                requireNotNull(nodeDescriptor) { "Invalid node type descriptor: $nodeDescriptor" }
                if (nodeDescriptor is Map<*, *>) {
                    JavaType.validateDescriptor(nodeDescriptor)
                }
            }
            // ADDITIONAL LINKING RULES
            deployment.getAssociatedLinkingRule<T>()?.let { newLinkingRule ->
                val composedLinkingRule = when (val linkingRule = environment.linkingRule) {
                    is NoLinks -> newLinkingRule
                    is CombinedLinkingRule -> CombinedLinkingRule(linkingRule.subRules + listOf(newLinkingRule))
                    else -> CombinedLinkingRule(listOf(linkingRule, newLinkingRule))
                }
                environment.linkingRule = composedLinkingRule
                registerSingleton<LinkingRule<T, P>>(composedLinkingRule)
            }
            val contents = visitContents(incarnation, context, descriptor)
            val programDescriptor = descriptor.getOrEmpty(DocumentRoot.Deployment.programs)
            deployment.stream().forEach { position ->
                val node = visitNode(simulationRNG, incarnation, environment, context, nodeDescriptor)
                registerSingleton<Node<T>>(node)
                // NODE CONTENTS
                contents.forEach { (shapes, molecule, concentrationMaker) ->
                    if (shapes.isEmpty() || shapes.any { position in it }) {
                        val concentration = concentrationMaker()
                        logger.debug("Injecting {} ==> {} in node {}", molecule, concentration, node.id)
                        node.setConcentration(molecule, concentration)
                    }
                }
                // PROGRAMS
                val programs = visitRecursively<Reaction<T>>(context, programDescriptor, ProgramSyntax) { program ->
                    requireNotNull(program) { "null is not a valid program in $descriptor. ${ProgramSyntax.guide}" }
                    (program as? Map<*, *>)?.let {
                        visitProgram(simulationRNG, incarnation, environment, node, context, it)
                            ?.onSuccess(node::addReaction)
                    }
                }
                logger.debug("Programs: {}", programs)
                environment.addNode(node, position)
                logger.debug("Added node {} at {}", node.id, position)
                factory.deregisterSingleton(node)
            }
        }

        private fun computeAllKnownValues(allVariableValues: Map<String, Any?>): Map<String, *> {
            val knownValues = allVariableValues.toMutableMap()
            var previousToVisitSize: Int? = null
            val toVisit = dependentVariables.toMutableMap()
            val failures = mutableListOf<Throwable>()
            while (toVisit.isNotEmpty() && toVisit.size != previousToVisitSize) {
                logger.debug("Variables to visit: {}", toVisit)
                failures.clear()
                previousToVisitSize = toVisit.size
                val iterator = toVisit.entries.iterator()
                while (iterator.hasNext()) {
                    val (name, variable) = iterator.next()
                    runCatching { variable.getWith(knownValues) }
                        .onSuccess { result ->
                            iterator.remove()
                            assert(previousToVisitSize != toVisit.size)
                            logger.debug("Created {}: {}", name, variable)
                            knownValues[name] = result
                        }
                        .onFailure { exception ->
                            failures.add(exception)
                            logger.debug("Could not create {}: {}", name, exception.message)
                        }
                }
            }
            failures.forEach { throw it }
            require(knownValues.size == constants.size + dependentVariables.size + variables.size) {
                val originalKeys = constants.keys + dependentVariables.keys + variables.keys
                val groups = listOf(knownValues.keys, originalKeys)
                val difference = groups.maxByOrNull { it.size }!! - groups.minByOrNull { it.size }
                "Something very smelly going on (a bug in Alchemist?): there are unknown variables: $difference"
            }
            return knownValues
        }

        private fun setCurrentRandomGenerator(randomGenerator: RandomGenerator) =
            factory.registerSingleton(RandomGenerator::class.java, randomGenerator)

        private val factory: Factory get() = context.factory

        private inline fun <reified T> registerSingleton(target: T) = factory.registerSingleton(T::class.java, target)
        private inline fun <reified T, reified R> registerImplicit(noinline translator: (T) -> R) =
            factory.registerImplicit(T::class.java, R::class.java, translator)
        private fun Map<*, *>.getOrEmpty(key: String) = get(key) ?: emptyList<Any>()
        private fun Map<*, *>.getOrEmptyMap(key: String) = get(key) ?: emptyMap<String, Any>()
    }
}
