/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.export

import com.google.common.base.Charsets
import it.unibo.alchemist.model.implementations.times.DoubleTime
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.Time
import java.io.File
import java.io.PrintStream
import java.text.SimpleDateFormat
import java.util.Arrays
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.io.path.Path

/**
 * Writes on file data provided by a number of {@link Extractor}s. Produces a
 * CSV with '#' as comment character.e
 * @param filename the name the file to export data to.
 * @param interval the sampling time, defaults to [AbstractExporter.DEFAULT_INTERVAL].
 */

class CSVExporter<T, P : Position<P>> @JvmOverloads constructor(
    private val filename: String = "",
    var interval: Double = DEFAULT_INTERVAL,
    private val appendTime: Boolean = false
) : AbstractExporter<T, P>(interval) {

    companion object {
        /**
         * Character used to separate comments from data on export files.
         */
        const val SEPARATOR = "#####################################################################"

        /**
         * If no path is specified or the input path is wrong, the output file will be placed inside this folder.
         */
        const val DEFAULT_PATH = "/build/exports/"
    }
    private lateinit var out: PrintStream

    /**
     * The path of the export output file.
     */
    lateinit var outputFile: String

    override fun setupExportEnvironment(environment: Environment<T, P>) {
        val exportDir = File(
            Path(File("").absolutePath).parent.toString() +
                DEFAULT_PATH + filename
        )
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }
        outputFile = exportDir.path + '/' + filename +
            "_" + variablesDescriptor + "_" + "${if (appendTime) System.currentTimeMillis() else ""}"
        out = PrintStream(outputFile, Charsets.UTF_8.name())
        out.println(SEPARATOR)
        out.print("# Alchemist log file - simulation started at: ")
        val isoTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ", Locale.US)
        isoTime.timeZone = TimeZone.getTimeZone("UTC")
        out.print(isoTime.format(Date()))
        out.println(" #")
        out.println(SEPARATOR)
        out.println(" #")
        out.println(variablesDescriptor)
        out.println(" #")
        out.println("# The columns have the following meaning: ")
        out.print("# ")
        dataExtractor.stream()
            .flatMap {
                it.names.stream()
            }.forEach {
                out.print(it)
                out.print(" ")
            }
        out.println()
        exportData(environment, null, DoubleTime(), 0)
    }

    override fun exportData(environment: Environment<T, P>, reaction: Reaction<T>?, time: Time, step: Long) {
        dataExtractor.stream()
            .flatMapToDouble { Arrays.stream(it.extractData(environment, reaction, time, step)) }
            .forEach {
                out.print(it)
                out.print(' ')
            }
        out.println()
    }

    override fun closeExportEnvironment(environment: Environment<T, P>, time: Time, step: Long) {
        out.println(SEPARATOR)
        out.print("# End of data export. Simulation finished at: ")
        val isoTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ", Locale.US)
        isoTime.timeZone = TimeZone.getTimeZone("UTC")
        out.print(isoTime.format(Date()))
        out.println(" #")
        out.println(SEPARATOR)
        out.close()
    }
}
