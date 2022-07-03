/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.m2m

import arrow.core.Either
import it.unibo.alchemist.loader.Loader
import it.unibo.alchemist.loader.export.Exporter
import it.unibo.alchemist.loader.export.Extractor
import it.unibo.alchemist.loader.export.FilteringPolicy
import it.unibo.alchemist.loader.export.extractors.MoleculeReader
import it.unibo.alchemist.loader.export.extractors.Time
import it.unibo.alchemist.loader.export.filters.CommonFilters
import it.unibo.alchemist.loader.m2m.LoadingSystemLogger.logger
import it.unibo.alchemist.loader.m2m.syntax.DocumentRoot
import it.unibo.alchemist.loader.m2m.syntax.DocumentRoot.JavaType
import it.unibo.alchemist.loader.m2m.syntax.SyntaxElement
import it.unibo.alchemist.loader.filters.Filter
import it.unibo.alchemist.loader.variables.Constant
import it.unibo.alchemist.loader.variables.DependentVariable
import it.unibo.alchemist.loader.variables.JSR223Variable
import it.unibo.alchemist.loader.variables.LinearVariable
import it.unibo.alchemist.loader.variables.Variable
import it.unibo.alchemist.model.api.SupportedIncarnations
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.implementations.linkingrules.CombinedLinkingRule
import it.unibo.alchemist.model.implementations.linkingrules.NoLinks
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Action
import it.unibo.alchemist.model.interfaces.NodeProperty
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
import kotlin.reflect.KClass
import it.unibo.alchemist.loader.m2m.syntax.DocumentRoot.Deployment.Program as ProgramSyntax
import it.unibo.alchemist.loader.m2m.syntax.DocumentRoot.Layer as LayerSyntax
import it.unibo.alchemist.loader.m2m.syntax.DocumentRoot.DependentVariable.formula as formulaKey
import it.unibo.alchemist.loader.m2m.syntax.DocumentRoot.DependentVariable.language as languageKey
import it.unibo.alchemist.loader.m2m.syntax.DocumentRoot.DependentVariable.timeout as timeoutKey

/*
 * UTILITY ALIASES
 */
private typealias Seeds = Pair<RandomGenerator, RandomGenerator>
private typealias ReactionComponentFunction<T, P, R> =
    (RandomGenerator, Environment<T, P>, Node<T>, TimeDistribution<T>, Reaction<T>, String?) -> R

/*
 * UTILITY FUNCTIONS
 */
private inline fun <reified T> cantBuildWith(root: Any?, syntax: SyntaxElement? = null): Nothing =
    cantBuildWith(T::class, root, syntax)

private fun cantBuildWith(clazz: KClass<*>, root: Any?, syntax: SyntaxElement? = null): Nothing =
    cantBuildWith(clazz.simpleName ?: "unknown type", root, syntax)

private fun cantBuildWith(name: String, root: Any?, syntax: SyntaxElement? = null): Nothing {
    val type = root?.let { it::class.simpleName }
    val guide = syntax?.guide?.let { " A guide follows.\n$it" } ?: ""
    throw IllegalArgumentException(
        "Invalid $name specification: $root: $type.$guide"
    )
}

private fun buildJSR223Variable(name: String, language: String, formula: String, timeout: Any?) = when (timeout) {
    null -> Result.success(JSR223Variable(language, formula))
    is Number -> Result.success(JSR223Variable(language, formula, timeout.toLong()))
    is String -> Result.success(JSR223Variable(language, formula, timeout.toTimeout(name)))
    else -> throw IllegalArgumentException(
        "Invalid timeout for $name: $timeout: ${timeout::class.simpleName}"
    )
}

/*
 * UTILITY EXTENSIONS
 */
private inline fun <T> T.whenNull(and: Boolean = true, then: () -> T): T =
    if (this == null && and) then() else this

private fun Any?.coerceToDouble(context: Context): Double = SimulationModel.visitBuilding<Double>(context, this)
    ?.getOrThrow()
    ?: cantBuildWith<Double>(this)

private fun Any?.removeKeysRecursively(keys: Set<Any>): Any? = when (this) {
    null -> null
    is Map<*, *> -> (this - keys).mapValues { it.value.removeKeysRecursively(keys) }
    is Iterable<*> -> map { it.removeKeysRecursively(keys) }
    else -> this
}

