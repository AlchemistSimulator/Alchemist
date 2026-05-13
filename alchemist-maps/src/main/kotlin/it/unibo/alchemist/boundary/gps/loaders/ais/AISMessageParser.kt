package it.unibo.alchemist.boundary.gps.loaders.ais

import dk.dma.ais.binary.SixbitException
import dk.dma.ais.message.AisMessage
import dk.dma.ais.message.AisMessageException
import dk.dma.ais.sentence.SentenceException
import dk.dma.ais.sentence.Vdm

/**
 * Parser for AIS NMEA VDM sentences.
 *
 * The AIS library already handles multipart messages using the sequence information
 * embedded in the sentence. This builder is more flexible than the library one because it allows to parse messages
 * with any subdivisions, without considering the whole stream of messages incoming.
 */
class AISMessageParser {
    private var vdm = Vdm()

    /**
     * Creates a new instance of the AIS sentence reader.
     */
    fun reset() {
        vdm = Vdm()
    }

    /**
     * Parses a line of a raw message into an AIS sentence.
     */
    fun parseLine(message: String) {
        try {
            vdm.parse(message)
        } catch (exception: SentenceException) {
            when {
                exception.message?.contains("Out of sequence sentence:") == true -> reset()
                exception.message?.contains("Invalid checksum") == true -> reset()
                else -> throw exception
            }
        }
    }

    /**
     * @return true if the AIS sentence is complete.
     */
    fun isComplete(): Boolean = vdm.isCompletePacket

    /**
     * @return the [AisMessage] from the read sentence.
     */
    fun build(): AisMessage? = try {
        AisMessage.getInstance(vdm)
    } catch (exception: AisMessageException) {
        null
    } catch (exception: SixbitException) {
        null
    } finally {
        reset()
    }
}
