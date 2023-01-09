/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.common.renderer

import com.soywiz.korim.bitmap.Bitmap32
import com.soywiz.korim.color.RgbaArray
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.IntArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

/**
 * Serializer for [Bitmap32] class.
 */
object Bitmap32Serializer : KSerializer<Bitmap32> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Bitmap32") {
        element<IntArray>("ints")
        element<Int>("width")
        element<Int>("height")
    }

    override fun serialize(encoder: Encoder, value: Bitmap32) =
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, IntArraySerializer(), value.ints)
            encodeIntElement(descriptor, 1, value.width)
            encodeIntElement(descriptor, 2, value.height)
        }

    override fun deserialize(decoder: Decoder): Bitmap32 =
        decoder.decodeStructure(descriptor) {
            var ints = intArrayOf()
            var width = -1
            var height = -1
            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> ints = decodeSerializableElement(descriptor, 0, IntArraySerializer())
                    1 -> width = decodeIntElement(descriptor, 1)
                    2 -> height = decodeIntElement(descriptor, 2)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }
            Bitmap32(width, height, RgbaArray(ints))
        }
}
