/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

import Libs.alchemist
import de.aaschmid.gradle.plugins.cpd.Cpd
import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import java.io.File.separator

plugins {
    id("kotlin-multiplatform-convention") apply false
    kotlin("multiplatform")
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "alchemist-composeui"
        browser {
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "alchemist-composeui.js"
                devServer =
                    (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                        static =
                            (static ?: mutableListOf()).apply {
                                // Serve sources to debug inside browser
                                add(projectDirPath)
                            }
                    }
            }
        }
        binaries.executable()
    }

    jvm {
        withJava()
    }

    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(alchemist("api"))
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
    }
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom {
        contributors {
            contributor {
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
        listOf("Jvm", "WasmJs").forEach { target ->
            dependsOn(named("generateActualResourceCollectorsFor${target}Main"))
        }
    }
}
