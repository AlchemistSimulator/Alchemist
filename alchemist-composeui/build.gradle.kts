/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

import Util.withWasm
import de.aaschmid.gradle.plugins.cpd.Cpd
import io.gitlab.arturbosch.detekt.Detekt
import java.io.File.separator

plugins {
    id("kotlin-multiplatform-convention")
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    withWasm("alchemist-composeui")
    js {
        moduleName = "alchemist-composeui"
        browser {
            commonWebpackConfig {
                outputFileName = "alchemist-composeui.js"
            }
        }
        binaries.executable()
        useEsModules()
    }

    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }

        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.androidx.lifecycle.runtime.compose)
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(compose.html.core)
            }
        }
    }
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom {
        developers {
            developer {
                name.set("Tommaso Bailetti")
                email.set("tommaso.bailetti@studio.unibo.it")
            }
        }
    }
}

tasks {
    fun PatternFilterable.excludeGenerated() = exclude { "build${separator}generated" in it.file.absolutePath }
    withType<Detekt>().configureEach { excludeGenerated() }
    ktlint { filter { excludeGenerated() } }
    withType<Cpd> {
        dependsOn(generateComposeResClass, generateExpectResourceCollectorsForCommonMain)
        listOf("Jvm", "WasmJs", "Js").forEach { target ->
            dependsOn(named("generateActualResourceCollectorsFor${target}Main"))
        }
    }
}
