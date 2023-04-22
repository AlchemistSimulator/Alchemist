/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.multivesta.adapter

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import it.unibo.alchemist.AlchemistExecutionOptions
import it.unibo.alchemist.cli.CLIMaker
import it.unibo.alchemist.loader.export.Extractor
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.multivesta.adapter.launch.AlchemistMultiVestaSimulationLauncher
import it.unibo.alchemist.multivesta.adapter.multivesta.MultiVestaEntryPoint
import it.unibo.alchemist.multivesta.adapter.utils.SeedsManager
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.ParseException
import org.slf4j.LoggerFactory
import org.slf4j.helpers.NOPLoggerFactory
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files
import kotlin.system.exitProcess

/**
 * Starts Alchemist.
 */
object AlchemistMultiVesta {
    /**
     * The maximum number of seconds to wait for the simulation to execute one time step.
     */
    const val MAX_WAIT_SECONDS = 30L

    private const val MULTIVESTA = "mv"
    private const val ALREADY_EXECUTED_SIMULATION = "aes"
    private const val HEADLESS = "hl"
    private const val VARIABLES = "var"
    private const val BATCH = 'b'
    private const val FXUI = "fxui"
    private const val DISTRIBUTED = 'd'
    private const val GRAPHICS = 'g'
    private const val HELP = 'h'
    private const val SERVER = 's'
    private const val PARALLELISM = 'p'
    private const val TIME = 't'
    private const val YAML = 'y'
    private const val EXTRACTOR = 'e'
    private val logger = LoggerFactory.getLogger(AlchemistMultiVesta::class.java)
    private val logLevels = mapOf(
        "v" to Level.INFO,
        "vv" to Level.DEBUG,
        "vvv" to Level.ALL,
        "q" to Level.ERROR,
        "qq" to Level.OFF,
    )

    /**
     * Set this to false for testing purposes.
     */
    private const val isNormalExecution = true

    private inline fun <reified T : Number> CommandLine.hasNumeric(name: Char, converter: String.() -> T?): T? =
        getOptionValue(name)?.let {
            when (val value = converter(it)) {
                null ->
                    exitBecause("Not a valid ${T::class.simpleName}: $it", ExitStatus.NUMBER_FORMAT_ERROR)
                else -> value
            }
        }

    private fun appendSeedsToYmlFile(seed: Int, configurationPath: String): String {
        try {
            val originalConfigFile = File(configurationPath)
            val newConfigFilePath = originalConfigFile.parentFile.absolutePath + "/seeds/" +
                originalConfigFile.nameWithoutExtension + "_seed_$seed.yml"
            File(newConfigFilePath).parentFile.mkdirs()
            val writer = BufferedWriter(FileWriter(newConfigFilePath))
            val data = String(Files.readAllBytes(originalConfigFile.toPath()))
            writer.write(data)
            writer.newLine()
            writer.write("seeds:")
            writer.newLine()
            writer.write("  scenario: $seed")
            writer.newLine()
            writer.write("  simulation: $seed")
            writer.close()
            return newConfigFilePath
        } catch (e: IOException) {
            exitBecause("Error while appending seeds to yml file", ExitStatus.IO_ERROR, e)
        }
    }

