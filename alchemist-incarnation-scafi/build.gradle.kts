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
    `java-library`
    scala
    alias(libs.plugins.scalafmt)
}

dependencies {
    compileOnly(libs.spotbugs.annotations)

    api(libs.scafi.core)

    implementation(alchemist("api"))
    implementation(alchemist("euclidean-geometry"))
    implementation(alchemist("implementationbase"))
    implementation(alchemist("physics"))
    implementation(libs.resourceloader)
    implementation(libs.bundles.scala)
    implementation(libs.bundles.scalacache)

    testCompileOnly(libs.spotbugs.annotations)
    testImplementation(alchemist("engine"))
    testImplementation(alchemist("loading"))
    testImplementation(libs.bundles.scalatest)
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

tasks {
    withType<ScalaCompile> {
        targetCompatibility = multiJvm.jvmVersionForCompilation.get().toString()
    }

    withType<Test> {
        reports {
            html.required.set(false)
        }
    }

    test {
        useJUnitPlatform {
            includeEngines("scalatest")
            testLogging {
                events("passed", "skipped", "failed")
            }
        }
    }
}
