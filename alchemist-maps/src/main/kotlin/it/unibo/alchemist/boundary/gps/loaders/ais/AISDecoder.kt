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
import kotlin.time.Instant

/**
 * Utility object to decode AIS raw messages.
 */
object AISDecoder {
    private const val DATE_TIME_PREFIX = "!DATE-TIME,"
    private const val DATE_TIME_SEPARATOR = 'T'
    private const val FALLBACK_DATE = "1970-01-01"
    private const val YEAR_DIGITS = 4
    private const val MONTH_START = YEAR_DIGITS
    private const val MONTH_END = 6
    private const val DAY_DIGITS = 2
    private val DATE_PATTERN = Regex("""\d{8}""")

    /**
     * @param date the payload date, formatted as an ISO local date (`yyyy-MM-dd`), or a resource name embedding a
     * date formatted as `yyyyMMdd`.
     * @return the message parsed from a raw [String] to [AisMessage] and maps it to the timestamp of the raw message.
     **/
    fun parsePayload(payload: String, date: String): List<Pair<Instant, AisMessage>> =
        parsePayload(payload, startOfDay(date.dateFromResourceNameOrSelf()))

    /**
     * @param date the payload date as an instant. Messages preceding the first explicit timestamp use this instant.
     * @return the message parsed from a raw [String] to [AisMessage] and maps it to the timestamp of the raw message.
     **/
    fun parsePayload(payload: String, date: Instant): List<Pair<Instant, AisMessage>> {
        val payloadDate = date.toIsoDate()
        var currentTimestamp = date
        return payload.lines().mapNotNull {
            when {
                it.startsWith(DATE_TIME_PREFIX) -> {
                    val time = it.substringAfter(DATE_TIME_PREFIX).trim()
                    currentTimestamp = Instant.parse("${payloadDate}T${time}Z")
                    null
                }
                it.isBlank() -> null
                else -> {
                    AISMessageParser.parseLine(Vdm(), it)
                        .takeIf { aisMessage -> aisMessage.isCompletePacket }
                        ?.let(AISMessageParser::build)
                        ?.let { message -> currentTimestamp to message }
                }
            }
        }
    }

    /** Parses all the raw AIS lines contained in a [File].
     * @param file the [File] from which parse AIS info.
     **/
    fun parseFile(file: File, date: String = dateFrom(file.name)): List<Pair<Instant, AisMessage>> =
        parsePayload(file.readText(Charsets.UTF_8), date)

    /**
     * Extract date from a file name.
     * @param resourceName the name of file.
     * @return the date or a fallback date.
     */
    private fun dateFrom(resourceName: String): String = DATE_PATTERN
        .find(resourceName)
        ?.value
        ?.let { date -> date.toIsoDate().takeIf { runCatching { startOfDay(it) }.isSuccess } }
        ?: FALLBACK_DATE

    private fun String.dateFromResourceNameOrSelf(): String =
        takeIf { runCatching { startOfDay(it) }.isSuccess } ?: dateFrom(this)

    private fun startOfDay(date: String): Instant = Instant.parse("${date}T00:00:00Z")

    private fun Instant.toIsoDate(): String = toString().substringBefore(DATE_TIME_SEPARATOR)

    private fun String.toIsoDate(): String =
        "${take(YEAR_DIGITS)}-${substring(MONTH_START, MONTH_END)}-${takeLast(DAY_DIGITS)}"
}
