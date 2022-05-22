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

dependencies {
    api(alchemist("api"))
    implementation(rootProject)
    implementation(alchemist("engine"))
    implementation(alchemist("euclidean-geometry"))
    implementation(alchemist("implementationbase"))
    implementation(alchemist("ui-tooling"))
    implementation(alchemist("loading"))
    implementation(alchemist("maps"))
    implementation(libs.bundles.jiconfont)
    implementation(libs.controlsfx)
    implementation(libs.jfoenix)
    implementation(libs.gson.extras)
    implementation(libs.guava)
    implementation(libs.apache.commons.collections4)
    implementation(libs.javafxSvg)
    implementation(libs.leafletmap)
    implementation(libs.tornadofx)
    val javaFXVersion = "11"
    for (platform in listOf("linux", "mac", "win")) {
        api("org.openjfx:javafx-base:$javaFXVersion:$platform")
        api("org.openjfx:javafx-controls:$javaFXVersion:$platform")
        api("org.openjfx:javafx-fxml:$javaFXVersion:$platform")
        api("org.openjfx:javafx-graphics:$javaFXVersion:$platform")
        api("org.openjfx:javafx-media:$javaFXVersion:$platform")
        api("org.openjfx:javafx-swing:$javaFXVersion:$platform")
        api("org.openjfx:javafx-web:$javaFXVersion:$platform")
    }
    testRuntimeOnly(incarnation("protelis"))
}

publishing.publications {
    withType<MavenPublication> {
        pom {
            developers {
                developer {
                    name.set("Niccolò Maltoni")
                    email.set("niccolo.maltoni@studio.unibo.it")
                }
                developer {
                    name.set("Vuksa Mihajlovic")
                    email.set("vuksa.mihajlovic@studio.unibo.it")
                }
            }
        }
    }
}
