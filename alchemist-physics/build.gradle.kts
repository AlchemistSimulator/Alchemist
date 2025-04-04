/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
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
    api(alchemist("euclidean-geometry"))
    implementation(alchemist("implementationbase"))
    implementation(libs.caffeine)
    implementation(libs.jgrapht.core)
    implementation(libs.rtree)
    testImplementation(alchemist("test"))
    testImplementation(alchemist("incarnation-protelis"))
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
