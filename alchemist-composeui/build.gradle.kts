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
import it.unibo.alchemist.build.devServer
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("kotlin-multiplatform-convention")
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        devServer()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.bundles.compose)
                implementation(libs.kotlinx.collections.immutable)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.coroutines.test)
            }
        }
        jvmMain {
            dependencies {
                implementation(alchemist("euclidean-geometry"))
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlin.coroutines.swing)
                implementation(libs.noncrypto.hash)
                implementation(libs.slf4j)
            }
        }
        jvmTest {
            dependencies {
                implementation(libs.compose.ui.test)
                implementation(alchemist("loading"))
                implementation(alchemist("test"))
                implementation(incarnation("protelis"))
                implementation(incarnation("sapere"))
                implementation(libs.snakeyaml)
            }
        }
    }
}

compose.resources {
    packageOfResClass = "it.unibo.alchemist.boundary.composeui.resources"
}

val renderSnapshots = tasks.register<JavaExec>("renderSnapshots") {
    group = "verification"
    description = "Renders the UI offscreen on the scenarios in src/jvmTest/resources/snapshots"
    val testCompilation = kotlin.jvm().compilations.getByName("test")
    val scenarios = layout.projectDirectory.dir("src/jvmTest/resources/snapshots")
    val snapshots = layout.buildDirectory.dir("ui-snapshots")
    classpath(listOfNotNull(testCompilation.output.allOutputs, testCompilation.runtimeDependencyFiles))
    mainClass = "it.unibo.alchemist.boundary.composeui.snapshots.SnapshotRendererKt"
    systemProperty("java.awt.headless", "true")
    inputs.dir(scenarios)
    outputs.dir(snapshots)
    argumentProviders += CommandLineArgumentProvider {
        listOf(scenarios.asFile.absolutePath, snapshots.get().asFile.absolutePath)
    }
}

val runScenario = tasks.register<JavaExec>("runScenario") {
    group = "application"
    description = "Opens the desktop UI on -Pscenario=<file>, relative to the repository root; " +
        "without it, on the gradient scenario of the tests"
    val testCompilation = kotlin.jvm().compilations.getByName("test")
    val repository = rootProject.layout.projectDirectory
    val scenario = providers.gradleProperty("scenario")
        .map { repository.file(it).asFile }
        .orElse(layout.projectDirectory.file("src/jvmTest/resources/composeui-gradient.yml").asFile)
    classpath(listOfNotNull(testCompilation.output.allOutputs, testCompilation.runtimeDependencyFiles))
    mainClass = "it.unibo.alchemist.boundary.composeui.live.LiveRunKt"
    argumentProviders += CommandLineArgumentProvider { listOf(scenario.get().absolutePath) }
}

// Rendering real scenarios also catches crashes that unit tests miss; it reruns only when code or scenarios change.
tasks.check { dependsOn(renderSnapshots) }

publishing.publications.withType<MavenPublication>().configureEach {
    pom {
        developers {
            developer {
                name.set("Tommaso Bailetti")
                email.set("tommaso.bailetti@studio.unibo.it")
            }
        }
    }
}
