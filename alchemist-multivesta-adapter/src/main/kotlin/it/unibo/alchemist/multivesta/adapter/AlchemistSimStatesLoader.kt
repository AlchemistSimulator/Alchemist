/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.multivesta.adapter

import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.FileReader
import java.util.Collections

/**
 * This object is used to load the simulation states from the relative output file.
 */
object AlchemistSimStatesLoader {

    private val logger = LoggerFactory.getLogger(AlchemistSimStatesLoader::class.java)

    /**
     * Reads the simulation states from the given file.
     * @param filepath the path of the file to read
     * @param separator the separator used in the file
     * @return the list of simulation states
     */
    fun fromCSV(filepath: String, separator: String = " "): List<AlchemistStateObservations> {
        logger.info("Reading file $filepath")
        val fields: List<String>
        val simStates: List<AlchemistStateObservations>
        BufferedReader(FileReader(filepath)).use { reader ->
            fields = parseHeader(reader, separator)
            reader.readLine() // skip the next line
            simStates = readAlchemistSimStates(reader, fields, separator)
        }
        return Collections.unmodifiableList(simStates)
    }

    private fun parseHeader(reader: BufferedReader, separator: String): List<String> {
        var line: String?
        while (reader.readLine().also { line = it?.trim() } != null && line?.startsWith("#") == true) {
            if (line?.contains("meaning") == true) {
                reader.readLine().also { line = it?.replace("#", "")?.trim() }
                return line?.split(separator).orEmpty()
            }
        }
        return emptyList()
    }

    private fun readAlchemistSimStates(reader: BufferedReader, fields: List<String>, separator: String):
        List<AlchemistStateObservations> {
        val simStates = mutableListOf<AlchemistStateObservations>()
        var line: String?
        while (reader.readLine().also { line = it?.trim() } != null) {
            if (line?.startsWith("#") == true) {
                continue
            }
            val values = line?.split(separator).orEmpty()
            val time = values[0].toDouble()
            val simState = AlchemistStateObservations(time)
            for (i in 1 until values.size) {
                simState.addObservation(fields[i], values[i].toDouble())
            }
            simStates.add(simState)
        }
        return simStates
    }
}
