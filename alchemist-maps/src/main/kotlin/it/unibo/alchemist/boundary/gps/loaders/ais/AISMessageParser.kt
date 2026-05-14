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
import org.slf4j.LoggerFactory

/**
 * Parser for AIS NMEA VDM sentences.
 *
 * The AIS library already handles multipart messages using the sequence information
 * embedded in the sentence.
 */
object AISMessageParser {
    /**
     * Parses a line of a raw message into an AIS sentence.
     */
    fun parseLine(vdm: Vdm, message: String): Vdm = runCatching {
        vdm.apply { parse(message) }
    }.recoverCatching { exception ->
        if (exception is SentenceException) {
            when {
                exception.message?.contains("Out of sequence sentence:") == true -> {
                    logger.debug("Resetting partial AIS message after an out-of-sequence sentence", exception)
                    Vdm()
                }
                exception.message?.contains("Invalid checksum") == true -> {
                    logger.debug("Resetting partial AIS message after an invalid checksum", exception)
                    Vdm()
                }
                else -> throw exception
            }
        } else {
            throw exception
        }
    }.getOrThrow()

    /**
     * @return the [AisMessage] from the read sentence.
     */
    fun build(vdm: Vdm): AisMessage? = runCatching {
        AisMessage.getInstance(vdm)
    }.recoverCatching { exception ->
        when (exception) {
            is AisMessageException, is SixbitException -> {
                logger.debug("Discarding undecodable AIS message", exception)
                null
            }
            else -> throw exception
        }
    }.getOrThrow()

    private val logger = LoggerFactory.getLogger(AISMessageParser::class.java)
}
