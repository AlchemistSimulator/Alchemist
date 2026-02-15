/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

import Libs.alchemist
import it.unibo.alchemist.build.catalog
import it.unibo.alchemist.build.webCommonConfiguration
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode

plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp")
    id("dokka-convention")
    id("io.kotest")
    id("kotlin-static-analysis-convention")
    id("power-assert-convention")
}

@OptIn(ExperimentalWasmDsl::class)
kotlin {
    jvm {
        compilerOptions {
            jvmDefault.set(JvmDefaultMode.ENABLE)
            freeCompilerArgs.add("-Xcontext-parameters") // Enable context receivers
        }
    }

    js { webCommonConfiguration() }

    sourceSets {
        val alchemistApi = alchemist("api")
        val alchemistMaintenanceTooling = alchemist("maintenance-tooling")
        val isNotRootRootProject = project != alchemistMaintenanceTooling && project != alchemistApi
        val commonMain by getting {
            dependencies {
                if (isNotRootRootProject) {
                    implementation(alchemist("maintenance-tooling"))
                }
            }
        }
        val commonTest by getting {
            dependencies {
                val kotlinTest by catalog
                val kotestAssertionsCore by catalog
                val kotestFrameworkEngine by catalog
                implementation(kotlinTest)
                implementation(kotestAssertionsCore)
                implementation(kotestFrameworkEngine)
            }
        }
        val jvmMain by getting {
            dependencies {
                if (isNotRootRootProject) {
                    implementation(alchemistApi)
                }
            }
        }
        val jvmTest by getting {
            dependencies {
                val `kotest-runner` by catalog
                implementation(`kotest-runner`)
            }
        }
    }
}
