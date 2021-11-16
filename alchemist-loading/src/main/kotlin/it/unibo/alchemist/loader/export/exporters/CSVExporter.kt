/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.export.exporters

import com.google.common.base.Charsets
import it.unibo.alchemist.model.implementations.times.DoubleTime
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.Time
import org.slf4j.LoggerFactory
import java.io.File
import java.io.PrintStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.io.path.absolutePathString
import kotlin.io.path.createTempDirectory

/**
 * Writes on file data provided by a number of {@link Extractor}s. Produces a
 * CSV with '#' as comment character.e
 * @param fileNameRoot the starting name of the file to export data to.
 * @param interval the sampling time, defaults to [AbstractExporter.DEFAULT_INTERVAL].
 * @param path if no path is specified it will generate the file inside a temporary folder.
 * @param appendTime if true it will always generate a new file, false to overwrite.
 */
class CSVExporter<T, P : Position<P>> @JvmOverloads constructor(
    private val fileNameRoot: String = "",
    val interval: Double = DEFAULT_INTERVAL,
    private val path: String = createTempDirectory("alchemist-export").absolutePathString() + '/'.also {
        logger.warn("No output folder specified but export required. Alchemist will export data in $it")
    },
    private val appendTime: Boolean = false
) : AbstractExporter<T, P>(interval) {

    companion object {
        /**
         * Character used to separate comments from data on export files.
         */
        private const val SEPARATOR = "#####################################################################"
        private val logger = LoggerFactory.getLogger(CSVExporter::class.java)
    }

    override val exportDestination: String
        get() = path + fileNameRoot + "_" +
            variablesDescriptor + "_" + "${if (appendTime) System.currentTimeMillis() else ""}"

    private lateinit var outputPrintStream: PrintStream

    override fun setup(environment: Environment<T, P>) {
        if (!File(path).exists()) {
            File(path).mkdirs()
        }
        outputPrintStream = PrintStream(exportDestination, Charsets.UTF_8.name())
        outputPrintStream.println(SEPARATOR)
        outputPrintStream.print("# Alchemist log file - simulation started at: ")
        val isoTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ", Locale.US)
        isoTime.timeZone = TimeZone.getTimeZone("UTC")
        outputPrintStream.print(isoTime.format(Date()))
        outputPrintStream.println(" #")
        outputPrintStream.println(SEPARATOR)
        outputPrintStream.println(" #")
        outputPrintStream.println(variablesDescriptor)
        outputPrintStream.println(" #")
        outputPrintStream.println("# The columns have the following meaning: ")
        outputPrintStream.print("# ")
        dataExtractors.flatMap {
            it.getColumnNames()
        }.forEach {
            outputPrintStream.print(it)
            outputPrintStream.print(" ")
        }
        outputPrintStream.println()
        exportData(environment, null, DoubleTime(), 0)
    }

    override fun exportData(environment: Environment<T, P>, reaction: Reaction<T>?, time: Time, step: Long) {
        dataExtractors.forEach {
            it.extractData(environment, reaction, time, step).values.forEach {
                value ->
                outputPrintStream.print(value)
                outputPrintStream.print(' ')
            }
        }
        outputPrintStream.println()
    }

    override fun close(environment: Environment<T, P>, time: Time, step: Long) {
        outputPrintStream.println(SEPARATOR)
        outputPrintStream.print("# End of data export. Simulation finished at: ")
        val isoTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ", Locale.US)
        isoTime.timeZone = TimeZone.getTimeZone("UTC")
        outputPrintStream.print(isoTime.format(Date()))
        outputPrintStream.println(" #")
        outputPrintStream.println(SEPARATOR)
        outputPrintStream.close()
    }
}
