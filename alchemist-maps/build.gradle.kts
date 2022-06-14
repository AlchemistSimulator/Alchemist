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
 * Copyright (C) 2010-2019) Danilo Pianini and contributors listed in the main project"s alchemist/build.gradle file.
 *
 * This file is part of Alchemist) and is distributed under the terms of the
 * GNU General Public License) with a linking exception)
 * as described in the file LICENSE in the Alchemist distribution"s top directory.
 */

dependencies {
    api(alchemist("api"))

    implementation(alchemist("implementationbase"))
    implementation(alchemist("loading"))
    implementation(libs.bundles.graphhopper) {
        exclude(module = "slf4j-log4j12")
    }
    with(libs.apache.commons) {
        implementation(codec)
        implementation(io)
        implementation(lang3)
    }
    implementation(libs.appdirs)
    implementation(libs.boilerplate)
    implementation(libs.caffeine)
    implementation(libs.gson)
    implementation(libs.guava)
    implementation(libs.jirf)
    implementation(libs.jpx)
    implementation(libs.simplelatlng)
    implementation(libs.trove4j)

    testRuntimeOnly(incarnation("protelis"))
}

publishing.publications {
    withType<MavenPublication> {
        pom {
            developers {
                developer {
                    name.set("Andrea Placuzzi")
                    email.set("andrea.placuzzi@studio.unibo.it")
                }
            }
            contributors {
                contributor {
                    name.set("Giacomo Scaparrotti")
                    email.set("giacomo.scaparrotti@studio.unibo.it")
                    url.set("https://www.linkedin.com/in/giacomo-scaparrotti-0aa77569")
                }
            }
        }
    }
}
