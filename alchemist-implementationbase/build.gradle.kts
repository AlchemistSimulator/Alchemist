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
    api(alchemist("maintenance-tooling"))
    api(libs.apache.commons.math3)
    api(libs.apache.commons.lang3)
    api(libs.quadtree)

    implementation(libs.kotlin.reflect)
    implementation(libs.boilerplate)
    implementation(libs.caffeine)
    implementation(libs.classgraph)
    implementation(libs.guava)
    implementation(libs.jgrapht.core)
    implementation(libs.symmetric.matrix)
    implementation(libs.trove4j)

    testImplementation(alchemist("loading"))
    testImplementation(alchemist("test"))
    testImplementation(alchemist("euclidean-geometry"))
    testImplementation(incarnation("protelis"))
}

publishing.publications {
    withType<MavenPublication> {
        pom {
            developers {
                developer {
                    name.set("Lorenzo Paganelli")
                    email.set("lorenzo.paganelli3@studio.unibo.it")
                }
                developer {
                    name.set("Federico Pettinari")
                    email.set("federico.pettinari2@studio.unibo.it")
                }
            }
            contributors {
                contributor {
                    name.set("Matteo Magnani")
                    email.set("matteo.magnani18@studio.unibo.it")
                }
                contributor {
                    name.set("Diego Mazzieri")
                    email.set("diego.mazzieri@studio.unibo.it")
                }
                contributor {
                    name.set("Franco Pradelli")
                    email.set("franco.pradelli@studio.unibo.it")
                }
            }
        }
    }
}
