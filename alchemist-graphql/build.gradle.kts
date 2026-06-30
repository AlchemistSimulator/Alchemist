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
import com.expediagroup.graphql.plugin.gradle.tasks.AbstractGenerateClientTask
import com.expediagroup.graphql.plugin.gradle.tasks.GraphQLGenerateSDLTask
import it.unibo.alchemist.build.allVerificationTasks

plugins {
    id("kotlin-multiplatform-convention")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.graphql.server)
    alias(libs.plugins.graphql.client)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.apollo.runtime)
                implementation(libs.kotlin.coroutines.core)
                implementation(libs.kotlin.stdlib)
                implementation(libs.kotlinx.serialization.json)
            }
        }
        commonTest {
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
        packages = listOf("it.unibo.alchemist.boundary.graphql")
    }
}

val surrogates = project(":${project.name}-surrogates")

evaluationDependsOn(surrogates.path)

val graphQLGenerateSDL = surrogates.tasks.named("graphqlGenerateSDL")

tasks.withType<AbstractGenerateClientTask>().configureEach {
    dependsOn(graphQLGenerateSDL)
    schemaFile.convention(surrogates.layout.buildDirectory.file("schema.graphqls"))
    packageName.set("it.unibo.alchemist.boundary.graphql.client.generated")
    kotlin {
        sourceSets {
            queryFileDirectory.set(file("src/commonMain/resources/graphql"))
        }
    }
}

/*
 * Configure the Apollo Gradle plugin to generate Kotlin models
 * from the GraphQL schema inside the `commonMain` sourceSet.
 */
apollo {
    service(name) {
        val graphQLSchemaFile = files(surrogates.layout.buildDirectory.file("schema.graphqls"))
            .builtBy(graphQLGenerateSDL)
        generateKotlinModels.set(true)
        generateSourcesDuringGradleSync.set(true)
        packageName.set("it.unibo.alchemist.boundary.graphql.client")
        schemaFiles.from(graphQLSchemaFile)
        srcDir("src/commonMain/resources/graphql")
        outputDirConnection {
            connectToKotlinSourceSet("commonMain")
        }
    }
}

tasks.allVerificationTasks.configureEach {
    dependsOn(tasks.generateApolloSources)
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
