/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

dependencies {
    api(project(":alchemist-interfaces"))
    implementation(project(":alchemist-euclidean-geometry"))
    implementation(project(":alchemist-implementationbase"))
    implementation(project(":alchemist-cognitive-agents"))
    // implementation(Libs.konf)
    implementation(Libs.jgrapht_core)
    testImplementation(project(":alchemist-test"))
    testImplementation(project(":alchemist-incarnation-protelis"))
}

publishing.publications {
    withType<MavenPublication> {
        pom {
            developers {
                developer {
                    name.set("Diego Mazzieri")
                    email.set("diego.mazzieri@studio.unibo.it")
                }
                developer {
                    name.set("Lorenzo Paganelli")
                    email.set("lorenzo.paganelli3@studio.unibo.it")
                }
            }
        }
    }
}
