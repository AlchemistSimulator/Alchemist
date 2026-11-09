/*
 * Copyright (c) 2023-2026, Danilo Pianini, Nicolas Farabegoli, Elisa Tronetti,
 * and all authors listed in the `build.gradle.kts` and the generated `pom.xml` file.
 *
 * This file is part of Collektive, and is distributed under the terms of the Apache License 2.0,
 * as described in the LICENSE file in this project's repository's top directory.
 */

package it.unibo.alchemist

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import it.unibo.alchemist.actions.RunCollektiveProgram
import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asPropertyOrNull
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.conditions.AbstractCondition
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.reactions.GenericReaction
import it.unibo.alchemist.model.timedistributions.DiracComb
import it.unibo.alchemist.model.times.DoubleTime
import it.unibo.alchemist.util.RandomGenerators.nextDouble
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.compiler.CollektiveK2JVMCompiler
import it.unibo.collektive.compiler.logging.CollectingMessageCollector
import java.io.File
import java.lang.reflect.Method
import java.net.URLClassLoader
import java.security.MessageDigest
import java.util.Locale
import javax.script.ScriptEngineManager
import kotlin.io.path.absolutePathString
import kotlin.io.path.createTempDirectory
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.reflect.KProperty
import kotlin.reflect.full.starProjectedType
import org.apache.commons.math3.random.RandomGenerator
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.ERROR
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.EXCEPTION
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.FIXED_WARNING
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.INFO
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.LOGGING
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.OUTPUT
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.STRONG_WARNING
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.WARNING
import org.slf4j.LoggerFactory

/**
 * Collektive incarnation in Alchemist.
 */
class CollektiveIncarnation<P> : Incarnation<Any?, P> where P : Position<P> {

    override fun createMolecule(s: String) = SimpleMolecule(s)

    override fun createConcentration(descriptor: Any?): Any? = descriptor

    override fun createAction(
        randomGenerator: RandomGenerator,
        environment: Environment<Any?, P>,
        node: Node<Any?>?,
        reaction: Reaction<Any?>,
        additionalParameters: Any?,
    ): Action<Any?> {
        requireNotNull(node) { GLOBAL_REACTION_ERROR }
        require(reaction is NodeReaction<Any?>) { GLOBAL_REACTION_ERROR }
        if (additionalParameters is CharSequence) {
            return RunCollektiveProgram(reaction, additionalParameters.toString())
        }
        val parameters = additionalParameters as? Map<*, *>
        requireNotNull(parameters) {
            val type = additionalParameters?.let { it::class.simpleName } ?: "null"
            "Invalid parameters for Collektive. Map required, but $type has been provided: $additionalParameters"
        }
        val entrypoint: String = requireNotNull(parameters["entrypoint"]) {
            "No entrypoint provided in $additionalParameters"
        }.toString()
        val code: String = parameters["code"]?.toString().orEmpty()
        val sourceSets: List<File> = parameters["source-sets"].toFiles()
        val classpath = sourceSets.joinToString(separator = File.pathSeparator) { it.absolutePath }
        val internalIdentifier = "$classpath$code$entrypoint".md5()
        val (name: String, methodName: String) =
            when (val nameFromParameters = parameters["name"]) {
                null -> "collektive$internalIdentifier".let { it to it }
                else ->
                    nameFromParameters.toString().let { originalName ->
                        originalName to "${originalName.replaceFirstChar { it.lowercase() }}$internalIdentifier"
                    }
            }
        check(name.matches(validName)) {
            "Invalid name for Collektive program: $name"
        }
        val className = methodName.replaceFirstChar { it.uppercase() }
        val packageMatch = findPackage.find(code)
        val packageName = packageMatch?.groupValues?.get(1).orEmpty()
        val classFqName = classFqNameFrom(packageName, className)
        val classLoader = classLoaders.get(methodName)

        fun loadMethod() = classLoader.loadClass(classFqName).methods.first { it.name == methodName }
        val methodToCall: Method = runCatching { loadMethod() }
            .recover { exception ->
                logger.info("Collektive program $name not found, compiling", exception)
                compileCollektive(name, sourceSets, className, methodName, code, entrypoint, classLoader)
                loadMethod()
            }
            .getOrThrow()
        return RunCollektiveProgram(reaction, methodToCall, name)
    }

