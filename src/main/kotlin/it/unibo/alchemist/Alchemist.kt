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
import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.boundary.Loader
import it.unibo.alchemist.config.Verbosity
import it.unibo.alchemist.model.SupportedIncarnations
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.cli.default
import kotlinx.cli.multiple
import org.kaikikm.threadresloader.ResourceLoader
import org.slf4j.LoggerFactory
import org.slf4j.helpers.NOPLoggerFactory
import java.io.File

/**
 * Starts Alchemist.
 */
object Alchemist {
    private val logger = LoggerFactory.getLogger(Alchemist::class.java)

    /**
     * Set this to false for testing purposes.
     */
    private var isNormalExecution = true

    /**
     * @param args
     * the argument for the program
     */
    @OptIn(ExperimentalCli::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val parser = ArgParser("alchemist")
        val run = createRunCommand(parser)
        parser.subcommands(run)
        parser.parse(args)
    }

    @OptIn(ExperimentalCli::class)
    private fun createRunCommand(parser: ArgParser): Subcommand {
        class Run : Subcommand("run", "Run a simulation or a batch of simulations") {

            val simulationFile by parser.argument(
                type = ArgType.String,
                fullName = "simulation configuration file",
                description = """
                File containing simulation configuration to be executed.
                """.trimIndent(),
            )

            val verbosity by parser.option(
                type = ArgType.Choice<Verbosity>(),
                fullName = "verbosity",
                description = """
                Simulation logging verbosity level. Choose one of the following values:
               
                - debug
                - info
                - warn
                - error
                - all
                - off
                
                defaults to "warn"
                """.trimIndent(),
            ).default(Verbosity.WARN)

            val overrides by parser.option(
                type = ArgType.String,
                fullName = "override",
                description = """
                Valid yaml files used to override simulation config,
                files are applied sequentially.
                """.trimIndent(),
            ).multiple()

            override fun execute() {
                executeSimlation(simulationFile, verbosity, overrides)
            }
        }
        return Run()
    }

    private fun executeSimlation(
        simulationFile: String,
        verbosity: Verbosity,
        overrides: List<String>,
    ) {
        validateOutputModule()
        validateIncarnations()
        setVerbosity(verbosity)
        val loader = createLoader(simulationFile, overrides)
        loader.launcher.launch(loader)
    }

    private fun validateOutputModule() {
        if (LoggerFactory.getILoggerFactory().javaClass == NOPLoggerFactory::class.java) {
            println("Alchemist could not load the output module (broken SLF4J depedencies?)") // NOPMD
            exitWith(ExitStatus.NO_LOGGER)
        }
    }

    private fun createLoader(simulationFile: String, overrides: List<String>): Loader {
        val url = ResourceLoader.getResource(simulationFile)
            ?: File(simulationFile).takeIf { it.exists() && it.isFile }?.toURI()?.toURL()
            ?: error("No classpath resource or file $simulationFile was found")
        return LoadAlchemist.from(
            url,
            overrides,
        )
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
