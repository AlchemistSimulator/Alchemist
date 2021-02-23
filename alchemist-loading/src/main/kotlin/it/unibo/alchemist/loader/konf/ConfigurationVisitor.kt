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
import com.google.errorprone.annotations.Var
import it.unibo.alchemist.loader.Loader
import it.unibo.alchemist.loader.export.Extractor
import it.unibo.alchemist.loader.konf.types.JVMConstructor
import it.unibo.alchemist.loader.konf.types.NamedParametersConstructor
import it.unibo.alchemist.loader.konf.types.OrderedParametersConstructor
import it.unibo.alchemist.loader.variables.DependentVariable
import it.unibo.alchemist.loader.variables.JSR223Variable
import it.unibo.alchemist.loader.variables.LinearVariable
import it.unibo.alchemist.loader.variables.Variable
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position
import org.danilopianini.jirf.Factory
import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.InputStream
import java.io.Reader
import java.net.URL
import kotlin.reflect.KClass
import kotlin.reflect.cast
import kotlin.reflect.full.isSubclassOf

data class Context(
    private val lookup: MutableMap<Map<*, *>, Any?> = mutableMapOf(),
    val constants: MutableMap<String, Any?> = mutableMapOf(),
    val factory: Factory = ObjectFactory.makeBaseFactory(),
) {

//    private val lookup: MutableMap<Map<*, *>, Any?> = mutableMapOf()
//    val constants: MutableMap<String, Any?> = mutableMapOf()
//    val factory = ObjectFactory.makeBaseFactory()

    fun pushReverseMapping(key: Map<*, *>, value: Any?) {
        lookup[key] = value
    }

    inline fun <reified T : Any> lookup(element: Map<*, *>): Result<T?>? = lookup(T::class, element)

    fun <T : Any> lookup(destinationType: KClass<T>, element: Map<*, *>): Result<T?>? =
        if (lookup.containsKey(element)) {
            val result = lookup[element]
            if (result == null) {
                Result.success(null)
            } else {
                require(result::class.isSubclassOf(destinationType)) {
                    "A request for type $destinationType has been fullfilled by the context based on $element, " +
                        "but the result does not match the expected type"
                }
                Result.success(destinationType.cast(result))
            }
        } else {
            null
        }

}

object DefaultVisitor {

    fun visitYaml(yaml: String) = visitRoot(Yaml().load(yaml))
    fun visitYaml(yaml: Reader) = visitRoot(Yaml().load(yaml))
    fun visitYaml(yaml: InputStream) = visitRoot(Yaml().load(yaml))
    fun visitYaml(yaml: URL) = visitRoot(Yaml().load(yaml.openStream()))

    fun visitRoot(root: Map<String, Any>): Loader {
        val variables = root["variables"] ?: emptyMap<Any, Any>()
        val context = Context()
        var previousSize: Int? = null
        if (variables is Map<*, *>) {
            while (context.constants.size != previousSize) {
                previousSize = context.constants.size
                context.constants += visitMultipleNamed(context, variables, ::visitConstant).mapValues { (_, v) -> v.value }
            }
        }
        return object : Loader {
            override fun getDependentVariables(): MutableMap<String, DependentVariable<*>> {
                TODO("Not yet implemented")
            }

            override fun getVariables(): MutableMap<String, Variable<*>> {
                TODO("Not yet implemented")
            }

            override fun <T : Any?, P : Position<P>?> getWith(values: Map<String, *>?): Environment<T, P> {
                TODO("Not yet implemented")
            }

            override fun getConstants(): Map<String, Any?> = context.constants.toMap()

            override fun getDataExtractors(): MutableList<Extractor> {
                TODO("Not yet implemented")
            }

            override fun getRemoteDependencies(): MutableList<String> {
                TODO("Not yet implemented")
            }
        }
    }

    fun visitAny(context: Context, root: Any?): Any? =
        when(root) {
            is Iterable<*> -> root.map { visitAny(context, it) }
            is Map<*, *> -> context.lookup(Any::class, root)?.getOrNull()
                ?: visitJVMConstructor(context, root)
                ?: root
            else -> root
        }

    inline fun <reified T : Any> visitAnyAndBuild(context: Context, root: Any): T? =
        when(root) {
            is T -> root
            is Map<*, *> -> context.lookup<T>(root)?.getOrNull()
                ?: visitJVMConstructor(context, root)?.buildAny(context.factory)
            else -> context.factory.convertOrFail(T::class.java, root)
        }

    fun visitParameters(context: Context, root: Any?): Either<List<*>, Map<String, *>> = when (root) {
            null -> Either.left(emptyList<Any>())
            is Iterable<*> -> Either.left(root.map { visitAny(context, it) })
            is Map<*, *> -> Either.right(
                root.map { visitString(context, it.key) to visitAny(context, it.value) }.toMap()
            )
            else -> Either.left(listOf(visitAny(context, root)))
        }

//    fun visitJVMConstructor(context: Context, root: Any): JVMConstructor? =
//        if (root is Map<*, *> && root.containsKey("type")) visitJVMConstructor(context, root) else null

//    inline fun <reified T : Any> visitJVMConstructor(context: Context, root: Map<*, *>): T =
//        visitJVMConstructor(T::class, context, root)

