/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.maps.haveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.beOfType
import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.boundary.launchers.DefaultLauncher
import org.kaikikm.threadresloader.ResourceLoader

class RegressionTestOnRealCases : FreeSpec(
    {
        "convoluted variable loading should work" {
            val loader = LoadAlchemist.from(ResourceLoader.getResource("synthetic/convoluted_variables.yml"))
            loader.getDefault<Nothing, Nothing>() shouldNotBe null
            loader.variables should haveSize(3)
            loader.variables.keys shouldContain "algorithm"
            loader.variables.getValue("algorithm").stream().count().toInt() shouldBeExactly 4 * 7 + 1
        }
        "the default loader should be configurable with two parameters" {
            val loader = LoadAlchemist.from(ResourceLoader.getResource("synthetic/launcherWithoutAutostart.yml"))
            loader.getDefault<Nothing, Nothing>() shouldNotBe null
            loader.launcher should beOfType<DefaultLauncher>()
            (loader.launcher as DefaultLauncher).autoStart shouldBe false
        }
    },
)
