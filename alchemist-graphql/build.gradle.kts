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
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    application
    alias(libs.plugins.kotest.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor)
    alias(libs.plugins.graphql.server)
    alias(libs.plugins.graphql.client)
}

dependencies {
    implementation(libs.ktor.server.cors.jvm)
    implementation(libs.ktor.server.core.jvm)
    implementation(libs.ktor.server.websockets)
}

kotlin {
    jvm {
        withJava()
        tasks {
            graphql {
                schema {
                    packages = listOf(
                        "it.unibo.alchemist.boundary.graphql",
                    )
                }
            }
            // Disabling GraphQL Kotlin unwanted tasks
            listOf(
                "graphqlGenerateClient",
                "graphqlGenerateTestClient",
            ).map {
                this.getByName(it)
            }.forEach { it.enabled = false }
        }
    }

    js(IR) {
        browser {
            binaries.executable()
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlin.stdlib)
                implementation(libs.apollo.runtime)
                implementation(libs.kotlin.coroutines.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                api(alchemist("api"))
                implementation(rootProject)
                implementation(libs.bundles.graphql.server)
                implementation(libs.bundles.ktor.server)
                implementation(alchemist("implementationbase"))
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.kotest.assertions)
                implementation(libs.kotest.runner)
                implementation(libs.ktor.server.test.host)
                implementation(incarnation("sapere"))
                implementation(alchemist("euclidean-geometry"))
                implementation(alchemist("implementationbase"))
                implementation(alchemist("test"))
            }
        }
        val jsMain by getting
        val jsTest by getting
    }
}

application {
    mainClass.set("it.unibo.alchemist.Alchemist")
}

/**
 * Configure the Apollo Gradle plugin to generate Kotlin models
 * from the GraphQL schema inside the `commonMain` sourceSet.
 */
apollo {
    service("alchemist") {
        generateKotlinModels.set(true)
        packageName.set("it.unibo.alchemist.boundary.graphql.client")
        schemaFiles.from(file("src/commonMain/resources/graphql/schema.graphqls"))
        srcDir("src/commonMain/resources/graphql")
        outputDirConnection {
            connectToKotlinSourceSet("commonMain")
        }
    }
}

fun isGeneratedFile(file: FileTreeElement): Boolean = file.file.absolutePath.contains("generated" + File.separator)

ktlint {
    filter {
        exclude(::isGeneratedFile)
    }
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>() {
    exclude(::isGeneratedFile)
}

/**
 * Webpack task that generates the JS artifacts.
 */
val webpackTask = tasks.named("jsBrowserProductionWebpack")

tasks.named("run", JavaExec::class).configure {
    classpath(
        tasks.named("compileKotlinJvm"),
        configurations.named("jvmRuntimeClasspath"),
        webpackTask.map { task ->
            task.outputs.files.map { file ->
                file.parent
            }
        },
    )
}

/**
 * Configure GraphQL Schema download task to download the schema from the server
 * at the default endpoint, locating the updated schema under the correct directory.
 */
tasks.withType<com.expediagroup.graphql.plugin.gradle.tasks.GraphQLDownloadSDLTask>().configureEach {
    outputFile.set(File("src/commonMain/resources/graphql/schema.graphqls"))
    endpoint = "http://localhost:8081/sdl"
}

/**
 * Configure the [ShadowJar] task to work exactly like the "jvmJar" task of Kotlin Multiplatform, but also
 * include the JS artifacts by depending on the "jsBrowserProductionWebpack" task.
 */
tasks.withType<ShadowJar>().configureEach {
    val jvmJarTask = tasks.named("jvmJar")
    from(webpackTask)
    from(jvmJarTask)
    from(tasks.named("jsBrowserDistribution"))
    mustRunAfter(tasks.distTar, tasks.distZip)
    archiveClassifier.set("all")
}

publishing.publications {
    withType<MavenPublication> {
        pom {
            contributors {
                contributor {
                    name.set("Stefano Furi")
                    email.set("stefano.furi@studio.unibo.it")
                }
            }
        }
    }
}