    fun visitJVMConstructor(context: Context, root: Map<*, *>): JVMConstructor? =
        if (root.containsKey("type")) {
            val type: String = visitString(context, root["type"])
            when (val parameters = visitParameters(context, root["parameters"])) {
                is Either.Left -> OrderedParametersConstructor(type, parameters.a)
                is Either.Right -> NamedParametersConstructor(type, parameters.b)
            }
        } else {
            null
        }
//        return constructor.newInstance(expected, context.factory)

    fun visitString(context: Context, root: Any?): String =
        when (root) {
            null -> throw IllegalStateException("null value provided where String was required")
            is CharSequence -> root.toString()
            is Map<*, *> -> context.lookup(CharSequence::class, root)?.getOrNull()?.toString()
            else -> null
        } ?: throw IllegalStateException("Unable to obtain a String from $root")

    fun visitConstant(context: Context, root: Any): Constant<*>? =
        if (root is Map<*, *>) {
            if (root.containsKey("formula")) {
                val formula = root["formula"]
                if (formula is String) {
                    val language = root["language"]?.toString()?.toLowerCase() ?: "groovy"
                    val interpreter = JSR223Variable<Any>(language, formula)
                    runCatching { interpreter.getWith(context.constants) }
                        .getOrElse {
                            logger.info("Unable to resolve constant from {} with context {}: {}", root, context, it.message)
                        }
                        .let { Constant(it) }
                } else {
                    Constant(formula)
                }.also { context.pushReverseMapping(root, it.value) }
            } else {
                visitJVMConstructor(context, root)
                    ?.buildAny<DependentVariable<Any>>(context.factory)
                    ?.getWith(context.constants)
                    ?.let { Constant(it) }
            }
        } else {
            null
        }

    val linearVariableParameters = listOf("default", "min", "max", "step")

//    fun visitVariable(context: Context, root: Any): Variable<*>? =
//        if (root is Map<*, *>) {
//            if (linearVariableParameters.any { root.keys.contains(it) }) {
//                val typeName = LinearVariable::class.qualifiedName!!
//                require(linearVariableParameters.size == root.keys.size
//                    && root.keys.containsAll(linearVariableParameters)) {
//                    "Inconsistent ${typeName} definition: requires parameters $linearVariableParameters, " +
//                        "but ${root} were provided."
//                }
//                val parameters = linearVariableParameters.map { visitAny(context, root[it]) }
//                OrderedParametersConstructor(typeName, linearVariableParameters)
//                    .buildAny<LinearVariable>(context.factory)
//            } else {
//                visitJVMConstructor(context, root)
//                    ?.buildAny<DependentVariable<Any>>(context.factory)
//                    ?.getWith(context.constants)
//                    ?.let { Constant(it) }
//            }
//        } else {
//            null
//        }.let{TODO()}

    fun <T : Any> visitMultipleOrdered(context: Context, root: Any, visitSingle: (Context, Any) -> T?): List<T?> =
        visitSingle(context, root)?.let { listOf(it) }
            ?: when(root) {
                is Iterable<*> -> root.flatMap { element ->
                    requireNotNull(element) {
                        "Illegal null element in $root"
                    }
                    visitMultipleOrdered(context, element, visitSingle)
                }
                is Map<*, *> -> visitMultipleOrdered(context, root.values, visitSingle)
                else -> visitSingle(context, root)?.let { listOf(it) } ?: emptyList()
            }

    fun <T : Any> visitMultipleNamed(context: Context, root: Iterable<*>, visitSingle: (Context, Any) -> T?): Map<String, T> =
        root.flatMap {
            when(it) {
                is Map<*, *> -> visitMultipleNamed(context, it, visitSingle)
                is Iterable<*> -> visitMultipleNamed(context, it, visitSingle)
                else -> throw IllegalStateException("Unnamed entity $root Probably an array has been used where an object was expected.")
            }.toList()
        }.toMap()

    fun <T : Any> visitMultipleNamed(context: Context, root: Map<*, *>, visitSingle: (Context, Any) -> T?): Map<String, T> =
        root.flatMap { (key, value) ->
            requireNotNull(value) {
                "Illegal null element found in $root. Current context: $context."
            }
            visitSingle(context, value)?.let { listOf(key.toString() to it) }
                ?: when (value) {
                    is Map<*, *> -> visitMultipleNamed(context, value, visitSingle)
                    is Iterable<*> -> visitMultipleNamed(context, value, visitSingle)
                    else -> emptyMap()
                }.toList()
        }.toMap()

    val logger = LoggerFactory.getLogger(DefaultVisitor::class.java)
}

class Constant<V>(val value: V): DependentVariable<V> {
    override fun getWith(variables: Map<String, Any>): V = value
}

fun main() {
    println(
        DefaultVisitor
            .visitYaml(File("/home/danysk/LocalProjects/Alchemist/alchemist-loading/src/test/resources/guidedTour/linearVariableRequiringConstant.yml").readText())
            .constants
    )
}
//class ContextImpl() : Context