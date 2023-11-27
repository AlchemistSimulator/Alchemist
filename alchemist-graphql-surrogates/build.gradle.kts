/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

import Libs.alchemist
import com.expediagroup.graphql.plugin.gradle.tasks.AbstractGenerateClientTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.graphql.server)
}

dependencies {
    api(alchemist("api"))
    implementation(libs.kotlin.stdlib)
    implementation(libs.graphql.hooks.provider)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.bundles.graphql.server)
    implementation(alchemist("implementationbase"))
}

tasks {
    graphqlGenerateSDL {
        packages = listOf("monitor", "schema", "operations", "util").map { "it.unibo.alchemist.boundary.graphql.$it" }
    }
    withType<KotlinCompile>().configureEach {
        finalizedBy(graphqlGenerateSDL)
    }
    withType<AbstractGenerateClientTask>().configureEach {
        enabled = false
    }
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
