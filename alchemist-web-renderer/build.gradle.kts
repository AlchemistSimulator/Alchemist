/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

import Libs.alchemist
import Libs.incarnation

plugins {
    id("kotlin-multiplatform-convention")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotest.multiplatform)
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlin.stdlib)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.korim)
                implementation(libs.ktor.client.core)
                implementation(libs.redux.kotlin.threadsafe)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlin.coroutines.test)
            }
        }
        val jvmMain by getting {
            dependencies {
                api(alchemist("api"))
                implementation(incarnation("sapere"))
                implementation(rootProject)
                implementation(libs.bundles.ktor.server)
                implementation(libs.logback)
                implementation(libs.resourceloader)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.ktor.server.test.host)
                implementation(alchemist("euclidean-geometry"))
                implementation(alchemist("implementationbase"))
                implementation(alchemist("test"))
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(libs.bundles.ktor.client)
                implementation(libs.bundles.kotlin.react)
                implementation(libs.kotlinx.atomicfu.runtime)
                implementation(npm("react-bootstrap", "2.5.0"))
                implementation(libs.kotlin.coroutines.test)
            }
        }
    }
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom {
        contributors {
            contributor {
                name.set("Angelo Filaseta")
                email.set("angelo.filaseta@studio.unibo.it")
            }
        }
    }
}
