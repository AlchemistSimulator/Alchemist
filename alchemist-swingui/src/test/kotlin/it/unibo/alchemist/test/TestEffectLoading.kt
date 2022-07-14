/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

@file:Suppress("DEPRECATION")

package it.unibo.alchemist.test

import io.kotest.core.spec.style.StringSpec
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.file.shouldBeAFile
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.file.shouldNotBeEmpty
import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import it.unibo.alchemist.boundary.swingui.effect.impl.DrawBidimensionalGaussianLayersGradient
import it.unibo.alchemist.boundary.swingui.effect.impl.EffectSerializationFactory
import org.kaikikm.threadresloader.ResourceLoader
import java.io.File

class TestEffectLoading : StringSpec(
    {
        "effects with layers should be (de)serializable" {
            val target =
                DrawBidimensionalGaussianLayersGradient()
            val tempFile = tempfile()
            EffectSerializationFactory.effectToFile(tempFile, target)
            println(tempFile.readText())
            tempFile.shouldExist()
            tempFile.shouldBeAFile()
            tempFile.shouldNotBeEmpty()
            EffectSerializationFactory.effectsFromFile(tempFile).shouldNotBeNull()
        }
        "legacy effects with layers should be deserializable" {
            val target = ResourceLoader.getResource("layer.json")
            target shouldNot beNull()
            val file = File(target.file)
            file.shouldExist()
            file.shouldNotBeEmpty()
            file.shouldBeAFile()
            val effects = EffectSerializationFactory.effectsFromFile(file)
            effects.shouldNotBeNull()
            effects.size shouldBe 4
        }
    }
)
