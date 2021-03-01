/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.konf

import arrow.core.Either
import it.unibo.alchemist.loader.Loader
import it.unibo.alchemist.loader.export.Extractor
import it.unibo.alchemist.loader.konf.types.JVMConstructor
import it.unibo.alchemist.loader.konf.types.NamedParametersConstructor
import it.unibo.alchemist.loader.konf.types.OrderedParametersConstructor
import it.unibo.alchemist.loader.variables.DependentVariable
import it.unibo.alchemist.loader.variables.JSR223Variable
import it.unibo.alchemist.loader.variables.Variable
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position
import org.danilopianini.jirf.Factory
import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.InputStream
import java.io.Reader
import java.lang.IllegalArgumentException
import java.net.URL
import kotlin.reflect.KClass
import kotlin.reflect.cast
import kotlin.reflect.full.isSubclassOf

object DefaultVisitor {

    private val linearVariableParameters = listOf("default", "min", "max", "step")

    fun visitYaml(yaml: String) = visitRoot(Yaml().load(yaml))
    fun visitYaml(yaml: Reader) = visitRoot(Yaml().load(yaml))
    fun visitYaml(yaml: InputStream) = visitRoot(Yaml().load(yaml))
    fun visitYaml(yaml: URL) = visitRoot(Yaml().load(yaml.openStream()))