private fun Map<*, *>.takeIfNotAConstant(name: String, context: Context) = takeUnless { name in context.constants }

private fun Any.validateVariableConsistencyRecursively(names: List<String> = emptyList()): Unit = when (this) {
    is Map<*, *> -> forEach { (key, value) ->
        key?.validateVariableConsistencyRecursively(names)
        value?.validateVariableConsistencyRecursively(names + key.toString())
    }
    is Iterable<*> -> forEach { it?.validateVariableConsistencyRecursively(names) }
    is SimulationModel.PlaceHolderForVariables -> throw IllegalArgumentException(
        "Variable '$name' could not be evaluated as a constant, but it is required to the definition of a variable " +
            "along this path: ${names.joinToString("->")}"
    )
    else -> Unit
}

private fun String.toTimeout(name: String): Long = runCatching { toLong() }.getOrElse {
    throw IllegalArgumentException("Invalid timeout for $name: '$this'", it)
}

/**
 * Contains the model-to-model translation between the Alchemist YAML specification and the
 * executable form of a simulation.
 */
internal object SimulationModel {

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
            val stillToTry = injectedRoot[DocumentRoot.variables]?.removeKeysRecursively(context.constants.keys)
            visitNamedRecursively(
                context = context,
                root = stillToTry ?: emptyMap<String, Any>(),
                syntax = DocumentRoot.DependentVariable,
                forceSuccess = false,
            ) { name, element -> visitConstant(name, context, element) }
            injectedRoot = inject(context, injectedRoot)
            logger.debug("{} constants: {}", context.constants.size, context.constants)
        }
        logger.info("{} constants: {}", context.constants.size, context.constants)
        val constantsNames = context.constants.keys
        val varsWithoutConsts = injectedRoot[DocumentRoot.variables].removeKeysRecursively(constantsNames)
        // Dependent variables
        val dependentVariables: Map<String, DependentVariable<*>> = visitNamedRecursively(
            context = context,
            root = varsWithoutConsts,
            syntax = DocumentRoot.DependentVariable,
            forceSuccess = false,
        ) { name, element ->
            visitDependentVariableRegistering(name, context, element)
        }
        logger.info("Dependent variables: {}", dependentVariables)
        injectedRoot = inject(context, injectedRoot)
        // Real variables
        val variablesLeft = injectedRoot[DocumentRoot.variables]
            .removeKeysRecursively(constantsNames + dependentVariables.keys)
        variablesLeft?.validateVariableConsistencyRecursively()
        val variables: Map<String, Variable<*>> = visitVariables(context, variablesLeft)
        logger.info("Variables: {}", variables)
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

    fun <P : Position<P>, T : Any?> visitIncarnation(root: Any?): Incarnation<T, P> =
        SupportedIncarnations.get<T, P>(root.toString()).orElseThrow {
            IllegalArgumentException(
                "Invalid incarnation descriptor: $root. " +
                    "Valid incarnations are ${SupportedIncarnations.getAvailableIncarnations()}"
            )
        }

    fun <P : Position<P>, T : Any?> visitLinkingRule(localContext: Context, root: Any?): LinkingRule<T, P> {
        val linkingRules = visitRecursively(localContext, root, JavaType) { element ->
            visitBuilding<LinkingRule<T, P>>(localContext, element)
        }
        return when {
            linkingRules.isEmpty() -> NoLinks()
            linkingRules.size == 1 -> linkingRules.first()
            else -> CombinedLinkingRule(linkingRules)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun inject(context: Context, root: Map<String, *>): Map<String, Any> =
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

    inline fun <reified T : Any> visitBuilding(context: Context, root: Any?): Result<T>? =
        when (root) {
            is T -> Result.success(root)
            is Map<*, *> -> visitJVMConstructor(context, root)?.buildAny(context.factory)
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

    fun <P : Position<P>> visitFilter(
        context: Context,
        element: Map<*, *>,
    ): List<Filter<P>> {
        val filterKey = DocumentRoot.Deployment.Filter.filter
        val filters = visitRecursively(context, element[filterKey] ?: emptyList<Any>()) { shape ->
            visitBuilding<Filter<P>>(context, shape)
        }
        logger.debug("Filters: {}", filters)
        return filters
    }

    fun <T, P : Position<P>> visitContents(
        incarnation: Incarnation<T, P>,
        context: Context,
        root: Map<*, *>
    ): List<Triple<List<Filter<P>>, Molecule, () -> T>> {
        logger.debug("Visiting contents: {}", root)
        val allContents = root[DocumentRoot.Deployment.contents] ?: emptyList<Any>()
        return visitRecursively(context, allContents) { element ->
            logger.debug("Visiting as content: {}", element)
            val moleculeKey = DocumentRoot.Deployment.Contents.molecule
            (element as? Map<*, *>)
                ?.takeIf { element.containsKey(moleculeKey) }
                ?.let {
                    logger.debug("Found content descriptor: {}", it)
                    val filters = visitFilter<P>(context, element)
                    val moleculeElement = element[moleculeKey]
                    require(moleculeElement !is Map<*, *> && moleculeElement !is Iterable<*>) {
                        val type = moleculeElement?.let { ": " + it::class.simpleName } ?: ""
                        "molecule $moleculeElement$type is not a scalar value." +
                            "This might be caused by a missing quotation of a String."
                    }
                    val molecule = incarnation.createMolecule(moleculeElement?.toString())
                    logger.debug("Molecule: {}", molecule)
                    val concentrationKey = DocumentRoot.Deployment.Contents.concentration
                    val concentrationMaker: () -> T = {
                        element[concentrationKey]?.toString().let { incarnation.createConcentration(it) }
                    }
                    Result.success(Triple(filters, molecule, concentrationMaker))
                }
        }
    }

    fun <T, P : Position<P>> visitProperty(
        context: Context,
        root: Map<*, *>,
    ): List<Pair<List<Filter<P>>, NodeProperty<T>>> {
        logger.debug("Visiting properties: {}", root)
        val capabilitiesKey = DocumentRoot.Deployment.properties
        val allCapabilities = root[capabilitiesKey] ?: emptyList<Any>()
        return visitRecursively(context, allCapabilities, DocumentRoot.Deployment.Property) { element ->
            (element as? Map<*, *>)?.let {
                val filters = visitFilter<P>(context, element)
                val nodeProperty = visitBuilding<NodeProperty<T>>(context, element)
                    ?.getOrThrow()
                    ?: cantBuildWith<NodeProperty<T>>(root, JavaType)
                logger.debug("Property: {}", nodeProperty)
                Result.success(Pair(filters, nodeProperty))
            }
        }
    }

    private fun visitDependentVariable(name: String, context: Context, root: Any?): Result<DependentVariable<*>>? {
        val descriptor = (root as? Map<*, *>)?.takeIfNotAConstant(name, context)
        return when {
            descriptor == null -> null
            root.containsKey(formulaKey) -> when (val formula = root[formulaKey]) {
                null -> Result.success(Constant(null))
                is String -> buildJSR223Variable(
                    name,
                    root[languageKey]?.toString()?.lowercase() ?: "groovy",
                    formula,
                    root[timeoutKey]
                )
                is Number -> Result.success(Constant(formula))
                is List<*> -> Result.success(Constant(formula))
                is Map<*, *> -> throw IllegalArgumentException(
                    """
                    Error on variable $name: associating YAML maps to dependent variables can lead to ambiguous code.
                    If you truly mean to associate a map to $name, go through Groovy or any other supported language:
                    $name:
                      formula: |
                        [${formula.keys.joinToString { "$it: ..." }}]
                    
                    See: https://bit.ly/groovy-map-literals
                    """.trimIndent()
                )
                else -> throw IllegalArgumentException(
                    "Unexpected type ${formula::class.simpleName} for variable $name"
                )
            }
            else -> visitJVMConstructor(context, root)
                ?.takeIf { TypeSearch.typeNamed<DependentVariable<Any>>(it.typeName).subOptimalMatches.isNotEmpty() }
                ?.buildAny<DependentVariable<Any>>(context.factory)
                ?.onFailure { logger.debug("Unable to build a dependent variable named {} from {}", name, root) }
        }
    }

    private fun visitDependentVariableRegistering(name: String, context: Context, root: Any?) =
        visitDependentVariable(name, context, root)
            ?.onSuccess { if (root is Map<*, *>) context.registerVariable(name, root) }

    @Suppress("UNCHECKED_CAST")
    fun <T, P : Position<P>> visitEnvironment(
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

    @Suppress("UNCHECKED_CAST")
    private fun <E : Any> visitExportData(
        incarnation: Incarnation<*, *>,
        context: Context,
        root: Any?
    ): Result<Extractor<E>>? =
        when {
            root is String && root.equals(DocumentRoot.Export.Data.time, ignoreCase = true) ->
                Result.success(Time())
            root is Map<*, *> && DocumentRoot.Export.Data.validateDescriptor(root) -> {
                val molecule = root[DocumentRoot.Export.Data.molecule]?.toString()
                if (molecule == null) {
                    visitBuilding(context, root)
                } else {
                    val property = root[DocumentRoot.Export.Data.property]?.toString()
                    val filter: FilteringPolicy = root[DocumentRoot.Export.Data.valueFilter]
                        ?.let { CommonFilters.fromString(it.toString()) }
                        ?: CommonFilters.NOFILTER.filteringPolicy
                    val aggregators: List<String> = visitRecursively(
                        context,
                        root[DocumentRoot.Export.Data.aggregators] ?: emptyList<Any>()
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
        } as Result<Extractor<E>>?

    fun <T, P : Position<P>> visitSingleExporter(
        incarnation: Incarnation<*, *>,
        context: Context,
        root: Any?,
    ): Result<Exporter<T, P>>? =
        when {
            root is Map<*, *> && DocumentRoot.Export.validateDescriptor(root) -> {
                val exporter = visitBuilding<Exporter<T, P>>(context, root)
                    ?.getOrThrow() ?: cantBuildWith<Exporter<T, P>>(root)
                val dataExtractors = visitRecursively(context, root[DocumentRoot.Export.data]) {
                    visitExportData<Any>(incarnation, context, it)
                }
                exporter.bindDataExtractors(dataExtractors)
                Result.success(exporter)
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

    fun <T, P : Position<P>> visitLayers(incarnation: Incarnation<T, P>, context: Context, root: Any?) =
        visitRecursively(context, root ?: emptyList<Any>(), LayerSyntax) { origin ->
            (origin as? Map<*, *>)?.let {
                visitBuilding<Layer<T, P>>(context, origin)
                    ?.map { incarnation.createMolecule(origin[LayerSyntax.molecule]?.toString()) to it }
            }
        }

    fun <T, P : Position<P>> visitNode(
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

    fun visitParameters(context: Context, root: Any?): Either<List<*>, Map<String, *>> = when (root) {
        null -> Either.Left(emptyList<Any>())
        is Iterable<*> -> Either.Left(root.map { visitParameter(context, it) })
        is Map<*, *> -> Either.Right(root.map { it.key.toString() to visitParameter(context, it.value) }.toMap())
        else -> Either.Left(listOf(visitParameter(context, root)))
    }

    fun visitRandomGenerator(context: Context, root: Any): RandomGenerator =
        when (root) {
            is Map<*, *> -> visitBuilding<RandomGenerator>(context, root)
            else -> visitBuilding<Long>(context, root) ?.map { makeDefaultRandomGenerator(it) }
        }?.onFailure { logger.error("Unable to build a random generator: {}", it.message) }
            ?.getOrThrow()
            ?: cantBuildWith<RandomGenerator>(root)

    fun <T, P : Position<P>> visitProgram(
        simulationRNG: RandomGenerator,
        incarnation: Incarnation<T, P>,
        environment: Environment<T, P>,
        node: Node<T>,
        context: Context,
        program: Map<*, *>,
    ): Result<Pair<List<Filter<P>>, Reaction<T>>>? = if (ProgramSyntax.validateDescriptor(program)) {
        val timeDistribution: TimeDistribution<T> = visitTimeDistribution(
            incarnation,
            simulationRNG,
            environment,
            context,
            program[ProgramSyntax.timeDistribution]
        )
        context.factory.registerSingleton(TimeDistribution::class.java, timeDistribution)
        val reaction: Reaction<T> =
            visitReaction(simulationRNG, incarnation, environment, node, timeDistribution, context, program)
        context.factory.registerSingleton(Reaction::class.java, reaction)
        fun <R> create(parameter: Any?, makeWith: ReactionComponentFunction<T, P, R>): Result<R> = runCatching {
            makeWith(simulationRNG, environment, node, timeDistribution, reaction, parameter?.toString())
        }
        val filters = visitFilter<P>(context, program)
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
        Result.success(Pair(filters, reaction))
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
        root: Map<*, *>,
    ) = if (root.containsKey(ProgramSyntax.program)) {
        val programDescriptor = root[ProgramSyntax.program]?.toString()
        incarnation.createReaction(simulationRNG, environment, node, timeDistribution, programDescriptor)
    } else {
        visitBuilding<Reaction<T>>(context, root)?.getOrThrow()
            ?: cantBuildWith<Reaction<T>>(root)
    }

    fun visitSeeds(context: Context, root: Any?): Seeds =
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
                    "Illegal seeds sub-keys: ${root.keys - stringKeys}. Valid keys are: $stringKeys"
                }
                val validKeys = DocumentRoot.Seeds.validKeys
                val nonPrivateKeys = stringKeys.filterNot { it.startsWith("_") }
                require(nonPrivateKeys.all { it in validKeys }) {
                    "Illegal seeds sub-keys: ${nonPrivateKeys - validKeys.toSet()}. Valid keys are: $validKeys"
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
        context: Context,
        root: Any?,
    ) = when (root) {
        is Map<*, *> -> visitBuilding<TimeDistribution<T>>(context, root)?.getOrThrow()
            ?: cantBuildWith<TimeDistribution<T>>(root)
        else -> incarnation.createTimeDistribution(simulationRNG, environment, root?.toString())
    }

    fun visitVariables(context: Context, root: Any?): Map<String, Variable<*>> = visitNamedRecursively(
        context,
        root,
        syntax = DocumentRoot.Variable,
    ) { name, element ->
        (element as? Map<*, *>?)
            ?.takeIfNotAConstant(name, context)
            ?.takeIf { DocumentRoot.Variable.validateDescriptor(element) }
            ?.let {
                val variable = when (JavaType.type) {
                    in element -> visitBuilding<Variable<*>>(context, element) // arbitrary type
                        ?.onFailure { logger.debug("Invalid variable: {} from {}: {}", name, element, it.message) }
                    else -> runCatching { // Must be a linear variable, or else fail
                        fun Any?.toDouble(): Double = coerceToDouble(context)
                        LinearVariable(
                            element[DocumentRoot.Variable.default].toDouble(),
                            element[DocumentRoot.Variable.min].toDouble(),
                            element[DocumentRoot.Variable.max].toDouble(),
                            element[DocumentRoot.Variable.step].toDouble(),
                        )
                    }
                }
                variable?.onSuccess { context.registerVariable(name, element) }
            }
    }

    inline fun <reified T : Any> visitRecursively(
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
        syntax: SyntaxElement,
        forceSuccess: Boolean = true,
        noinline visitSingle: (String, Any?) -> Result<T>?
    ): Map<String, T> = visitNamedRecursively(T::class, context, root, syntax, forceSuccess, visitSingle)

    private fun <T : Any> visitNamedRecursively(
        evidence: KClass<T>,
        context: Context,
        root: Any?,
        syntax: SyntaxElement,
        forceSuccess: Boolean = true,
        visitSingle: (String, Any?) -> Result<T>?
    ): Map<String, T> {
        logger.debug("Visiting: {} searching for {}", root, evidence.simpleName)
        return when (root) {
            is Map<*, *> -> visitNamedRecursivelyFromMap(evidence, context, root, syntax, forceSuccess, visitSingle)
            is Iterable<*> -> root.flatMap {
                visitNamedRecursively(evidence, context, it, syntax, forceSuccess, visitSingle).toList()
            }.toMap()
            else -> emptyMap()
        }
    }

    private fun <T : Any> visitNamedRecursivelyFromMap(
        evidence: KClass<T>,
        context: Context,
        root: Map<*, *>,
        syntax: SyntaxElement,
        forceSuccess: Boolean = true,
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
                visitNamedRecursively(evidence, context, value, syntax, forceSuccess, visitSingle).toList()
            fun result() = visitSingle(key.toString(), value)?.map { listOf(key.toString() to it) }
            when (value) {
                is Map<*, *> -> when {
                    syntax.validateDescriptor(value) -> result()
                        ?.getOrThrow()
                        .whenNull(and = forceSuccess) { cantBuildWith(evidence, value) }
                        ?: emptyList()
                    else -> recurse()
                }
                is Iterable<*> -> result()?.getOrNull() ?: recurse()
                else -> result()?.getOrThrow() ?: emptyList()
            }
        }.toMap()

    internal data class PlaceHolderForVariables(val name: String)
}
