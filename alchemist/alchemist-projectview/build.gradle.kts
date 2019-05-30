/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project"s alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution"s top directory.
 */
dependencies {
    api(project(":alchemist-interfaces"))

    if (JavaVersion.current().isJava9Compatible) {
        implementation("org.controlsfx:controlsfx:${extra["controlsFXVersion"]}")
    } else {
        implementation("org.controlsfx:controlsfx:${extra["controlsFXJ8Version"]}")
    }
    if (JavaVersion.current().isJava11Compatible()) {
        val javaFXVersion = "11"
        for (platform in listOf("linux", "mac", "win")) {
            api("org.openjfx:javafx-base:$javaFXVersion:$platform")
            api("org.openjfx:javafx-controls:$javaFXVersion:$platform")
            api("org.openjfx:javafx-fxml:$javaFXVersion:$platform")
            api("org.openjfx:javafx-graphics:$javaFXVersion:$platform")
            api("org.openjfx:javafx-media:$javaFXVersion:$platform")
            api("org.openjfx:javafx-swing:$javaFXVersion:$platform")
        }
    }
    implementation(project(":alchemist-implementationbase"))
    implementation(project(":alchemist-loading"))
    implementation(project(":alchemist-runner"))
    implementation(project(":alchemist-swingui"))
    implementation(project(":alchemist-time"))
    implementation(Libs.gson)
    implementation(Libs.javafxsvg)
    implementation(Libs.urlclassloader_util)
    implementation(Libs.commons_io)

    testRuntimeOnly(project(":alchemist-incarnation-protelis"))
    testRuntimeOnly(project(":alchemist-incarnation-sapere"))
    testRuntimeOnly(project(":alchemist-incarnation-biochemistry"))
}
