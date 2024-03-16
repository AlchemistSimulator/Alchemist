/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.loader

import arrow.core.Either
import it.unibo.alchemist.boundary.DependentVariable
import it.unibo.alchemist.boundary.ExportFilter
import it.unibo.alchemist.boundary.Exporter
import it.unibo.alchemist.boundary.Extractor
import it.unibo.alchemist.boundary.Launcher
import it.unibo.alchemist.boundary.Loader
import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.boundary.Variable
import it.unibo.alchemist.boundary.exportfilters.CommonFilters
import it.unibo.alchemist.boundary.extractors.MoleculeReader
import it.unibo.alchemist.boundary.extractors.Time
import it.unibo.alchemist.boundary.launchers.DefaultLauncher
import it.unibo.alchemist.boundary.loader.LoadingSystemLogger.logger
import it.unibo.alchemist.boundary.loader.syntax.DocumentRoot
import it.unibo.alchemist.boundary.loader.syntax.DocumentRoot.JavaType
import it.unibo.alchemist.boundary.loader.syntax.SyntaxElement
import it.unibo.alchemist.boundary.loader.util.JVMConstructor
import it.unibo.alchemist.boundary.loader.util.NamedParametersConstructor
import it.unibo.alchemist.boundary.loader.util.OrderedParametersConstructor
import it.unibo.alchemist.boundary.loader.util.TypeSearch
import it.unibo.alchemist.boundary.variables.Constant
import it.unibo.alchemist.boundary.variables.JSR223Variable
import it.unibo.alchemist.boundary.variables.LinearVariable
import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.GlobalReaction
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Layer
import it.unibo.alchemist.model.LinkingRule
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.PositionBasedFilter
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.linkingrules.CombinedLinkingRule
import it.unibo.alchemist.model.linkingrules.NoLinks
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.random.RandomGenerator
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName
import it.unibo.alchemist.boundary.loader.syntax.DocumentRoot.DependentVariable.formula as formulaKey
import it.unibo.alchemist.boundary.loader.syntax.DocumentRoot.DependentVariable.language as languageKey
import it.unibo.alchemist.boundary.loader.syntax.DocumentRoot.DependentVariable.timeout as timeoutKey
import it.unibo.alchemist.boundary.loader.syntax.DocumentRoot.Deployment.Program as ProgramSyntax
import it.unibo.alchemist.boundary.loader.syntax.DocumentRoot.Environment.GlobalProgram as GlobalProgramSyntax
import it.unibo.alchemist.boundary.loader.syntax.DocumentRoot.Layer as LayerSyntax

/*
 * UTILITY ALIASES
 */
private typealias Seeds = Pair<RandomGenerator, RandomGenerator>
private typealias ReactionComponentFunction<T, P, R> =
    (RandomGenerator, Environment<T, P>, Node<T>?, TimeDistribution<T>, Actionable<T>, Any?) -> R

/*
 * UTILITY FUNCTIONS
 */
private inline fun <reified T> cantBuildWith(
    root: Any?,
    syntax: SyntaxElement? = null,
    error: Throwable? = null,
): Nothing = cantBuildWith(T::class, root, syntax, error)

private fun cantBuildWith(
    clazz: KClass<*>,
    root: Any?,
    syntax: SyntaxElement? = null,
    error: Throwable? = null,
): Nothing = cantBuildWith(clazz.simpleName ?: "unknown type", root, syntax, error)

private fun cantBuildWith(name: String, root: Any?, syntax: SyntaxElement? = null, error: Throwable? = null): Nothing {
    val type = root?.let { it::class.simpleName }
    val guide = syntax?.guide?.let { " A guide follows.\n$it" }.orEmpty()
    val message = "Invalid $name specification: $root: $type.$guide"
    if (error == null) {
        error(message)
    }
    val suppressed = error.suppressed.takeIf { it.isNotEmpty() }?.joinToString { "  - ${it.message}\n" }
        ?.let { "\nPreviously encountered non-fatal errors that may have caused this one as a consequence:\n$it" }
        .orEmpty()
    throw IllegalStateException("$message\nProximal cause: ${error.message}$suppressed", error)
}

private fun buildJSR223Variable(name: String, language: String, formula: String, timeout: Any?) = when (timeout) {
    null -> Result.success(JSR223Variable(language, formula))
    is Number -> Result.success(JSR223Variable(language, formula, timeout.toLong()))
    is String -> Result.success(JSR223Variable(language, formula, timeout.toTimeout(name)))
    else -> error("Invalid timeout for $name: $timeout: ${timeout::class.simpleName}")
}

