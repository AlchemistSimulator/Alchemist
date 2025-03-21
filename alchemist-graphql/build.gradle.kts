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
import Util.allVerificationTasks
import com.apollographql.apollo3.gradle.internal.ApolloGenerateSourcesTask
import com.expediagroup.graphql.plugin.gradle.tasks.AbstractGenerateClientTask

plugins {
    id("kotlin-multiplatform-convention")
    alias(libs.plugins.kotest.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.graphql.server)
    alias(libs.plugins.graphql.client)
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.apollo.runtime)
                implementation(libs.kotlin.coroutines.core)
                implementation(libs.kotlin.stdlib)
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
                api(alchemist("graphql-surrogates"))
                implementation(rootProject)
                implementation(alchemist("implementationbase"))
                implementation(libs.graphql.client)
                implementation(libs.ktor.server.websockets)
                implementation(libs.bundles.graphql.server)
                implementation(libs.bundles.ktor.server)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotest.runner)
                implementation(libs.ktor.server.test.host)
                implementation(incarnation("sapere"))
                implementation(alchemist("euclidean-geometry"))
                implementation(alchemist("implementationbase"))
                implementation(alchemist("test"))
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(libs.kotlinx.atomicfu.runtime)
            }
        }
    }
}

graphql {
    schema {
        packages =
            listOf(
                "it.unibo.alchemist.boundary.graphql",
            )
    }
}

tasks.withType<AbstractGenerateClientTask>().configureEach {
    val graphQLGenerateSDL =
        project(":${project.name}-surrogates")
            .tasks
            .named("graphqlGenerateSDL")
    dependsOn(graphQLGenerateSDL)
    schemaFile.convention(
        graphQLGenerateSDL
            .map { it.property("schemaFile") as RegularFileProperty }
            .map { it.get() },
    )
    packageName.set("it.unibo.alchemist.boundary.graphql.client.generated")
    kotlin {
        sourceSets {
            queryFileDirectory.set(file("src/commonMain/resources/graphql"))
        }
    }
}

val surrogates = project(":${project.name}-surrogates")

/**
 * Configure the Apollo Gradle plugin to generate Kotlin models
 * from the GraphQL schema inside the `commonMain` sourceSet.
 */
apollo {
    service(name) {
        generateKotlinModels.set(true)
        generateSourcesDuringGradleSync.set(true)
        packageName.set("it.unibo.alchemist.boundary.graphql.client")
        schemaFiles.from(surrogates.layout.buildDirectory.file("schema.graphql"))
        srcDir("src/commonMain/resources/graphql")
        outputDirConnection {
            connectToKotlinSourceSet("commonMain")
        }
    }
}

val apolloGenerationTasks = tasks.withType<ApolloGenerateSourcesTask>()
apolloGenerationTasks.configureEach {
    dependsOn(surrogates.tasks.named("graphqlGenerateSDL"))
}

tasks.allVerificationTasks.configureEach { dependsOn(apolloGenerationTasks) }

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
