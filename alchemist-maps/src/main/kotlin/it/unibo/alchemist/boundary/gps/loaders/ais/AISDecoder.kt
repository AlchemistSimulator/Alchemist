/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.gps.loaders.ais

import dk.dma.ais.binary.SixbitException
import dk.dma.ais.message.AisMessage
import dk.dma.ais.message.AisMessageException
import dk.dma.ais.sentence.SentenceException
import dk.dma.ais.sentence.Vdm
import java.io.File
import kotlin.time.Instant
import org.slf4j.LoggerFactory

/**
 * Utility object to decode AIS raw messages.
 */
object AISDecoder {
    private const val DATE_TIME_PREFIX = "!DATE-TIME,"
    private const val DATE_TIME_SEPARATOR = 'T'
    private const val DAY_DIGITS = 2
    private const val FALLBACK_DATE = "1970-01-01"
    private const val INVALID_CHECKSUM = "Invalid checksum"
    private const val MONTH_END = 6
    private const val MONTH_START = 4
    private const val OUT_OF_SEQUENCE_SENTENCE = "Out of sequence sentence:"
    private const val YEAR_DIGITS = 4

    private val DATE_PATTERN = Regex("""\d{8}""")
    private val logger = LoggerFactory.getLogger(AISDecoder::class.java)

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

    /** Parses all the raw AIS lines contained in a [File].
     * @param file the [File] from which parse AIS info.
     **/
    fun parseFile(file: File, date: String = dateFrom(file.name)): List<Pair<Instant, AisMessage>> =
        parsePayload(file.readText(Charsets.UTF_8), date)

    /**
     * @param date the payload date as an instant. Messages preceding the first explicit timestamp use this instant.
     * @return the message parsed from a raw [String] to [AisMessage] and maps it to the timestamp of the raw message.
     **/
    fun parsePayload(payload: String, date: Instant): List<Pair<Instant, AisMessage>> {
        val payloadDate = date.toIsoDate()
        var currentTimestamp = date
        var vdmAccumulator = Vdm()
        return buildList {
            for (line in payload.lines()) {
                when {
                    line.startsWith(DATE_TIME_PREFIX) -> {
                        val time = line.substringAfter(DATE_TIME_PREFIX).trim()
                        currentTimestamp = Instant.parse("${payloadDate}T${time}Z")
                    }
                    line.isBlank() -> Unit
                    else -> vdmAccumulator = vdmAccumulator.parseSentence(line) { message ->
                        add(currentTimestamp to message)
                    }
                }
            }
        }
    }

    /**
     * @param date the payload date, formatted as an ISO local date (`yyyy-MM-dd`), or a resource name embedding a
     * date formatted as `yyyyMMdd`.
     * @return the message parsed from a raw [String] to [AisMessage] and maps it to the timestamp of the raw message.
     **/
    fun parsePayload(payload: String, date: String): List<Pair<Instant, AisMessage>> =
        parsePayload(payload, startOfDay(date.dateFromResourceNameOrSelf()))

    private fun startOfDay(date: String): Instant = Instant.parse("${date}T00:00:00Z")

    private fun Instant.toIsoDate(): String = toString().substringBefore(DATE_TIME_SEPARATOR)

    private val SentenceException.hasInvalidChecksum: Boolean
        get() = message?.contains(INVALID_CHECKSUM) == true

    private val SentenceException.isOutOfSequence: Boolean
        get() = message?.contains(OUT_OF_SEQUENCE_SENTENCE) == true

    private val SentenceException.isRecoverable: Boolean
        get() = isOutOfSequence || hasInvalidChecksum

    private fun String.dateFromResourceNameOrSelf(): String =
        takeIf { runCatching { startOfDay(it) }.isSuccess } ?: dateFrom(this)

    private fun String.toIsoDate(): String =
        "${take(YEAR_DIGITS)}-${substring(MONTH_START, MONTH_END)}-${takeLast(DAY_DIGITS)}"

    private fun Vdm.parseSentence(line: String, onComplete: (AisMessage) -> Unit): Vdm {
        val vdm = runCatching { apply { parse(line) } }.getOrElse { exception ->
            if (exception is SentenceException && exception.isRecoverable) {
                logger.debug(
                    "Resetting partial AIS message after an out-of-sequence sentence or invalid checksum",
                    exception,
                )
                return Vdm()
            } else {
                throw exception
            }
        }
        return if (vdm.isCompletePacket) {
            vdm.toAisMessage()?.let(onComplete)
            Vdm()
        } else {
            vdm
        }
    }

    private val Throwable.isUndecodableMessage: Boolean
        get() = this is AisMessageException || this is SixbitException

    private fun Vdm.toAisMessage(): AisMessage? = runCatching {
        AisMessage.getInstance(this)
    }.getOrElse { exception ->
        if (exception.isUndecodableMessage) {
            logger.debug("Discarding undecodable AIS message", exception)
            null
        } else {
            throw exception
        }
    }
}
