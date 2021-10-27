/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import it.unibo.alchemist.cli.CLIMaker
import it.unibo.alchemist.launch.Launcher
import it.unibo.alchemist.launch.Priority
import it.unibo.alchemist.launch.Validation
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.ParseException
import org.slf4j.LoggerFactory
import org.slf4j.helpers.NOPLoggerFactory

private typealias ValidLauncher = Pair<Validation.OK, Launcher>
/**
 * Starts Alchemist.
 */
object Alchemist {
    private const val HEADLESS = "hl"
    private const val VARIABLES = "var"
    private const val BATCH = 'b'
    private const val FXUI = "fxui"
    private const val DISTRIBUTED = 'd'
    private const val GRAPHICS = 'g'
    private const val HELP = 'h'
    private const val INTERVAL = 'i'
    private const val SERVER = 's'
    private const val PARALLELISM = 'p'
    private const val TIME = 't'
    private const val YAML = 'y'
    private val logger = LoggerFactory.getLogger(Alchemist::class.java)
    private val launchers: List<Launcher> = ClassPathScanner
        .subTypesOf(Launcher::class.java, inPackage = "it.unibo.alchemist")
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
                    throw IllegalStateException("Cannot instance or access an instance of $clazz")
                }
            }
        }
    private val logLevels = mapOf(
        "v" to Level.INFO,
        "vv" to Level.DEBUG,
        "vvv" to Level.ALL,
        "q" to Level.ERROR,
        "qq" to Level.OFF
    )
    /**
     * Set this to false for testing purposes.
     */
    private var isNormalExecution = true

    private inline fun <reified T : Number> CommandLine.hasNumeric(name: Char, converter: String.() -> T?): T? =
        getOptionValue(name)?.let {
            val value = converter(it)
            when {
                value == null ->
                    exitBecause("Not a valid ${T::class.simpleName}: $it", ExitStatus.NUMBER_FORMAT_ERROR)
                else -> value
            }
        }

    /**
     * @param args
     * the argument for the program
     */
    @JvmStatic
    fun main(args: Array<String>) {
        if (LoggerFactory.getILoggerFactory().javaClass == NOPLoggerFactory::class.java) {
            println("Alchemist could not load the output module (broken SLF4J depedencies?)") // NOPMD
            exitWith(ExitStatus.NO_LOGGER)
        }
        val opts = CLIMaker.getOptions()
        fun printHelp() = HelpFormatter().printHelp("java -jar alchemist-redist-{version}.jar", opts)
        val parser: CommandLineParser = DefaultParser()
        try {
            val cmd = parser.parse(opts, args)
            setVerbosity(cmd)
            val options = cmd.toAlchemist
            if (options.isEmpty || options.help) {
                printHelp()
                exitWith(if (options.help) ExitStatus.OK else ExitStatus.INVALID_CLI)
            }
            require(SupportedIncarnations.getAvailableIncarnations().isNotEmpty()) {
                logger.error(
                    """
                    Alchemist requires an incarnation to execute, but none was found in the classpath.
                    Please refer to the alchemist manual at https://alchemistsimulator.github.io to learn more on
                    how to include incarnations in your project.
                    If you believe this is a bug, please open a report at:
                    https://github.com/AlchemistSimulator/Alchemist/issues/new/choose
                    """.trimIndent().trim().replace('\n', ' ')
                )
                "There are no incarnations in the classpath, no simulation can get executed"
            }
            val (validLaunchers, invalidLaunchers) = options.classifyLaunchers()
            val sortedLaunchers: List<ValidLauncher> = validLaunchers
                .map { (validation, launcher) ->
                    validation as Validation.OK to launcher
                }
                .sortedByDescending { it.first.priority }
            when {
                sortedLaunchers.size == 1 -> sortedLaunchers.first().launch(options)
                validLaunchers.size > 1 ->
                    if (sortedLaunchers.priorityOf(0) > sortedLaunchers.priorityOf(1)) {
                        sortedLaunchers.first().launch(options)
                    } else {
                        logger.error(
                            "Multiple execution strategies match options {} with the same priority, available: {}",
                            options,
                            sortedLaunchers,
                        )
                        exitWith(ExitStatus.INVALID_CLI)
                    }
                else -> {
                    logger.error("No valid launchers for {}", options)
                    printLaunchers()
                    logger.error("Available launchers: {}", launchers.map { it.name })
                    invalidLaunchers.forEach { (validation, launcher) ->
                        if (validation is Validation.Invalid) {
                            logger.error("{}: {}", launcher::class.java.simpleName, validation.reason)
                        }
                    }
                    exitWith(ExitStatus.INVALID_CLI)
                }
            }
        } catch (e: ParseException) {
            logger.error("Your command sequence could not be parsed.", e)
            printHelp()
            exitWith(ExitStatus.INVALID_CLI)
        }
    }

    private fun AlchemistExecutionOptions.classifyLaunchers() = launchers
        .map { it.validate(this) to it }
        .partition { (validation, _) -> validation is Validation.OK }

    private fun printLaunchers() {
        logger.warn("Available launchers: {}", launchers.map { it.name })
    }

    private fun List<ValidLauncher>.priorityOf(index: Int) = get(index).first.priority

    private fun ValidLauncher.launch(options: AlchemistExecutionOptions) {
        if (first.priority !is Priority.Normal) {
            printLaunchers()
        }
        second(options)
    }
    /**
     * Call this method to enable testing mode, preventing Alchemist from shutting down the JVM.
     */
    fun enableTestMode() {
        isNormalExecution = false
    }

    private fun setVerbosity(cmd: CommandLine) {
        val verbosity = logLevels.filterKeys { cmd.hasOption(it) }.values
        when {
            verbosity.size > 1 ->
                exitBecause(
                    "Conflicting verbosity specification. Only one of ${logLevels.keys} can be specified.",
                    ExitStatus.MULTIPLE_VERBOSITY
                )
            verbosity.size == 1 -> setLogbackLoggingLevel(verbosity.first())
        }
    }

    private fun setLogbackLoggingLevel(level: Level) {
        val root =
            LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
        root.level = level
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

    private val CommandLine.toAlchemist: AlchemistExecutionOptions
        get() = AlchemistExecutionOptions(
            server = getOptionValue(SERVER),
            help = hasOption(HELP),
            batch = hasOption(BATCH),
            distributed = getOptionValue(DISTRIBUTED),
            endTime = hasNumeric(TIME, kotlin.String::toDoubleOrNull)
                ?: AlchemistExecutionOptions.defaultEndTime,
            graphics = getOptionValue(GRAPHICS),
            fxui = hasOption(FXUI),
            headless = hasOption(HEADLESS),
            interval = hasNumeric(INTERVAL, kotlin.String::toDoubleOrNull)
                ?: AlchemistExecutionOptions.defaultInterval,
            parallelism = hasNumeric(PARALLELISM, kotlin.String::toIntOrNull)
                ?: AlchemistExecutionOptions.defaultParallelism,
            variables = getOptionValues(VARIABLES)?.toList()
                ?: emptyList(),
            configuration = getOptionValue(YAML)
        )

    private enum class ExitStatus {
        OK, INVALID_CLI, NO_LOGGER, NUMBER_FORMAT_ERROR, MULTIPLE_VERBOSITY
    }

    /**
     * This exception is thrown in place of calling [System.exit] when the simulator is used in debug mode.
     * The [exitStatus] returns the exit status the execution would have had.
     */
    data class AlchemistWouldHaveExitedException(
        val exitStatus: Int
    ) : RuntimeException("Alchemist would have exited with $exitStatus")
}