/*
 * UTILITY EXTENSIONS
 */
private inline fun <T> T.whenNull(and: Boolean = true, then: () -> T): T = if (this == null && and) then() else this

private fun Any?.coerceToDouble(context: Context): Double =
    SimulationModel.visitBuilding<Double>(context, this)?.getOrThrow() ?: cantBuildWith<Double>(this)

private fun Any?.removeKeysRecursively(keys: Set<Any>): Any? = when (this) {
    null -> null
    is Map<*, *> -> (this - keys).mapValues { it.value.removeKeysRecursively(keys) }
    is Iterable<*> -> map { it.removeKeysRecursively(keys) }
    else -> this
}

private fun Map<*, *>.takeIfNotAConstant(name: String, context: Context) = takeUnless { name in context.constants }

private fun Any.validateVariableConsistencyRecursively(
    names: List<String> = emptyList(),
    errors: Map<String, List<Throwable>>,
): Unit = when (this) {
    is Map<*, *> -> forEach { (key, value) ->
        key?.validateVariableConsistencyRecursively(names, errors)
        value?.validateVariableConsistencyRecursively(names + key.toString(), errors)
    }

    is Iterable<*> -> forEach { it?.validateVariableConsistencyRecursively(names, errors) }
    is SimulationModel.PlaceHolderForVariables -> {
        val message =
            "Variable '$name' could not be evaluated as a constant, " + "but it is required to the definition of a variable along path ${
                names.joinToString("->")
            }"
        val related = errors[name]?.mapIndexed { index, error -> "$index. ${error.message}" }?.distinct()
            ?.joinToString(prefix = "Possibly related causes:\n", separator = "\n")
        error("$message\n${related.orEmpty()}")
    }

    else -> Unit
}

private fun String.toTimeout(name: String): Long = runCatching { toLong() }.getOrElse {
    throw IllegalArgumentException("Invalid timeout for $name: '$this'", it)
}

/**
 * Contains the model-to-model translation between the Alchemist YAML specification and the
 * executable form of a simulation.
 */
