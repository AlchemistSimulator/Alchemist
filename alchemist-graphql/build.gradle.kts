/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

import Libs.alchemist
import com.expediagroup.graphql.plugin.gradle.tasks.GraphQLGenerateSDLTask
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    application
    alias(libs.plugins.kotest.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor)
    alias(libs.plugins.graphql.server)
}

dependencies {
    implementation("io.ktor:ktor-server-cors-jvm:2.3.3")
    implementation("io.ktor:ktor-server-core-jvm:2.3.3")
    implementation("io.ktor:ktor-server-websockets-jvm:2.3.4")
}

kotlin {
    jvm {
        withJava()

        // workaround for fixing "task compileKotlin not found", will be fixed in GraphQL Kotlin v.7
        tasks.maybeCreate("compileKotlin").dependsOn(tasks.named("compileKotlinJvm"))
        tasks.named<GraphQLGenerateSDLTask>("graphqlGenerateSDL") {
            val srcSet = sourceSets.getByName("jvmMain").kotlin
            source = srcSet.asFileTree
            projectClasspath.setFrom(srcSet)
        }
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
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.kotest.assertions)
                implementation(libs.kotest.runner)
                implementation(libs.ktor.server.test.host)
                implementation(alchemist("euclidean-geometry"))
                implementation(alchemist("implementationbase"))
                implementation(alchemist("test"))
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(libs.apollo.runtime)
                implementation(libs.kotlin.coroutines.core)
            }
        }
        val jsTest by getting
    }
}

application {
    mainClass.set("it.unibo.alchemist.Alchemist")
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
