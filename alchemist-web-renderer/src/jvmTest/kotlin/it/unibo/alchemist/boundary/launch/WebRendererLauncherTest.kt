/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.AlchemistExecutionOptions
import it.unibo.alchemist.boundary.launch.Validation
import it.unibo.alchemist.boundary.launch.WebRendererLauncher

class WebRendererLauncherTest : StringSpec(
    {

        val expectedLauncherName = "Web Renderer Launcher"

        fun checkOptionsAreInvalid(options: AlchemistExecutionOptions, reason: String) {
            WebRendererLauncher.validate(options) shouldBe Validation.Invalid(reason)
        }

        fun checkOptionsAreValid(options: AlchemistExecutionOptions) {
            WebRendererLauncher.validate(options) shouldBe Validation.OK()
        }

        "WebRendererLauncher should have the correct name" {
            WebRendererLauncher.name shouldBe expectedLauncherName
        }

        "Web Renderer Launcher is not compatible with distributed execution" {
            checkOptionsAreInvalid(
                AlchemistExecutionOptions(configuration = "placeholder", distributed = "something"),
                "$expectedLauncherName is not compatible with distributed execution",
            )
        }

        "Web Renderer Launcher is not compatible with graphics mode" {
            checkOptionsAreInvalid(
                AlchemistExecutionOptions(configuration = "placeholder", graphics = "something"),
                "$expectedLauncherName is not compatible with graphics mode",
            )
            checkOptionsAreInvalid(
                AlchemistExecutionOptions(configuration = "placeholder", fxui = true),
                "$expectedLauncherName is not compatible with graphics mode",
            )
            checkOptionsAreInvalid(
                AlchemistExecutionOptions(configuration = "placeholder", graphics = "something", fxui = true),
                "$expectedLauncherName is not compatible with graphics mode",
            )
        }

        "Web Renderer Launcher is not compatible with grid execution" {
            checkOptionsAreInvalid(
                AlchemistExecutionOptions(configuration = "placeholder", server = "something"),
                "$expectedLauncherName is not compatible with Alchemist grid computing server mode",
            )
        }

        "Web Renderer Launcher is not compatible with headless mode" {
            checkOptionsAreInvalid(
                AlchemistExecutionOptions(configuration = "placeholder", headless = true),
                "$expectedLauncherName is not compatible with headless mode",
            )
        }

        "Web Renderer Launcheris not compatible with variable exploration mode" {
            checkOptionsAreInvalid(
                AlchemistExecutionOptions(configuration = "placeholder", variables = listOf("something")),
                "$expectedLauncherName is not compatible with variable exploration mode",
            )
        }

        "Web Renderer Launcher can contain only configuration" {
            checkOptionsAreValid(AlchemistExecutionOptions(configuration = "placeholder"))
        }
    },
)
