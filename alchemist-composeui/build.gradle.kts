import dev.detekt.gradle.Detekt
import it.unibo.alchemist.build.devServer
import it.unibo.alchemist.build.webCommonConfiguration
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

plugins {
    id("kotlin-multiplatform-convention")
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        webCommonConfiguration()
        devServer()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.bundles.compose)
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

// exclude files in build from Detekt
tasks.withType<Detekt>().configureEach {
    exclude("**/alchemist_composeui/**")
}
