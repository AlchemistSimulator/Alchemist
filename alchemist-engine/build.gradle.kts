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

/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project"s alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution"s top directory.
 */

dependencies {
    api(alchemist("api"))

    implementation(alchemist("maintenance-tooling"))
    implementation(libs.jgrapht.core)
    implementation(libs.guava)
    implementation(libs.trove4j)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")

    testImplementation(alchemist("euclidean-geometry"))
    testImplementation(alchemist("implementationbase"))
    testImplementation(alchemist("loading"))
    testImplementation(incarnation("biochemistry"))
}

publishing.publications {
    withType<MavenPublication> {
        pom {
            contributors {
                contributor {
                    name.set("Andrea Placuzzi")
                    email.set("andrea.placuzzi@studio.unibo.it")
                }
                contributor {
                    name.set("Franco Pradelli")
                    email.set("franco.pradelli@studio.unibo.it")
                }
                contributor {
                    name.set("Giacomo Scaparrotti")
                    email.set("giacomo.scaparrotti@studio.unibo.it")
                    url.set("https://www.linkedin.com/in/giacomo-scaparrotti-0aa77569")
                }
            }
        }
    }
}

tasks.compileTestKotlin {
    kotlinOptions {
        freeCompilerArgs = listOf(
            "-Xjvm-default=all", // Enable default methods in Kt interfaces
            // Context receivers are being used when testing
            "-Xcontext-receivers", // Enable context receivers
        )
    }
}
