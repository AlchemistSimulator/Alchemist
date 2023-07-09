/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import it.unibo.alchemist.launch.Launcher
import it.unibo.alchemist.launch.Validation
import it.unibo.alchemist.model.api.SupportedIncarnations
import it.unibo.alchemist.util.ClassPathScanner
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.cli.default
import org.apache.commons.cli.CommandLine
import org.slf4j.LoggerFactory
import org.slf4j.helpers.NOPLoggerFactory
import java.nio.file.Files
import java.nio.file.Paths

private typealias ValidLauncher = Pair<Validation.OK, Launcher>

/**
 * Starts Alchemist.
 */
object Alchemist {
    private val logger = LoggerFactory.getLogger(Alchemist::class.java)
    private val launchers: List<Launcher> = loadLaunchers()

    private val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()

    private const val defaultLauncherName = "HeadlessSimulationLauncher"
    private const val javaFxLauncherName = "SingleRunFXUI"

    private fun loadLaunchers(): List<Launcher> {
        return ClassPathScanner
            .subTypesOf(Launcher::class.java, "it.unibo.alchemist")
            .map { clazz ->
                val zeroAryConstructors = clazz.constructors
                    .filter { it.parameterCount == 0 && it.canAccess(null) }
                if (zeroAryConstructors.size == 1) {
                    zeroAryConstructors.first().newInstance() as Launcher
                } else {
                    val instances = clazz.fields.filter {
                        it.name == "INSTANCE" &&
                            it.canAccess(null) &&
                            Launcher::class.java.isAssignableFrom(it.type)
                    }
                    if (instances.size == 1) {
                        instances.first().get(null) as Launcher
                    } else {
                        error("Cannot instance or access an instance of $clazz")
                    }
                }
            }
    }

    /**
     * Set this to false for testing purposes.
     */
    private var isNormalExecution = true

    private inline fun <reified T : Number> CommandLine.hasNumeric(name: Char, converter: String.() -> T?): T? =
        getOptionValue(name)?.let {
            when (val value = converter(it)) {
                null ->
                    exitBecause("Not a valid ${T::class.simpleName}: $it", ExitStatus.NUMBER_FORMAT_ERROR)

                else -> value
            }
        }

    /**
     * @param args
     * the argument for the program
     */
    @OptIn(ExperimentalCli::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val parser = ArgParser("alchemist")

        class Run : Subcommand("run", "Run a simulation or a batch of simulations") {

            val simulationFile by parser.argument(
                type = ArgType.String,
                fullName = "simulation file",
                description = """
                File containing simulation configuration to be executed.
                """.trimIndent(),
            )

            val launcher by parser.option(
                type = ArgType.String,
                fullName = "launcher",
                description = """
                Simulation launcher class to be used. Use fully-qualified name e.g. it.unibo.alchemist.launch.HeadlessSimulationLauncher.
                """.trimIndent(),
            ).default("it.unibo.alchemist.launch.HeadlessSimulationLauncher")

            val options by parser.option(
                type = ArgType.String,
                fullName = "options",
                description = """
                Path to a valid yaml file containing additional launch options.
                Currently supported options are:
                
                - variables : comma separated list of strings : selected batch variables
                - isBatch : boolean : whether batch mode is selected
                - distributedConfigPath : string : the path to the file with the load distribution configuration, or null if the run is local
                - graphicsPath : string : the path to the effects file, or null if unspecified
                - serverConfigPath : string : if launched as Alchemist grid node server, the path to the configuration file. Null otherwise.
                - parallelism : integer : parallel threads used for running locally. Defaults to available processores at runtime
                - endTime : decimal : final simulation time. Defaults to positive infinity.
                - isWeb : boolean : true if the web renderer is used. Defaults to false.
                - engineConfig : object containing configurations related to engine execution mode.
                    - engineMode : one option of [deterministic, batchFixed, batchEpsilon] : engine event processing mode, defaults to deterministic. Batch modes utilize parallel processing and may not yield predicatble results, use at your own risk.
                    - outputReplayStrategy : one option of [aggregate, replay] : engine events batch output strategy, defaults to replay.
                    - batchSize : integer : events batch size, only used with batchFixed mode
                    - epsilon : decimal : events epsilon value, only used with batchEpsilon mode
                - verbosity : one option of [debug, info, all, error, off, warn] : determines log verbosity level. v = info vv = debug, vvv = all, q = error, qq = off, w = warn. Defaults to warn.
                """.trimIndent(),
            )

            val overrides by parser.option(
                type = ArgType.String,
                fullName = "overrides",
                description = """
                TODO
                """.trimIndent(),
            )

            override fun execute() {
                executeSimlation(simulationFile, launcher, options, overrides)
            }
        }
        parser.subcommands(Run())
        parser.parse(args)
    }

    private fun executeSimlation(
        simulationFile: String,
        launcher: String?,
        optionsFile: String?,
        overridesFile: String?,
    ) {
        println(overridesFile) // TODO do something later
        validateOutputModule()
        validateIncarnations()
        val launcherName = launcher ?: defaultLauncherName
        val optionsConfig = parseOptions(optionsFile)
        val legacyConfig = optionsConfig.toLegacy(simulationFile, launcherName)
        setVerbosity(optionsConfig.verbosity)
        val selectedLauncher = selectLauncher(legacyConfig, launcherName)
        selectedLauncher.launch(legacyConfig)
    }

