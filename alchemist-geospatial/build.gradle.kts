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
}

dependencies {
    api(alchemist("api"))
    implementation(libs.cdm.core)
    runtimeOnly(libs.cdm.grib)
    implementation(libs.gson)
    implementation(libs.guava)
    implementation(libs.mockk)
    implementation(libs.slf4j)
    testImplementation(alchemist("test"))
}

publishing.publications {
    withType<MavenPublication> {
        pom {
            contributors {
                contributor {
                    name.set("Emir Wanes Aouioua")
                    email.set("emirwanes.aouioua@studio.unibo.it")
                }
            }
        }
    }
}
