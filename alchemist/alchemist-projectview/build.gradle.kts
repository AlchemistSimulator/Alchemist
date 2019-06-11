/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project"s alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution"s top directory.
 */
val platforms = listOf("linux", "mac", "win")
val libraries = listOf(Libs.javafx_base, Libs.javafx_controls, Libs.javafx_fxml, Libs.javafx_graphics, Libs.javafx_media, Libs.javafx_swing)
dependencies {
    api(project(":alchemist-interfaces"))
    for (platform in platforms) {
        for (library in libraries) {
            api("$library:$platform")
        }
    }
    implementation(project(":alchemist-implementationbase"))
    implementation(project(":alchemist-loading"))
    implementation(project(":alchemist-runner"))
    implementation(project(":alchemist-swingui"))
    implementation(project(":alchemist-time"))
    implementation(Libs.controlsfx)
    implementation(Libs.gson)
    implementation(Libs.javafxsvg)
    implementation(Libs.commons_io)

    testRuntimeOnly(project(":alchemist-incarnation-protelis"))
    testRuntimeOnly(project(":alchemist-incarnation-sapere"))
    testRuntimeOnly(project(":alchemist-incarnation-biochemistry"))
}

spotbugs {
    isIgnoreFailures = true
}