    override fun createCondition(
        randomGenerator: RandomGenerator,
        environment: Environment<Any?, P>,
        node: Node<Any?>?,
        reaction: Reaction<Any?>,
        additionalParameters: Any?,
    ): Condition<Any?> {
        requireNotNull(node)
        return object : AbstractCondition<Any?>(reaction) {
            override fun cloneCondition(newReaction: Reaction<Any?>): Condition<Any?> = createCondition(
                randomGenerator,
                environment,
                newReaction.host as? Node<Any?>,
                newReaction,
                additionalParameters,
            )
        }
    }

    override fun createReaction(
        randomGenerator: RandomGenerator,
        environment: Environment<Any?, P>,
        parameter: Any?,
    ): Reaction<Any?> = throw UnsupportedOperationException(GLOBAL_REACTION_ERROR)

    override fun createNodeReaction(
        randomGenerator: RandomGenerator,
        environment: Environment<Any?, P>,
        node: Node<Any?>,
        timeDistribution: TimeDistribution<Any?>,
        parameter: Any?,
    ): NodeReaction<Any?> = GenericReaction(node, timeDistribution).also {
        it.actions = listOf(
            createAction(randomGenerator, environment, node, it, parameter),
        )
    }

    override fun createTimeDistribution(
        randomGenerator: RandomGenerator,
        environment: Environment<Any?, P>,
        node: Node<Any?>?,
        parameter: Any?,
    ): TimeDistribution<Any?> {
        val frequency =
            when (parameter) {
                null -> 1.0
                is Number -> parameter.toDouble()
                is String -> parameter.toDouble()
                else -> error("Invalid time distribution parameter: $parameter")
            }
        return DiracComb(DoubleTime(randomGenerator.nextDouble(0.0, 1.0 / frequency)), frequency)
    }

    override fun createNode(
        randomGenerator: RandomGenerator,
        environment: Environment<Any?, P>,
        parameter: Any?,
    ): Node<Any?> = GenericNode(environment).also { genericNode ->
        genericNode.addProperty(
            CollektiveDevice(
                randomGenerator,
                environment,
                genericNode,
                when (parameter) {
                    null -> null
                    is Number -> DoubleTime(parameter.toDouble())
                    is String -> DoubleTime(parameter.toDouble())
                    else -> error("Invalid message retention time: $parameter")
                },
            ),
        )
    }

    override fun readConcentration(node: Node<Any?>, molecule: Molecule, property: String): Double {
        val concentration = node[molecule].getOrNull()
        val interpreted = when {
            property.isBlank() -> concentration
            else -> {
                val toInvoke = propertyCache.get(CodeElements(concentration, property))
                node.asPropertyOrNull<Any?, CollektiveDevice<*>>().toInvoke(concentration)
            }
        }
        return when (interpreted) {
            is Double -> interpreted
            is Number -> interpreted.toDouble()
            is String -> interpreted.toDoubleOrNull() ?: Double.NaN
            is Boolean -> if (interpreted) 1.0 else 0.0
            else -> Double.NaN
        }
    }

