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
    `java-library`
    scala
    alias(libs.plugins.spotless)
}

dependencies {
    compileOnly(libs.spotbugs.annotations)

    api(libs.scafi.core)

    implementation(alchemist("api"))
    implementation(alchemist("euclidean-geometry"))
    implementation(alchemist("implementationbase"))
    implementation(alchemist("physics"))
    implementation(libs.resourceloader)
    implementation(libs.scala3.library)
    implementation(libs.guava)
    implementation(libs.slf4j)

    runtimeOnly(libs.scala3.repl)

    testCompileOnly(libs.spotbugs.annotations)
    testImplementation(alchemist("engine"))
    testImplementation(alchemist("loading"))
    testImplementation(libs.bundles.scalatest)
}

/*
 * Scala sources are formatted by Scalafmt through Spotless, with the version and rules of the root .scalafmt.conf.
 */
val scalafmtConfiguration = rootProject.file(".scalafmt.conf")
val scalafmtVersion = scalafmtConfiguration.readLines()
    .map { it.trim() }
    .single { it.startsWith("version") }
    .substringAfter("=")
    .trim()

spotless {
    scala {
        target("src/**/*.scala")
        scalafmt(scalafmtVersion).configFile(scalafmtConfiguration)
    }
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
    withType<ScalaCompile>().configureEach {
        targetCompatibility = multiJvm.jvmVersionForCompilation.get().toString()
    }

    withType<Test>().configureEach {
        reports {
            html.required.set(false)
        }
    }

    test.configure {
        useJUnitPlatform {
            includeEngines("scalatest")
            testLogging {
                events("passed", "skipped", "failed")
            }
        }
    }
}
