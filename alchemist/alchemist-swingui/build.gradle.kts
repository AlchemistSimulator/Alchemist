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
    implementation(project(":alchemist-implementationbase"))
    implementation(project(":alchemist-loading"))
    implementation(project(":alchemist-maps"))
    implementation(project(":alchemist-smartcam"))
    implementation(project(":alchemist-cognitive-agents"))
    implementation(Libs.miglayout_swing)
    implementation(Libs.mapsforge_map_awt) {
        exclude(group = "com.github.blackears", module = "svgSalamander")
    }
    implementation(Libs.gson_extras)
    implementation(Libs.jfoenix)
    implementation(Libs.commons_lang3)
    implementation(Libs.javafxsvg)
    implementation(Libs.javalib_java7)
    implementation(Libs.controlsfx)
    implementation(Libs.jiconfont_javafx)
    implementation(Libs.jiconfont_google_material_design_icons)
    implementation(Libs.tornadofx)
    implementation(Libs.svgsalamander)
    // TODO: deprecated, must be removed
    implementation(Libs.javalib_java7) {
        exclude(group = "org.ow2.asm")
        exclude(module = "findbugs")
    }
    implementation(Libs.org_danilopianini_conrec)

    val javaFXVersion = "11"
    for (platform in listOf("linux", "mac", "win")) {
        api("org.openjfx:javafx-base:$javaFXVersion:$platform")
        api("org.openjfx:javafx-controls:$javaFXVersion:$platform")
        api("org.openjfx:javafx-fxml:$javaFXVersion:$platform")
        api("org.openjfx:javafx-graphics:$javaFXVersion:$platform")
        api("org.openjfx:javafx-media:$javaFXVersion:$platform")
        api("org.openjfx:javafx-swing:$javaFXVersion:$platform")
    }

    testRuntimeOnly(project(":alchemist-incarnation-protelis"))
}

configurations.all {
    resolutionStrategy {
        eachDependency {
            if (requested.name == "svgSalamander") {
                useTarget(Libs.svgsalamander)
                because("mapsforge version is not on central")
            }
        }
    }
}