    private companion object {

        private const val GLOBAL_REACTION_ERROR =
            "Collektive requires a device and cannot execute in a Global Reaction"

        private data class CodeElements(val concentrationType: String, val property: String) {
            constructor(concentration: Any?, property: String) : this(
                when (concentration) {
                    null -> "Any?"
                    is List<*> -> "List<*>" // TODO: select the minimal common supertype
                    // TODO: add support for maps, pairs, sets
                    else -> {
                        val type = concentration::class.starProjectedType
                        "$type${"?".takeIf { type.isMarkedNullable }.orEmpty()}"
                    }
                },
                property,
            )
        }

        private object ScriptEngine {
            operator fun getValue(thisRef: Any?, property: KProperty<*>) =
                ScriptEngineManager().getEngineByName(property.name)
                    ?: error("No script engine with ${property.name} found.")
        }

        private fun String.md5(): String = MessageDigest.getInstance("MD5").digest(toByteArray()).joinToString("") {
            "%02x".format(Locale.US, it)
        }

        private val findPackage = Regex("""package\s+((\w+\.)*\w+)(\s|;|/|$)""", RegexOption.MULTILINE)
        private val validName = Regex("^[a-zA-Z_][a-zA-Z0-9_]*$", RegexOption.MULTILINE)
        private val logger = LoggerFactory.getLogger(CollektiveIncarnation::class.java)
        private val kotlin by ScriptEngine
        private val defaultLambda: CollektiveDevice<*>?.(Any?) -> Any? = { Double.NaN }

        private val propertyCache: LoadingCache<CodeElements, CollektiveDevice<*>?.(Any?) -> Any?> =
            Caffeine
                .newBuilder()
                .maximumSize(1000)
                .build { (concentrationType, property) ->
                    val header = """
                        import kotlin.math.*
                        import ${CollektiveDevice::class.qualifiedName}
                        val `a beautiful lambda function`: CollektiveDevice<*>?.($concentrationType) -> Any? = {
                    """.trimIndent()
                    val footer = """
                        }
                        `a beautiful lambda function`
                    """.trimIndent()
                    val indentedProperty = property.lines().joinToString("\n") { "    $it" }
                    runCatching {
                        kotlin.eval("$header\n$indentedProperty\n$footer")
                    }.recover { _ ->
                        val doubleIndentedProperty = indentedProperty.lines().joinToString("\n") { "    $it" }
                        kotlin.eval("$header\n    this?.run {\n$doubleIndentedProperty\n    }$footer")
                    }.map { interpreted ->
                        @Suppress("UNCHECKED_CAST")
                        when (interpreted) {
                            is Nothing.(Nothing) -> Any? -> interpreted
                            else -> defaultLambda
                        } as CollektiveDevice<*>?.(Any?) -> Any?
                    }.getOrElse { defaultLambda }
                }

        private val classLoaders: LoadingCache<String, URLClassLoader> =
            Caffeine.newBuilder().maximumSize(1000).build { name ->
                val outputFolder = createTempDirectory(name).toFile()
                URLClassLoader(arrayOf(outputFolder.toURI().toURL()), Thread.currentThread().contextClassLoader)
            }

        private fun classFqNameFrom(packageName: String?, className: String) =
            "${packageName?.takeUnless { it.isEmpty() }?.let { "$it." }.orEmpty()}$className"

        private fun compileCollektive(
            name: String,
            sourceSets: List<File>,
            className: String,
            methodName: String,
            code: String,
            entrypoint: String,
            classLoader: URLClassLoader,
        ): Pair<CollectingMessageCollector, ExitCode> {
            val inputFolder = createTempDirectory("collektive").toFile()
            logger.info("Compiling Collektive program {} in folder {}", name, inputFolder.absolutePath)
            val finalCode =
                """
                |@file:JvmName("$className")
                |${code.replace("\n", "\n|")}
                |fun <P : ${Position::class.qualifiedName}<P>> ${Aggregate::class.qualifiedName}<Int>.$methodName(device: ${CollektiveDevice::class.qualifiedName}<P>) =
                |   $entrypoint
                """.trimMargin()
            logger.info("Final code for Collektive program {}:\n{}", name, finalCode)
            inputFolder.resolve("$className.kt").writeText(finalCode)
            val outputFolder = File(classLoader.urLs.first().file).toPath()
            check(outputFolder.exists() && outputFolder.isDirectory()) {
                "Output folder ${outputFolder.absolutePathString()} does not exist or is not a directory"
            }
            val messages = CollectingMessageCollector()
            val result: ExitCode =
                CollektiveK2JVMCompiler.compile(
                    sourceSets + inputFolder,
                    destinationFolder = outputFolder,
                    messageCollector = messages,
                )
            messages.messages.forEach { (severity, message) ->
                when (severity) {
                    ERROR, EXCEPTION -> logger.error(message)
                    STRONG_WARNING, WARNING, FIXED_WARNING -> logger.error(message)
                    INFO, OUTPUT -> logger.info(message)
                    LOGGING -> logger.debug(message)
                }
            }
            check(!messages.hasErrors() && result == ExitCode.OK) {
                "Compilation of Collektive program $name failed:\n$finalCode\n\n" +
                    messages[ERROR].joinToString(separator = "\n")
            }
            return messages to result
        }

        /**
         * Convert the source set to a list of files.
         */
        private fun Any?.toFiles(): List<File> = when (this) {
            null -> emptyList()
            is File -> listOf(this)
            is CharSequence -> {
                val file = File(toString())
                check(file.exists()) {
                    "Collektive source root ${file.absolutePath} does not exist"
                }
                listOf(file)
            }
            is Iterable<*> -> flatMap { files -> files.toFiles() }
            else -> error("Invalid source set of type ${this::class.simpleName ?: "anonymous"}: $this")
        }
    }
}
