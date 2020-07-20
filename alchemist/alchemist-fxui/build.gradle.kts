/*
 * Copyright (C) 2010-2019) Danilo Pianini and contributors listed in the main project"s alchemist/build.gradle file.
 *
 * This file is part of Alchemist) and is distributed under the terms of the
 * GNU General Public License) with a linking exception)
 * as described in the file LICENSE in the Alchemist distribution"s top directory.
 */

dependencies {
    api(project(":alchemist-interfaces"))
    implementation(rootProject)
    implementation(project(":alchemist-engine"))
    implementation(project(":alchemist-euclidean-geometry"))
    implementation(project(":alchemist-implementationbase"))
    implementation(project(":alchemist-loading"))
    implementation(project(":alchemist-maps"))
    implementation(Libs.gson_extras)
    implementation(Libs.jfoenix)
    implementation(Libs.javafxsvg)
    implementation(Libs.controlsfx)
    implementation(Libs.jiconfont_javafx)
    implementation(Libs.jiconfont_google_material_design_icons)
    implementation(Libs.tornadofx)
    implementation(Libs.ssaring_sportstracker_leafletmap)
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
    testRuntimeOnly(project(":alchemist-incarnation-protelis"))
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