    /**
     * Parse the command line arguments.
     */
    private fun parseOptions(args: Array<String>): CommandLine {
        val opts = CLIMaker.getOptions()
        opts.addOption(MULTIVESTA, true, "All the arguments to pass to MultiVesta, as a single string")
        opts.addOption(
            ALREADY_EXECUTED_SIMULATION,
            false,
            "If the simulation has been already executed, this option allows to use a simulation adapter " +
                "that loads the simulation from the output files",
        )
        fun printHelp() = HelpFormatter().printHelp("java -jar alchemist-redist-{version}.jar", opts)
        val parser: CommandLineParser = DefaultParser()
        try {
            val cmd: CommandLine = parser.parse(opts, args)
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
                    """.trimIndent().trim().replace('\n', ' '),
                )
                "There are no incarnations in the classpath, no simulation can get executed"
            }
            return cmd
        } catch (e: ParseException) {
            logger.error("Your command sequence could not be parsed.", e)
            printHelp()
            exitWith(ExitStatus.INVALID_CLI)
        }
    }

    private fun saveAlchemistArgs(args: Array<String>) {
        val alchemistArgs = args.joinToString(" ").substringBefore("-mv").trim()
        File("alchemist_args.txt").writeText(alchemistArgs)
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
        try {
            val cmd = parseOptions(args)
            saveAlchemistArgs(args)
            if (cmd.isSimulationAlreadyExecuted) {
                logger.info("Reusing already executed simulation")
                SeedsManager.createAvailableSeedsFile(List(100) { it })
            } else {
                logger.info("Performing a new simulation")
            }
            MultiVestaEntryPoint.launch(cmd.toMultiVesta)
        } catch (e: ClassNotFoundException) {
            exitBecause("Can't launch MultiVesta since it is not in classpath.", ExitStatus.CLASS_NOT_FOUND, e)
        }
    }

    /**
     * Launch a new simulation with the specified seed. This method is thread-safe.
     */
    fun launchSimulation(seed: Int): AlchemistSimulationAdapter {
        logger.info("Launching simulation with seed $seed")
        val cmd = parseOptions(File("alchemist_args.txt").readText().split(" ").toTypedArray())
        var options = cmd.toAlchemist
        options.configuration?.let { configPath ->
            appendSeedsToYmlFile(seed, configPath).let { newConfigPath ->
                options = options.copy(configuration = newConfigPath)
            }
        }
        if (cmd.isSimulationAlreadyExecuted) {
            return ExperimentAlchemistSimulationAdapter(ExperimentAlchemistSimulationAdapter.GridType.REGULAR, seed)
            // to do: load the specified CSVAlchemistSimulationAdapter by using reflection
        }
        val launcher = AlchemistMultiVestaSimulationLauncher()
        launcher.launch(options)
        return when (cmd.extractor) {
            null -> AlchemistSimulationAdapterWithExporter(launcher.simulation)
            else -> {
                val extractorClass = Class.forName(cmd.extractor)
                val extractorInstance = extractorClass.getConstructor().newInstance() as Extractor<*>
                AlchemistSimulationAdapterWithExtractor(launcher.simulation, extractorInstance)
            }
        }
    }

    private fun setVerbosity(cmd: CommandLine) {
        (LoggerFactory.getLogger("org.reflections.Reflections") as Logger).level = Level.OFF
        val verbosity = logLevels.filterKeys { cmd.hasOption(it) }.values
        when {
            verbosity.size > 1 ->
                exitBecause(
                    "Conflicting verbosity specification. Only one of ${logLevels.keys} can be specified.",
                    ExitStatus.MULTIPLE_VERBOSITY,
                )
            verbosity.size == 1 -> setLogbackLoggingLevel(verbosity.first())
            else -> setLogbackLoggingLevel(Level.WARN)
        }
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
            exitProcess(status.ordinal)
        }
        throw AlchemistWouldHaveExitedException(status.ordinal)
    }

    private fun exitBecause(reason: String, status: ExitStatus, exception: Exception? = null): Nothing {
        when (exception) {
            null -> logger.error(reason)
            else -> logger.error(reason, exception)
        }
        exitWith(status)
    }

    private val CommandLine.toAlchemist: AlchemistExecutionOptions
        get() {
            return AlchemistExecutionOptions(
                server = getOptionValue(SERVER),
                help = hasOption(HELP),
                batch = hasOption(BATCH),
                distributed = getOptionValue(DISTRIBUTED),
                endTime = hasNumeric(TIME, String::toDoubleOrNull)
                    ?: AlchemistExecutionOptions.defaultEndTime,
                graphics = getOptionValue(GRAPHICS),
                fxui = hasOption(FXUI),
                headless = hasOption(HEADLESS),
                parallelism = hasNumeric(PARALLELISM, String::toIntOrNull)
                    ?: AlchemistExecutionOptions.defaultParallelism,
                variables = getOptionValues(VARIABLES)?.toList().orEmpty(),
                configuration = getOptionValue(YAML),
            )
        }

    private val CommandLine.toMultiVesta: Array<String>
        get() = getOptionValue(MULTIVESTA)?.split(" ").orEmpty().toTypedArray()

    private val CommandLine.isSimulationAlreadyExecuted: Boolean
        get() = hasOption(ALREADY_EXECUTED_SIMULATION)

    private val CommandLine.extractor: String?
        get() = getOptionValue(EXTRACTOR)

    private enum class ExitStatus {
        OK, INVALID_CLI, NO_LOGGER, NUMBER_FORMAT_ERROR, MULTIPLE_VERBOSITY, CLASS_NOT_FOUND, IO_ERROR
    }

    /**
     * This exception is thrown in place of calling [System.exit] when the simulator is used in debug mode.
     * The [exitStatus] returns the exit status the execution would have had.
     */
    data class AlchemistWouldHaveExitedException(
        val exitStatus: Int,
    ) : RuntimeException("Alchemist would have exited with $exitStatus")
}
