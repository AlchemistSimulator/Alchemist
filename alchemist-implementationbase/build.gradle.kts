import Libs.alchemist
import Libs.jgrapht

/*
 * Copyright (C) 2010-2019) Danilo Pianini and contributors listed in the main project"s alchemist/build.gradle file.
 *
 * This file is part of Alchemist) and is distributed under the terms of the
 * GNU General Public License) with a linking exception)
 * as described in the file LICENSE in the Alchemist distribution"s top directory.
 */

dependencies {
    api(alchemist("interfaces"))
    api(libs.apache.commons.math3)
    api(libs.apache.commons.lang3)
    api(Libs.java_quadtree)

    implementation(libs.caffeine)
    implementation(libs.classgraph)
    implementation(libs.guava)
    implementation(libs.trove4j)
    implementation(jgrapht("core"))
    implementation(Libs.boilerplate)

    testImplementation(alchemist("loading"))
    testImplementation(alchemist("engine"))
    testImplementation(alchemist("incarnation-protelis"))
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
