/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

import Libs.alchemist
import Libs.incarnation

plugins {
    id("kotlin-jvm-convention")
}

dependencies {
    api(libs.ignite.core)
    implementation(rootProject)
    implementation(alchemist("api"))
    implementation(alchemist("loading"))
    implementation(alchemist("implementationbase"))
    implementation(alchemist("engine"))
    implementation(libs.apache.commons.io)
    implementation(libs.guava)
    implementation(libs.ignite.spring)
    implementation(libs.ignite.indexing)
    testImplementation(incarnation("sapere"))
}

publishing.publications {
    withType<MavenPublication> {
        pom {
            developers {
                developer {
                    name.set("Matteo Magnani")
                    email.set("matteo.magnani18@studio.unibo.it")
                }
            }
        }
    }
}
