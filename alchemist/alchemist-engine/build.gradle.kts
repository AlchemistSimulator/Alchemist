/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project"s alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution"s top directory.
 */

dependencies {
    api(project(":alchemist-interfaces"))
    implementation(Libs.trove4j)
    implementation(Libs.boilerplate)
    implementation(Libs.jgrapht_core)

    testImplementation(project(":alchemist-implementationbase"))
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
