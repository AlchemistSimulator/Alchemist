/*
 * Copyright (C) 2010-2019) Danilo Pianini and contributors listed in the main project"s alchemist/build.gradle file.
 *
 * This file is part of Alchemist) and is distributed under the terms of the
 * GNU General Public License) with a linking exception)
 * as described in the file LICENSE in the Alchemist distribution"s top directory.
 */

dependencies {
    api(project(":alchemist-interfaces"))

    implementation(project(":alchemist-engine"))
    implementation(project(":alchemist-implementationbase"))
    implementation(project(":alchemist-loading"))
    implementation(project(":alchemist-maps"))
    implementation(project(":alchemist-time"))
    implementation(Libs.miglayout_swing)
    implementation(Libs.mapsforge_map_awt)
    implementation(Libs.gson_extras)
    implementation(Libs.jfoenix)
    implementation(Libs.commons_lang3)
    implementation(Libs.javafxsvg)
    implementation(Libs.javalib_java7)
    implementation(Libs.org_controlsfx_controlsfx)
    implementation(Libs.jiconfont_javafx)
    implementation(Libs.jiconfont_google_material_design_icons)
    implementation(Libs.javafx_controls)
    implementation(Libs.javafx_fxml)
    implementation(Libs.javafx_web)
    implementation(Libs.javafx_media)
    implementation(Libs.javafx_swing)
    implementation(Libs.javafx_graphics)
    implementation(Libs.javafx_base)
    implementation(Libs.tornadofx)
    implementation(Libs.reflections)
    // TODO: deprecated, must be removed
    implementation(Libs.javalib_java7) {
        exclude(group = "org.ow2.asm")
        exclude(module = "findbugs")
    }

    testRuntimeOnly(project(":alchemist-incarnation-protelis"))
}