    private fun validateOutputModule() {
        if (LoggerFactory.getILoggerFactory().javaClass == NOPLoggerFactory::class.java) {
            println("Alchemist could not load the output module (broken SLF4J depedencies?)") // NOPMD
            exitWith(ExitStatus.NO_LOGGER)
        }
    }

    private fun parseOptions(pathString: String?): OptionsConfig {
        return if (pathString != null) {
            val path = Paths.get(pathString)
            Files.newBufferedReader(path).use {
                mapper.readValue(it, OptionsConfig::class.java)
            }
        } else {
            OptionsConfig()
        }
    }

    private fun OptionsConfig.toLegacy(simulationFile: String, launcherName: String): AlchemistExecutionOptions {
        return AlchemistExecutionOptions(
            configuration = simulationFile,
            headless = launcherName == defaultLauncherName,
            variables = this.variables,
            batch = this.isBatch,
            distributed = this.distributedConfigPath,
            graphics = this.graphicsPath,
            fxui = launcherName == javaFxLauncherName,
            web = this.isWeb,
            help = false,
            server = this.serverConfigPath,
            parallelism = this.parallelism,
            endTime = this.endTime,
        )
    }

    private fun selectLauncher(options: AlchemistExecutionOptions, launcherClass: String): Launcher {
        val (validLaunchers, invalidLaunchers) = options.classifyLaunchers()
        val sortedLaunchers: List<ValidLauncher> = validLaunchers
            .map { (validation, launcher) ->
                validation as Validation.OK to launcher
            }
            .sortedByDescending { it.first.priority }
        val candidateLauncher = sortedLaunchers.find { it.second.javaClass.name == launcherClass }
        if (candidateLauncher == null) {
            logger.error("No valid launchers for {} and class {}", options, launcherClass)
            printLaunchers()
            logger.error("Available launchers: {}", launchers.map { "${it.name} - [${it.javaClass.name}]" })
            invalidLaunchers.forEach { (validation, launcher) ->
                if (validation is Validation.Invalid) {
                    logger.error("{}: {}", launcher::class.java.simpleName, validation.reason)
                }
            }
            exitWith(ExitStatus.INVALID_CLI)
        }
        return candidateLauncher.second
    }

    private fun validateIncarnations() {
        require(SupportedIncarnations.getAvailableIncarnations().isNotEmpty()) {
            logger.error(
                """
                    Alchemist requires an incarnation to execute, but none was found in the classpath.
                    Please refer to the alchemist manual at https://alchemistsimulator.github.io to learn more on
                    how to include incarnations in your project.
                    If you believe this is a bug, please open a report at:
                    https://github.com/AlchemistSimulator/Alchemist/issues/new/choose
                """.trimIndent().trim().replace('\n', ' '),
            )
            "There are no incarnations in the classpath, no simulation can get executed"
        }
    }

    private fun AlchemistExecutionOptions.classifyLaunchers() = launchers
        .map { it.validate(this) to it }
        .partition { (validation, _) -> validation is Validation.OK }

    private fun printLaunchers() {
        logger.warn("Available launchers: {}", launchers.map { "${it.name} - [${it.javaClass.name}]" })
    }

    /**
     * Call this method to enable testing mode, preventing Alchemist from shutting down the JVM.
     */
    fun enableTestMode() {
        isNormalExecution = false
    }

    private fun setVerbosity(verbosity: Verbosity) {
        (LoggerFactory.getLogger("org.reflections.Reflections") as Logger).level = Level.OFF
        setLogbackLoggingLevel(verbosity.logLevel)
    }

    private fun setLogbackLoggingLevel(level: Level) {
        val root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
        root.level = level
        root.addAppender(
            ConsoleAppender<ILoggingEvent?>().apply {
                encoder = PatternLayoutEncoder().apply {
                    pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{20} - %msg%n"
                }
            },
        )
    }

    private fun exitWith(status: ExitStatus): Nothing {
        if (isNormalExecution) {
            System.exit(status.ordinal)
        }
        throw AlchemistWouldHaveExitedException(status.ordinal)
    }

    private fun exitBecause(reason: String, status: ExitStatus, exception: Exception? = null): Nothing {
        when {
            exception == null -> logger.error(reason)
            else -> logger.error(reason, exception)
        }
        exitWith(status)
    }

    private enum class ExitStatus {
        OK, INVALID_CLI, NO_LOGGER, NUMBER_FORMAT_ERROR, MULTIPLE_VERBOSITY
    }

    /**
     * This exception is thrown in place of calling [System.exit] when the simulator is used in debug mode.
     * The [exitStatus] returns the exit status the execution would have had.
     *
     * @property exitStatus exit status
     */
    data class AlchemistWouldHaveExitedException(
        val exitStatus: Int,
    ) : RuntimeException("Alchemist would have exited with $exitStatus")
}
