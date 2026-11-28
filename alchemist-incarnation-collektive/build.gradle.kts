/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

import Libs.alchemist

plugins {
    id("kotlin-jvm-convention")
    alias(libs.plugins.collektive)
}

dependencies {
    api(alchemist("implementationbase"))
    api(libs.bundles.collektive)
    implementation(kotlin("compiler-embeddable"))
    implementation(libs.caffeine)
    implementation(libs.kotlin.reflect)
    implementation(libs.collektive.compiler.embeddable) {
        // Test-only deps leaked into the published POM: drop them until fixed upstream
        exclude(group = "com.perfma.wrapped") // JDK 8 tools.jar, unneeded on JDK 9+
        exclude(group = "io.kotest")
    }
    // Kotlin JSR-223 engine, used to evaluate the properties passed to readConcentration
    runtimeOnly(kotlin("scripting-jsr223"))

    testImplementation(alchemist("euclidean-geometry"))
}

publishing.publications {
    withType<MavenPublication> {
        pom {
            contributors {
                contributor {
                    name.set("Nicolas Farabegoli")
                    email.set("nicolas.farabegoli@unibo.it")
                }
                contributor {
                    name.set("Angela Cortecchia")
                    email.set("angela.cortecchia@unibo.it")
                }
            }
        }
    }
}