@Suppress("LargeClass")
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
        val collectedNonFatalFailures = mutableMapOf<String, List<Throwable>>()
        while (context.constants.size != previousSize) {
            previousSize = context.constants.size
            val stillToTry = injectedRoot[DocumentRoot.variables]?.removeKeysRecursively(context.constants.keys)
            visitNamedRecursively(
                context = context,
                root = stillToTry ?: emptyMap<String, Any>(),
                syntax = DocumentRoot.DependentVariable,
                forceSuccess = false,
            ) { name, element ->
                val evaluationAsConstant = visitConstant(name, context, element)
                when {
                    evaluationAsConstant == null -> null
                    evaluationAsConstant.isSuccess -> evaluationAsConstant
                    else -> null.also { _ ->
                        evaluationAsConstant.onFailure {
                            collectedNonFatalFailures[name] = collectedNonFatalFailures[name].orEmpty() + it
                        }
                    }
                }
            }
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
        val variablesLeft =
            injectedRoot[DocumentRoot.variables].removeKeysRecursively(constantsNames + dependentVariables.keys)
        variablesLeft?.validateVariableConsistencyRecursively(errors = collectedNonFatalFailures)
        val variables: Map<String, Variable<*>> = visitVariables(context, variablesLeft)
        logger.info("Variables: {}", variables)
        var launcherDescriptor = injectedRoot[DocumentRoot.launcher]
        fun Map<*, *>.isJvmConstructorWithoutType() = containsKey(JavaType.parameters) && !containsKey(JavaType.type)
        if (launcherDescriptor is Map<*, *> && launcherDescriptor.isJvmConstructorWithoutType()) {
            launcherDescriptor += JavaType.type to DefaultLauncher::class.simpleName.orEmpty()
        }
        val launcher: Launcher = visitBuilding<Launcher>(context, launcherDescriptor)?.getOrThrow() ?: DefaultLauncher()
        injectedRoot = inject(context, injectedRoot)
        val remoteDependencies = visitRecursively(
            context,
            injectedRoot[DocumentRoot.remoteDependencies] ?: emptyMap<String, Any>(),
        ) { visitBuilding<String>(context, it) }
        logger.info("Remote dependencies: {}", remoteDependencies)
        return object : LoadingSystem(context, injectedRoot) {
            override val constants: Map<String, Any?> = context.constants
            override val remoteDependencies: List<String> = remoteDependencies
            override val launcher: Launcher = launcher
            override val dependentVariables: Map<String, DependentVariable<*>> = dependentVariables
            override val variables: Map<String, Variable<*>> = variables
        }
    }

    internal fun <P : Position<out P>, T : Any?> visitIncarnation(root: Any?): Incarnation<T, P> =
        SupportedIncarnations.get<T, P>(root.toString()).orElseThrow {
            IllegalArgumentException(
                "Invalid incarnation descriptor: $root. " +
                    "Valid incarnations are ${SupportedIncarnations.getAvailableIncarnations()}",
            )
        }

    internal fun <P : Position<P>, T : Any?> visitLinkingRule(localContext: Context, root: Any?): LinkingRule<T, P> {
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
    internal fun inject(context: Context, root: Map<String, *>): Map<String, Any> =
        (replaceKnownRecursively(context, root) as Map<String, Any>).also { logger.debug("New model: {}", it) }

    private fun makeDefaultRandomGenerator(seed: Long) = MersenneTwister(seed)

    private fun replaceKnownRecursively(context: Context, root: Any?): Any? = when (root) {
        is PlaceHolderForVariables -> context.lookup(root).also { logger.debug("Set {} = {}", root.name, it) }
        is Map<*, *> -> {
            when (val lookup = context.lookup(root)) {
                null -> root.entries.associate {
                    replaceKnownRecursively(context, it.key) to replaceKnownRecursively(context, it.value)
                }

                else -> lookup
            }
        }
        is Iterable<*> -> root.map { replaceKnownRecursively(context, it) }
        else -> root.also { logger.debug("Could not replace nor iterate over {}", root) }
    }

    private fun visitParameter(context: Context, root: Any?): Any? = when (root) {
        is Iterable<*> -> root.map { visitParameter(context, it) }
        is Map<*, *> -> context.lookup(root) ?: visitJVMConstructor(context, root) ?: root
        else -> root
    }

    internal inline fun <reified T : Any> visitBuilding(context: Context, root: Map<*, *>): Result<T>? =
        visitJVMConstructor(context, root)
            ?.also { constructor: JVMConstructor ->
                val params = when (constructor) {
                    is OrderedParametersConstructor -> constructor.parameters
                    is NamedParametersConstructor -> constructor.parametersMap.values
                }
                for (param in params) {
                    if (param is PlaceHolderForVariables) {
                        error(
                            """
                                Attempted construction of a ${T::class.simpleName} with an unresolvable variable
                                §placeholder referring to '${param.name}: $root'.
                                This usually happens when a variable depends on another variable,
                                §check that no other variable is using the variable named '${param.name}'.
                                Variables can depend solely on constants to prevent circular dependencies.
                            """.trimIndent().replace(Regex("\\R§"), " "),
                        )
                    }
                }
            }
            ?.buildAny(context.factory)

    private fun <T> Iterable<T>.deepFlatten(): List<*> = flatMap {
        when (it) {
            is Iterable<*> -> it.deepFlatten()
            else -> listOf(it)
        }
    }

    private inline fun <reified T> Iterable<*>.extractOne(): Any? {
        val flattened = deepFlatten()
        check(flattened.size == 1) {
            """
            Alchemist was requested to build a single ${T::class.simpleName} using a ${this::class.simpleName}
            of ${count()} elements with contents $this, which Alchemist tried to flatten obtaining $flattened,
            which contains ${flattened.size} elements, thus ending up into an ambiguous state.
            Replace the collection with a single object, or use a collection with a single element.
            """.trimIndent().replace(Regex("\\R"), " ")
        }
        return flattened.first()
    }

    private inline fun <reified T : Any> visitBuildingExcludingIterable(
        context: Context,
        root: Any?,
    ): Result<T>? = when (root) {
        is T -> Result.success(root)
        is Map<*, *> -> visitBuilding(context, root)
        null -> null
        else -> {
            logger.debug("Unable to build a {} with {}, attempting a JIRF conversion ", root, T::class.simpleName)
            context.factory.convert(T::class.java, root).map { Result.success(it) }.orElseGet {
                Result.failure(
                    IllegalArgumentException(
                        """Unable to convert $root into a ${T::class.simpleName}""",
                    ),
                )
            }
        }
    }

    internal inline fun <reified T : Any> visitBuilding(context: Context, root: Any?): Result<T>? = when (root) {
        is T -> Result.success(root)
        is Iterable<*> -> {
            logger.warn(
                "Alchemist is trying to build a single {} from a collection of type {}: {}. " +
                    "Even if the operation succeeds, you should make your configuration clearer using a single object.",
                T::class.simpleName,
                root::class.simpleName,
                root,
            )
            visitBuildingExcludingIterable(context, root.extractOne<T>())
        }
        else -> visitBuildingExcludingIterable(context, root)
    }

    private fun visitConstant(name: String, context: Context, root: Any?): Result<Constant<*>>? {
        val constant: Result<Constant<*>>? = when (root) {
            is Map<*, *> -> {
                visitDependentVariable(name, context, root)?.mapCatching { it.getWith(context.constants) }
                    ?.onFailure { logger.debug("Evaluation failed at {}, context {}:\n{}", root, context, it.message) }
                    ?.onSuccess { context.registerConstant(name, root, it) }?.map { Constant(it) }
            }

            else -> null
        }
        constant?.onSuccess {
            logger.debug("Variable {}, evaluated from {} as constant, returned {}", name, root, it.value)
            check(!context.constants.containsKey(name) || context.constants[name] == it.value) {
                """
                Inconsistent definition of variables named $name:
                  - previous evaluation: ${context.constants[name]}
                  - current value: $constant
                Item originating this issue: $root
                Context at time of failure: $context
                """.trimIndent()
            }
        }
        return constant
    }

    private fun <P : Position<P>> visitFilter(
        context: Context,
        element: Map<*, *>,
    ): List<PositionBasedFilter<P>> {
        val filterKey = DocumentRoot.Deployment.Filter.filter
        val positionBasedFilters = visitRecursively(context, element[filterKey] ?: emptyList<Any>()) { shape ->
            visitBuilding<PositionBasedFilter<P>>(context, shape)
        }
        logger.debug("Filters: {}", positionBasedFilters)
        return positionBasedFilters
    }

    internal fun <T, P : Position<P>> visitContents(
        incarnation: Incarnation<T, P>,
        context: Context,
        root: Map<*, *>,
    ): List<Triple<List<PositionBasedFilter<P>>, Molecule, () -> T>> {
        logger.debug("Visiting contents: {}", root)
        val allContents = root[DocumentRoot.Deployment.contents] ?: emptyList<Any>()
        return visitRecursively(context, allContents) { element ->
            logger.debug("Visiting as content: {}", element)
            val moleculeKey = DocumentRoot.Deployment.Contents.molecule
            (element as? Map<*, *>)?.takeIf { element.containsKey(moleculeKey) }?.let { contentDescriptor ->
                logger.debug("Found content descriptor: {}", contentDescriptor)
                val filters = visitFilter<P>(context, element)
                val moleculeElement = element[moleculeKey]
                require(moleculeElement !is Map<*, *> && moleculeElement !is Iterable<*>) {
                    val type = moleculeElement?.let { ": " + it::class.simpleName }.orEmpty()
                    "molecule $moleculeElement$type is not a scalar value." +
                        "This might be caused by a missing quotation of a String."
                }
                val molecule = incarnation.createMolecule(moleculeElement?.toString())
                logger.debug("Molecule: {}", molecule)
                val concentrationKey = DocumentRoot.Deployment.Contents.concentration
                val concentrationMaker: () -> T = {
                    incarnation.createConcentration(element[concentrationKey])
                }
                Result.success(Triple(filters, molecule, concentrationMaker))
            }
        }
    }

    internal fun <T, P : Position<P>> visitProperty(
        context: Context,
        root: Map<*, *>,
    ): List<Pair<List<PositionBasedFilter<P>>, NodeProperty<T>>> {
        logger.debug("Visiting properties: {}", root)
        val capabilitiesKey = DocumentRoot.Deployment.properties
        val allCapabilities = root[capabilitiesKey] ?: emptyList<Any>()
        return visitRecursively(context, allCapabilities, DocumentRoot.Deployment.Property) { element ->
            (element as? Map<*, *>)?.let {
                val filters = visitFilter<P>(context, element)
                val nodeProperty =
                    visitBuilding<NodeProperty<T>>(context, element)?.getOrThrow() ?: cantBuildWith<NodeProperty<T>>(
                        root,
                        JavaType,
                    )
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
                    root[timeoutKey],
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
                    """.trimIndent(),
                )

                else -> throw IllegalArgumentException(
                    "Unexpected type ${formula::class.simpleName} for variable $name",
                )
            }

            else -> visitJVMConstructor(
                context,
                root,
            )?.takeIf { TypeSearch.typeNamed<DependentVariable<Any>>(it.typeName).subOptimalMatches.isNotEmpty() }
                ?.buildAny<DependentVariable<Any>>(context.factory)
                ?.onFailure { logger.debug("Unable to build a dependent variable named {} from {}", name, root) }
        }
    }

    private fun visitDependentVariableRegistering(name: String, context: Context, root: Any?) =
        visitDependentVariable(name, context, root)?.onSuccess {
            if (root is Map<*, *>) {
                context.registerVariable(
                    name,
                    root,
                )
            }
        }

    @Suppress("UNCHECKED_CAST")
    fun <T, P : Position<P>> visitEnvironment(
        incarnation: Incarnation<*, *>,
        context: Context,
        root: Any?,
    ): Environment<T, P> = if (root == null) {
        logger.warn("No environment specified, defaulting to {}", Continuous2DEnvironment::class.simpleName)
        Continuous2DEnvironment(incarnation as Incarnation<T, Euclidean2DPosition>) as Environment<T, P>
    } else {
        visitBuilding<Environment<T, P>>(context, root)?.getOrThrow() ?: cantBuildWith<Environment<T, P>>(
            root, JavaType,
        )
    }

    @Suppress("UNCHECKED_CAST", "CyclomaticComplexMethod")
    private fun <E : Any> visitExportData(
        incarnation: Incarnation<*, *>,
        context: Context,
        root: Any?,
    ): Result<Extractor<E>>? = when {
        root is String && root.equals(DocumentRoot.Export.Data.time, ignoreCase = true) -> Result.success(Time())

        root is Map<*, *> && DocumentRoot.Export.Data.validateDescriptor(root) -> {
            val molecule = root[DocumentRoot.Export.Data.molecule]?.toString()
            if (molecule == null) {
                visitBuilding<Extractor<E>>(context, root)
            } else {
                val property = root[DocumentRoot.Export.Data.property]?.toString()
                val filter: ExportFilter =
                    root[DocumentRoot.Export.Data.valueFilter]?.let { CommonFilters.fromString(it.toString()) }
                        ?: CommonFilters.NOFILTER.filteringPolicy
                val precision: Int? = when (val digits = root[DocumentRoot.Export.Data.precision]) {
                    null -> null
                    is Byte -> digits.toInt()
                    is Short -> digits.toInt()
                    is Int -> digits
                    is Number -> {
                        logger.warn(
                            "Coercing {} {} to integer, potential precision loss.",
                            digits::class.simpleName ?: digits::class.jvmName,
                            digits,
                        )
                        digits.toInt()
                    }

                    else -> runCatching { digits.toString().toInt() }.getOrElse { exception ->
                        throw IllegalArgumentException(
                            "Invalid digit precision: '$digits' (type: ${digits::class.simpleName})." +
                                "Must be an integer number, or parseable to an integer number.",
                            exception,
                        )
                    }
                }
                val aggregators: List<String> = visitRecursively(
                    context,
                    root[DocumentRoot.Export.Data.aggregators] ?: emptyList<Any>(),
                ) {
                    require(it is CharSequence) {
                        "Invalid aggregator $it:${it?.let { it::class.simpleName }}. Must be a String."
                    }
                    Result.success(it.toString())
                }
                Result.success(MoleculeReader(molecule, property, incarnation, filter, aggregators, precision))
            }
        }

        else -> null
    } as Result<Extractor<E>>?

    fun <T, P : Position<P>> visitSingleExporter(
        incarnation: Incarnation<*, *>,
        context: Context,
        root: Any?,
    ): Result<Exporter<T, P>>? = when {
        root is Map<*, *> && DocumentRoot.Export.validateDescriptor(root) -> {
            val exporter =
                visitBuilding<Exporter<T, P>>(context, root)?.getOrThrow() ?: cantBuildWith<Exporter<T, P>>(root)
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
            (origin as? Map<*, *>)?.let { _ ->
                visitBuilding<Layer<T, P>>(
                    context,
                    origin,
                )?.map { incarnation.createMolecule(origin[LayerSyntax.molecule]?.toString()) to it }
            }
        }

    fun <P : Position<P>, T> visitOutputMonitors(
        context: Context,
        root: Any?,
    ): List<OutputMonitor<T, P>> =
        visitRecursively(context, root ?: emptyList<Any>(), DocumentRoot.Monitor) { origin ->
            (origin as? Map<*, *>)?.let { _ ->
                visitBuilding<OutputMonitor<T, P>>(context, origin)
            }
        }
    fun <T, P : Position<P>> visitNode(
        randomGenerator: RandomGenerator,
        incarnation: Incarnation<T, P>,
        environment: Environment<T, P>,
        context: Context,
        root: Any?,
    ): Node<T> = when {
        root is Map<*, *> && root.containsKey(JavaType.type) -> visitBuilding<Node<T>>(context, root)?.getOrThrow()
        else -> incarnation.createNode(randomGenerator, environment, root)
    } ?: cantBuildWith<Node<T>>(root)

    private fun visitParameters(context: Context, root: Any?): Either<List<*>, Map<String, *>> = when (root) {
        null -> Either.Left(emptyList<Any>())
        is Iterable<*> -> Either.Left(root.map { visitParameter(context, it) })
        is Map<*, *> -> Either.Right(root.map { it.key.toString() to visitParameter(context, it.value) }.toMap())
        else -> Either.Left(listOf(visitParameter(context, root)))
    }

    private fun visitRandomGenerator(context: Context, root: Any): RandomGenerator = when (root) {
        is Map<*, *> -> visitBuilding<RandomGenerator>(context, root)
        else -> visitBuilding<Long>(context, root)?.map { makeDefaultRandomGenerator(it) }
    }?.onFailure { logger.error("Unable to build a random generator: {}", it.message) }?.getOrThrow()
        ?: cantBuildWith<RandomGenerator>(root)

    @Suppress("CyclomaticComplexMethod")
    fun <T, P : Position<P>> visitProgram(
        simulationRNG: RandomGenerator,
        incarnation: Incarnation<T, P>,
        environment: Environment<T, P>,
        node: Node<T>?,
        context: Context,
        program: Map<*, *>,
    ): Result<Pair<List<PositionBasedFilter<P>>, Actionable<T>>>? =
        if (ProgramSyntax.validateDescriptor(program) || GlobalProgramSyntax.validateDescriptor(program)) {
            /*
             * Time distribution
             */
            val timeDistribution: TimeDistribution<T> = visitTimeDistribution(
                incarnation,
                simulationRNG,
                environment,
                node,
                context,
                program[ProgramSyntax.timeDistribution],
            )
            context.factory.registerSingleton(TimeDistribution::class.java, timeDistribution)
            /*
             * Actionable
             */
            val actionable: Actionable<T> =
                visitActionable(simulationRNG, incarnation, environment, node, timeDistribution, context, program)
            context.factory.registerSingleton(Actionable::class.java, actionable)

            /*
             * Support function implementing the lookup strategy for conditions and actions
             */
            fun <R : Any> visitIncarnationBuildable(
                parameter: Any?,
                incarnationFactory: ReactionComponentFunction<T, P, R>,
                genericFactory: (Context, Any?) -> Result<R>?,
            ): Result<R>? {
                fun <R> create(parameter: Any?, makeWith: ReactionComponentFunction<T, P, R>): Result<R> = runCatching {
                    makeWith(simulationRNG, environment, node, timeDistribution, actionable, parameter)
                }
                return when (parameter) {
                    is Map<*, *> ->
                        /*
                         * First try the generic factory if there is a type specified, in case of failure fallback to
                         * the incarnation factory
                         */
                        genericFactory.takeIf { parameter.containsKey(JavaType.type) }
                            ?.invoke(context, parameter)
                            ?: create(parameter, incarnationFactory)
                    is Iterable<*> -> {
                        /*
                         * Try with the generic factory first (it is recursive)
                         */
                        val firstAttempt = genericFactory(context, parameter)
                        val exception = firstAttempt?.exceptionOrNull()
                        val recovery = exception?.let { create(parameter, incarnationFactory) }
                        recovery?.exceptionOrNull()?.addSuppressed(exception)
                        recovery ?: firstAttempt
                    }
                    else -> create(parameter, incarnationFactory)
                }
            }

            val filters = visitFilter<P>(context, program)
            val conditions = visitRecursively<Condition<T>>(
                context,
                program[ProgramSyntax.conditions] ?: emptyList<Any>(),
                JavaType,
            ) {
                visitIncarnationBuildable(it, incarnation::createCondition, ::visitBuilding)
            }
            if (conditions.isNotEmpty()) {
                actionable.conditions = actionable.conditions + conditions
            }
            val actions = visitRecursively<Action<T>>(
                context,
                program[ProgramSyntax.actions] ?: emptyList<Any>(),
                JavaType,
            ) {
                visitIncarnationBuildable(it, incarnation::createAction, ::visitBuilding)
            }
            if (actions.isNotEmpty()) {
                actionable.actions = actionable.actions + actions
            }
            context.factory.deregisterSingleton(actionable)
            context.factory.deregisterSingleton(timeDistribution)
            Result.success(Pair(filters, actionable))
        } else {
            null
        }

    private fun <P : Position<P>, T> visitActionable(
        simulationRNG: RandomGenerator,
        incarnation: Incarnation<T, P>,
        environment: Environment<T, P>,
        node: Node<T>?,
        timeDistribution: TimeDistribution<T>,
        context: Context,
        root: Map<*, *>,
    ) = when {
        root.containsKey(ProgramSyntax.program) ->
            incarnation.createReaction(simulationRNG, environment, node, timeDistribution, root[ProgramSyntax.program])
        node != null ->
            // This is a node-local reaction
            visitBuilding<Reaction<T>>(context, root)?.getOrThrow() ?: cantBuildWith<Reaction<T>>(root)
        else ->
            // A reaction with no node is a GlobalReaction
            visitBuilding<GlobalReaction<T>>(context, root)?.getOrThrow() ?: cantBuildWith<GlobalReaction<T>>(root)
    }

    fun visitSeeds(context: Context, root: Any?): Seeds = when (root) {
        null -> makeDefaultRandomGenerator(0) to makeDefaultRandomGenerator(0).also {
            logger.warn(
                "No seeds specified, defaulting to 0 for both {} and {}",
                DocumentRoot.Seeds.scenario,
                DocumentRoot.Seeds.simulation,
            )
        }

        is Map<*, *> -> {
            val stringKeys = root.keys.filterIsInstance<String>()
            require(stringKeys.size == root.keys.size) {
                "Illegal seeds sub-keys: ${root.keys - stringKeys.toSet()}. Valid keys are: $stringKeys"
            }
            val validKeys = DocumentRoot.Seeds.validKeys
            val nonPrivateKeys = stringKeys.filterNot { it.startsWith("_") }
            require(nonPrivateKeys.all { it in validKeys }) {
                "Illegal seeds sub-keys: ${nonPrivateKeys - validKeys.toSet()}. Valid keys are: $validKeys"
            }
            fun valueOf(element: String): Any = if (root.containsKey(element)) {
                root[element] ?: throw IllegalArgumentException(
                    "Invalid random generator descriptor $root has a null value associated to $element",
                )
            } else {
                0
            }
            visitRandomGenerator(context, valueOf(DocumentRoot.Seeds.scenario)) to visitRandomGenerator(
                context, valueOf(DocumentRoot.Seeds.simulation),
            )
        }

        else -> throw IllegalArgumentException(
            "Not a valid ${DocumentRoot.seeds} section: $root. Expected " +
                DocumentRoot.Seeds.validKeys.map { it to "<a number>" },
        )
    }

    private fun <P : Position<P>, T> visitTimeDistribution(
        incarnation: Incarnation<T, P>,
        simulationRNG: RandomGenerator,
        environment: Environment<T, P>,
        node: Node<T>?,
        context: Context,
        root: Any?,
    ) = when {
        root is Map<*, *> && root.containsKey(JavaType.type) ->
            visitBuilding<TimeDistribution<T>>(context, root)?.getOrThrow() ?: cantBuildWith<TimeDistribution<T>>(root)
        else ->
            incarnation.createTimeDistribution(simulationRNG, environment, node, root)
    }

    private fun visitVariables(context: Context, root: Any?): Map<String, Variable<*>> = visitNamedRecursively(
        context,
        root,
        syntax = DocumentRoot.Variable,
    ) { name, element ->
        (element as? Map<*, *>?)
            ?.takeIfNotAConstant(name, context)
            ?.takeIf { DocumentRoot.Variable.validateDescriptor(element) }?.let { _ ->
                fun Any?.toDouble(): Double = coerceToDouble(context)
                val variable = when (JavaType.type) {
                    in element ->
                        visitBuilding<Variable<*>>(context, element) // arbitrary type
                            ?.onFailure { logger.debug("Invalid variable: {} from {}: {}", name, element, it.message) }
                    else ->
                        runCatching { // Must be a linear variable, or else fail
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
        noinline visitSingle: (Any?) -> Result<T>?,
    ): List<T> = visitRecursively(
        evidence = T::class,
        context = context,
        root = root,
        syntax = syntax,
        visitSingle = visitSingle,
    )

    @Suppress("CyclomaticComplexMethod")
    private fun <T : Any> visitRecursively(
        evidence: KClass<T>,
        context: Context,
        root: Any?,
        syntax: SyntaxElement? = null,
        error: Throwable? = null,
        /**
         * Meaning:
         *  - Result.success: the target entity has been constructed correctly;
         *  - Result.failure: the target entity could not get built and an exception was generated;
         *  - null: the target entity could not get built, but there was no exception.
         */
        visitSingle: (Any?) -> Result<T>?,
    ): List<T> {
        /*
         * Generates an exception that also carries information about the previous failures encountered while
         * searching for a buildable definition.
         */
        fun Throwable.populate() = apply {
            if (error != null) {
                addSuppressed(error)
                error.suppressed.forEach { addSuppressed(it) }
            }
        }

        /*
         * Tries to build a definition from this point of the tree.
         */
        fun tryVisit(): Result<T>? = visitSingle(root)?.onSuccess {
            logger.debug("Built {}: {} using syntax {} from {}", it, evidence.simpleName, root, syntax)
        }

        /*
         * Forces a definition to be built successfully. If it is not, populates the exception with all previous
         * errors and throws it.
         */
        fun forceVisit(): List<T> = tryVisit()?.map { listOf(it) }
            ?.getOrElse { exception -> cantBuildWith(evidence, root, syntax, exception.populate()) } ?: cantBuildWith(
            evidence, root, syntax, error,
        )
        /*
         * Defines the behavior in case of Iterable (recursion), Map (build or recursion), or other type (build as-is).
         */
        return when (root) {
            is Iterable<*> -> root.flatMap { visitRecursively(evidence, context, it, syntax, error, visitSingle) }

            is Map<*, *> -> {
                logger.debug("Trying to build a {} from {} (syntax: {})", evidence.simpleName, root, syntax)
                fun recurse(previousResult: Result<T>?): List<T> = visitRecursively(
                    evidence,
                    context,
                    root.values,
                    syntax,
                    previousResult?.exceptionOrNull()?.populate() ?: error,
                    visitSingle,
                )
                when {
                    syntax == null -> tryVisit().let { result ->
                        result?.map { listOf(it) }?.getOrNull() ?: recurse(result)
                    }

                    syntax.validateDescriptor(root) -> forceVisit()
                    else -> recurse(null)
                }
            }

            else -> forceVisit()
        }
    }

    private inline fun <reified T : Any> visitNamedRecursively(
        context: Context,
        root: Any?,
        syntax: SyntaxElement,
        forceSuccess: Boolean = true,
        noinline visitSingle: (String, Any?) -> Result<T>?,
    ): Map<String, T> = visitNamedRecursively(T::class, context, root, syntax, forceSuccess, visitSingle)

    private fun <T : Any> visitNamedRecursively(
        evidence: KClass<T>,
        context: Context,
        root: Any?,
        syntax: SyntaxElement,
        forceSuccess: Boolean = true,
        visitSingle: (String, Any?) -> Result<T>?,
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
        visitSingle: (String, Any?) -> Result<T>?,
    ): Map<String, T> = root.flatMap { (key, value) ->
        logger.debug("Visiting: {} searching for {}", root, evidence.simpleName)
        fun recurse(): List<Pair<String, T>> =
            visitNamedRecursively(evidence, context, value, syntax, forceSuccess, visitSingle).toList()

        fun result() = visitSingle(key.toString(), value)?.map { listOf(key.toString() to it) }
        when (value) {
            is Map<*, *> -> when {
                syntax.validateDescriptor(value) -> result()?.getOrThrow()
                    .whenNull(and = forceSuccess) { cantBuildWith(evidence, value) }.orEmpty()

                else -> recurse()
            }

            is Iterable<*> -> result()?.getOrNull() ?: recurse()
            else -> result()?.getOrThrow().orEmpty()
        }
    }.toMap()

    internal data class PlaceHolderForVariables(val name: String)
}