    fun visitRoot(root: Map<String, Any>): Loader {
        val context = Context()
        var previousSize: Int? = null
        var injectedRoot = root
        while (context.constants.size != previousSize) {
            previousSize = context.constants.size
            val resolvedConstants = visitMultipleNamed(context, injectedRoot["variables"], false) { _, name, element ->
                visitConstant(context, root)
                    ?.also { context.pushLookupEntry(name, element as Map<*, *>, it.value) }
            }
            context.constants += resolvedConstants.mapValues { (_, v) -> v.value }
            injectedRoot = inject(context, injectedRoot)
            logger.debug("Constants {}", context.constants)
            logger.debug("Lookup {}", context.elementLookup)
            logger.debug("New map {}", injectedRoot)
        }
        val variables = visitMultipleNamed(context, injectedRoot["variables"]) { _, name, element ->
            visitAnyAndBuild<Variable<*>>(context, element)
                ?.also { context.pushLookupEntry(name, element as Map<*, *>, it) }
        }
        logger.debug("Variables: {}", variables)
        val dependentVariables = visitMultipleNamed(context, injectedRoot["variables"]) { _, name, element ->
            visitDependentVariable(context, element)
                ?.also { context.pushLookupEntry(name, element as Map<*, *>, it) }
        }
        logger.debug("Dependent variables: {}", dependentVariables)
        val remoteDependencies = visitMultipleOrdered(context, injectedRoot["dependencies"]) { _, element ->
            visitAnyAndBuild<String>(context, element)
        }
        return object : Loader {
            override fun getDependentVariables(): Map<String, DependentVariable<*>> = dependentVariables

            override fun getVariables(): Map<String, Variable<*>> = variables

            override fun <T : Any?, P : Position<P>> getWith(values: Map<String, *>): Environment<T, P> {
                val localContext = Context(namedLookup = context.namedLookup)
                var localRoot = injectedRoot
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
                @Suppress("UNCHECKED_CAST") val environment = localRoot["environment"]
                    ?.let { visitAnyAndBuild<Environment<T, P>>(localContext, it) }
                    ?: Continuous2DEnvironment<T>() as Environment<T, P>
                println(environment)
                return environment
            }

            override fun getConstants(): Map<String, Any?> = context.constants.toMap()

            override fun getDataExtractors(): MutableList<Extractor> {
                TODO("Not yet implemented")
            }

            override fun getRemoteDependencies(): List<String> = remoteDependencies
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun inject(context: Context, root: Map<String, Any>): Map<String, Any> =
        replaceKnownValuesRecursively(context, root) as Map<String, Any>

    private fun replaceKnownValuesRecursively(context: Context, root: Any?): Any? =
        when (root) {
            is Map<*, *> -> context.lookup<Any>(root)?.getOrNull()
                ?: root.entries.map {
                    replaceKnownValuesRecursively(context, it.key) to replaceKnownValuesRecursively(context, it.value)
                }.toMap()
            is Iterable<*> -> root.map { replaceKnownValuesRecursively(context, it) }
            else -> root
        }

    private fun visitAny(context: Context, root: Any?): Any? =
        when(root) {
            is Iterable<*> -> root.map { visitAny(context, it) }
            is Map<*, *> -> context.lookup<Any>(root)?.getOrNull()
                ?: visitJVMConstructor(context, root)
                ?: root
            else -> root
        }

    private inline fun <reified T : Any> visitAnyAndBuild(context: Context, root: Any): T? =
        when(root) {
            is T -> root
            is Map<*, *> -> context.lookup<T>(root)?.getOrNull()
                ?: visitJVMConstructor(context, root)?.buildAny<T>(context.factory)?.getOrNull()
            else -> context.factory.convert(T::class.java, root).orElse(null)
                .also { logger.debug("Unable to convert {} into a {}, discarding.", root, T::class.simpleName) }
        }

    private fun visitParameters(context: Context, root: Any?): Either<List<*>, Map<String, *>> = when (root) {
            null -> Either.left(emptyList<Any>())
            is Iterable<*> -> Either.left(root.map { visitAny(context, it) })
            is Map<*, *> -> Either.right(
                root.map { visitString(context, it.key) to visitAny(context, it.value) }.toMap()
            )
            else -> Either.left(listOf(visitAny(context, root)))
        }

    private fun visitJVMConstructor(context: Context, root: Map<*, *>): JVMConstructor? =
        if (root.containsKey("type")) {
            val type: String = visitString(context, root["type"])
            when (val parameters = visitParameters(context, root["parameters"])) {
                is Either.Left -> OrderedParametersConstructor(type, parameters.a)
                is Either.Right -> NamedParametersConstructor(type, parameters.b)
            }
        } else {
            null
        }

    private fun visitString(context: Context, root: Any?): String =
        when (root) {
            null -> throw IllegalStateException("null value provided where String was required")
            is CharSequence -> root.toString()
            is Map<*, *> -> context.lookup<CharSequence>(root)?.getOrNull()?.toString()
            else -> null
        } ?: throw IllegalStateException("Unable to obtain a String from $root")

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

    private fun <T : Any> visitMultipleOrdered(context: Context, root: Any?, visitSingle: (Context, Any) -> T?): List<T> =
        root?.let {
            visitSingle(context, root)?.let { single -> listOf(single) }
                ?: when(root) {
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
        context: Context, root: Any?,
        failOnError: Boolean = false,
        visitSingle: (Context, String, Any) -> T?
    ): Map<String, T> =
        when(root) {
            is Map<*, *> -> visitMultipleNamedFromMap(context, root, failOnError, visitSingle)
            is Iterable<*> -> root.flatMap { visitMultipleNamed(context, it, failOnError, visitSingle).toList() }.toMap()
            else -> emptyMap<String, T>().takeUnless { failOnError }
                ?: throw IllegalArgumentException("Unable to build a named object from $root")
        }

    private fun <T : Any> visitMultipleNamedFromMap(
        context: Context, root: Map<*, *>,
        failOnError: Boolean = false,
        visitSingle: (Context, String, Any) -> T?
    ): Map<String, T> =
        root.flatMap { (key, value) ->
            requireNotNull(value) {
                "Illegal null element found in $root. Current context: $context."
            }
            visitSingle(context, key.toString(), value)?.let { listOf(key.toString() to it) }
                ?: visitMultipleNamed(context, value, failOnError, visitSingle).toList()
        }.toMap()

    val logger = LoggerFactory.getLogger(DefaultVisitor::class.java)

    private data class PlaceHolder (val name: String)

    private data class Context(
        val constants: MutableMap<String, Any?> = mutableMapOf(),
        val elementLookup: MutableMap<Map<*, *>, Any?> = mutableMapOf(),
        val namedLookup: MutableMap<String, Map<*, *>> = mutableMapOf(),
        val factory: Factory = ObjectFactory.makeBaseFactory(),
    ) {

//        fun pushPlaceHolder(key: String, value: Any) {
//            if (value is Map<*, *>) {
//                if(placeHolderLookup.containsKey(placeHolder))
//            } else {
//                throw IllegalStateException("$value can't be associated with $key")
//            }
//        }

        fun pushLookupEntry(name: String, element: Map<*, *>, value: Any?) {
            if (namedLookup.containsKey(name)) {
                val previous = elementLookup[element]
                if (value != previous && value != null && previous != null && value::class == previous::class) {
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
                                "A request for type ${destinationType.qualifiedName} has been fullfilled by the context based on " +
                                    "$element, but the result does not match the expected type"
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

class Constant<V>(val value: V): DependentVariable<V> {
    override fun getWith(variables: Map<String, Any>): V = value
}

fun main() {
    println(
        DefaultVisitor
            .visitYaml(File("/home/danysk/LocalProjects/Alchemist/alchemist-loading/src/test/resources/guidedTour/linearVariableRequiringConstant.yml").readText())
            .getDefault<Any, Nothing>()
    )
}
//class ContextImpl() : Context