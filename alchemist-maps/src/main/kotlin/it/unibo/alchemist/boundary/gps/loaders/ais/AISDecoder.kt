/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.gps.loaders.ais

import dk.dma.ais.message.AisMessage
import dk.dma.ais.sentence.Vdm
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Utility object to decode AIS raw messages.
 */
object AISDecoder {
    private const val DATE_TIME_PREFIX = "!DATE-TIME,"
    private const val FALLBACK_DATE = "1970-01-01"
    private val DATE_PATTERN = Regex("""\d{8}""")

    /**
     * @param date the payload date, formatted as an ISO local date (`yyyy-MM-dd`).
     * @throws java.time.format.DateTimeParseException if [date] is not a valid ISO local date.
     * @return the message parsed from a raw [String] to [AisMessage] and maps it to the timestamp of the raw message.
     **/
    fun parsePayload(payload: String, date: String): List<Pair<Instant, AisMessage>> {
        val payloadDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
        var currentTimestamp = Instant.parse("${payloadDate}T00:00:00Z")
        return payload.lines().mapNotNull {
            when {
                it.startsWith(DATE_TIME_PREFIX) -> {
                    val time = it.substringAfter(DATE_TIME_PREFIX).trim()
                    currentTimestamp = Instant.parse("${payloadDate}T${time}Z")
                    null
                }
                it.isBlank() -> null
                else -> {
                    val vdm = AISMessageParser.parseLine(Vdm(), it)
                    vdm.takeIf { aisMessage -> aisMessage.isCompletePacket }
                        ?.let(AISMessageParser::build)
                        ?.let { message -> currentTimestamp to message }
                }
            }
        }
    }

    /** Parses all the raw AIS lines contained in a [File].
     * @param file the [File] from which parse AIS info.
     **/
    fun parseFile(file: File): List<Pair<Instant, AisMessage>> =
        parsePayload(file.readText(Charsets.UTF_8), dateFrom(file.name))

    /**
     * Extract date from a file name.
     * @param resourceName the name of file.
     * @return the date or a fallback date.
     */
    fun dateFrom(resourceName: String): String = DATE_PATTERN
        .find(resourceName)
        ?.value
        ?.let { date ->
            runCatching {
                LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE).toString()
            }.getOrNull()
        }
        ?: FALLBACK_DATE
}
