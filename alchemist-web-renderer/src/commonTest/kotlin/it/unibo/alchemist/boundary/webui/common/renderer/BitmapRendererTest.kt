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
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import it.unibo.alchemist.boundary.webui.common.model.serialization.jsonFormat
import it.unibo.alchemist.boundary.webui.common.model.surrogate.EmptyConcentrationSurrogate
import it.unibo.alchemist.boundary.webui.common.model.surrogate.EnvironmentSurrogate
import it.unibo.alchemist.boundary.webui.common.model.surrogate.MoleculeSurrogate
import it.unibo.alchemist.boundary.webui.common.model.surrogate.NodeSurrogate
import it.unibo.alchemist.boundary.webui.common.model.surrogate.Position2DSurrogate
import it.unibo.alchemist.boundary.webui.common.model.surrogate.PositionSurrogate

class BitmapRendererTest : StringSpec({

    val envSurrogate: EnvironmentSurrogate<Any, PositionSurrogate> = EnvironmentSurrogate(
        2,
        listOf(
            NodeSurrogate(
                0,
                mapOf(MoleculeSurrogate("concentration") to EmptyConcentrationSurrogate),
                Position2DSurrogate(5.6, 8.42),
            ),
        ),
    )

    val renderer: Renderer<Any, PositionSurrogate, Bitmap> = BitmapRenderer()

    "BitmapRenderer should output a Bitmap correctly" {
        val bmp = renderer.render(envSurrogate)
        bmp.shouldBeInstanceOf<Bitmap32>()
        val encoded = jsonFormat.encodeToString(Bitmap32Serializer, bmp.toBMP32IfRequired())
        val decoded = jsonFormat.decodeFromString(Bitmap32Serializer, encoded)
        bmp.height shouldBe decoded.height
        bmp.width shouldBe decoded.width
        bmp.toBMP32().ints shouldBe decoded.ints
    }

    "BitmapRenderer can't work with Environments with != 2 dimensions" {
        val mockEnv: EnvironmentSurrogate<Any, PositionSurrogate> = EnvironmentSurrogate(-1, listOf())
        shouldThrow<IllegalArgumentException> {
            renderer.render(mockEnv)
        }
    }
})
