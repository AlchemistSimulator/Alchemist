/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
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
    ksp(alchemist("factories-generator"))

    api(alchemist("api"))
    api(alchemist("implementationbase"))

    implementation(alchemist("engine"))
    implementation(alchemist("euclidean-geometry"))
    implementation(kotlin("reflect"))
    implementation(kotlin("script-runtime"))
    implementation(kotlin("scripting-common"))
    implementation(kotlin("scripting-jvm"))
    implementation(kotlin("scripting-jvm-host"))
    implementation(libs.apache.commons.lang3)
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
    testImplementation(alchemist("kotlinscript"))
    testImplementation(alchemist("maps"))
    testImplementation(alchemist("test"))
    testImplementation(incarnation("sapere"))
    testImplementation(incarnation("protelis"))
    testImplementation(libs.appdirs)
    testImplementation(libs.caffeine)
    testImplementation(libs.embedmongo)
}

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/test/kotlin")
    }
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
            "-Xcontext-parameters",
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
