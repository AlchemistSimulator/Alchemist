/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.common.renderer

import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korim.bitmap.Bitmap32
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.common.model.serialization.jsonFormat
import it.unibo.alchemist.common.model.surrogate.EmptyConcentrationSurrogate
import it.unibo.alchemist.common.model.surrogate.EnvironmentSurrogate
import it.unibo.alchemist.common.model.surrogate.MoleculeSurrogate
import it.unibo.alchemist.common.model.surrogate.NodeSurrogate
import it.unibo.alchemist.common.model.surrogate.Position2DSurrogate
import it.unibo.alchemist.common.model.surrogate.PositionSurrogate

class BitmapRendererTest : StringSpec({

    val envSurrogate: EnvironmentSurrogate<Any, PositionSurrogate> = EnvironmentSurrogate(
        2,
        listOf(
            NodeSurrogate(
                0,
                mapOf(MoleculeSurrogate("concentration") to EmptyConcentrationSurrogate),
                Position2DSurrogate(5.6, 8.42)
            )
        )
    )

    val renderer: Renderer<Any, PositionSurrogate, Bitmap> = BitmapRenderer()

    "BitmapRenderer should output a Bitmap correctly" {
        val bmp = renderer.render(envSurrogate)
        (bmp is Bitmap32) shouldBe true
        val ser = jsonFormat.encodeToString(Bitmap32Serializer, bmp.toBMP32IfRequired())
        val des = jsonFormat.decodeFromString(Bitmap32Serializer, ser)
        bmp.height shouldBe des.height
        bmp.width shouldBe des.width
        bmp.toBMP32().ints shouldBe des.ints
        bmp shouldBe des
    }

    "BitmapRenderer can't work with Environments with != 2 dimensions" {
        val mockEnv: EnvironmentSurrogate<Any, PositionSurrogate> = EnvironmentSurrogate(-1, listOf())
        shouldThrow<IllegalArgumentException> {
            renderer.render(mockEnv)
        }
    }
})
