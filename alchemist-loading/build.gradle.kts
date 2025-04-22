/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
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
plugins {
    id("kotlin-jvm-convention")
}

dependencies {
    api(alchemist("api"))
    api(alchemist("implementationbase"))

    implementation(alchemist("engine"))
    implementation(alchemist("euclidean-geometry"))
    implementation(libs.apache.commons.lang3)
    implementation(libs.arrow.core)
    implementation(libs.dsiutils)
    implementation(libs.graphstream.core)
    implementation(libs.graphstream.algo)
    implementation(libs.gson)
    implementation(libs.guava)
    implementation(libs.jirf)
    implementation(libs.kasechange)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.mongodb)
    implementation(libs.snakeyaml)

    runtimeOnly(libs.groovy.jsr223)
    runtimeOnly(kotlin("scripting-jsr223"))
    runtimeOnly(libs.scala.compiler)

    testImplementation(alchemist("engine"))
    testImplementation(alchemist("maps"))
    testImplementation(libs.appdirs)
    testImplementation(libs.caffeine)
    testImplementation(libs.embedmongo)

    testRuntimeOnly(incarnation("sapere"))
    testRuntimeOnly(incarnation("protelis"))
}

tasks.withType<Test> {
    useJUnitPlatform()
    maxHeapSize = "1500m"
}

tasks.withType<Test>().configureEach {
    // for changing the default JVM args
    jvmArgs("--add-opens=java.base/java.util=ALL-UNNAMED")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.time.ExperimentalTime",
        )
    }
}

publishing.publications {
    withType<MavenPublication> {
        pom {
            contributors {
                contributor {
                    name.set("Matteo Magnani")
                    email.set("matteo.magnani18@studio.unibo.it")
                }
                contributor {
                    name.set("Andrea Placuzzi")
                    email.set("andrea.placuzzi@studio.unibo.it")
                }
                contributor {
                    name.set("Franco Pradelli")
                    email.set("franco.pradelli@studio.unibo.it")
                }
            }
        }
    }
}
