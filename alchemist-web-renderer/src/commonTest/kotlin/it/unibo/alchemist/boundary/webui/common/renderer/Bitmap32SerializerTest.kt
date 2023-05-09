/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.common.renderer

import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korim.bitmap.Bitmap32
import com.soywiz.korim.paint.ColorPaint
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import it.unibo.alchemist.boundary.webui.common.model.serialization.jsonFormat

class Bitmap32SerializerTest : StringSpec({
    "Bitmap32 should be serialized and deserialized correctly" {
        val bmp: Bitmap = Bitmap32(1000, 1000, ColorPaint(0))
        val serialized = jsonFormat.encodeToString(Bitmap32Serializer, bmp.toBMP32IfRequired())
        val deserialized = jsonFormat.decodeFromString(Bitmap32Serializer, serialized)
        deserialized.shouldBeInstanceOf<Bitmap32>()
        check(bmp is Bitmap32)
        deserialized.bounds shouldBe bmp.bounds
        deserialized.height shouldBe bmp.height
        deserialized.width shouldBe bmp.width
        deserialized.ints shouldBe bmp.ints
    }
})
