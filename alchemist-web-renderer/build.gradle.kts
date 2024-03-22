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
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotest.multiplatform)
}

kotlin {
    jvm {
        withJava()
    }
    js(IR) {
        browser {
            binaries.executable()
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                compileOnly(libs.spotbugs.annotations)
                implementation(libs.korim)
                implementation(libs.kotlin.stdlib)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.core)
                implementation(libs.redux.kotlin.threadsafe)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotest.framework.engine)
                implementation(libs.kotest.framework.datatest)
                implementation(libs.kotlin.test.common)
                implementation(libs.kotlin.test.annotations)
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
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotest.runner)
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
                implementation(npm("react-bootstrap", "2.5.0"))
            }
        }
    }

    targets.all {
        compilations.configureEach {
            // Workaround for w: duplicate library name: org.jetbrains.kotlin:kotlinx-atomicfu-runtime
            if (defaultSourceSet.name != "jsTest") {
                kotlinOptions {
                    allWarningsAsErrors = true
                }
            }
        }
    }
}

publishing.publications {
    withType<MavenPublication> {
        pom {
            contributors {
                contributor {
                    name.set("Angelo Filaseta")
                    email.set("angelo.filaseta@studio.unibo.it")
                }
            }
        }
    }
}
