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
    api(alchemist("api"))

    implementation(alchemist("euclidean-geometry"))
    implementation(alchemist("implementationbase"))
    implementation(alchemist("physics"))
    implementation(libs.jgrapht.core)
    implementation(libs.konf)
    implementation(libs.kotlin.reflect)

    testImplementation(alchemist("test"))
    testImplementation(incarnation("protelis"))
    testImplementation(libs.apache.commons.collections4)
}

publishing.publications {
    withType<MavenPublication> {
        pom {
            developers {
                developer {
                    name.set("Diego Mazzieri")
                    email.set("diego.mazzieri@studio.unibo.it")
                }
                developer {
                    name.set("Lorenzo Paganelli")
                    email.set("lorenzo.paganelli3@studio.unibo.it")
                }
            }
        }
    }
}
