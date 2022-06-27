/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

import Libs.alchemist

/*
 * Copyright (C) 2010-2019) Danilo Pianini and contributors listed in the main project"s alchemist/build.gradle file.
 *
 * This file is part of Alchemist) and is distributed under the terms of the
 * GNU General Public License) with a linking exception)
 * as described in the file LICENSE in the Alchemist distribution"s top directory.
 */
plugins {
    scala
    alias(libs.plugins.scalatest)
    alias(libs.plugins.scalafmt)
}

dependencies {
    api(alchemist("api"))
    api(libs.scafi.core)

    implementation(alchemist("implementationbase"))
    implementation(alchemist("euclidean-geometry"))
    implementation(libs.bundles.scala)
    implementation(libs.bundles.scalacache)

    testImplementation(alchemist("engine"))
    testImplementation(alchemist("loading"))
    testImplementation(libs.bundles.scalatest)
}

tasks.withType<ScalaCompile> {
    targetCompatibility = "1.8"
}

publishing.publications {
    withType<MavenPublication> {
        pom {
            developers {
                developer {
                    name.set("Roberto Casadei")
                    email.set("roby.casadei@unibo.it")
                    url.set("https://www.unibo.it/sitoweb/roby.casadei")
                }
            }
            contributors {
                contributor {
                    name.set("Gianluca Aguzzi")
                    email.set("gianluca.aguzzi@unibo.it")
                }
            }
        }
    }
}

tasks.withType<Test> {
    reports {
        html.required.set(false)
    }
}
