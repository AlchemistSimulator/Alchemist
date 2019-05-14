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
    implementation("com.miglayout:miglayout-swing:${extra["miglayoutVersion"]}")
    implementation("org.mapsforge:mapsforge-map-awt:${extra["mapsforgeVersion"]}")
    implementation("org.danilopianini:gson-extras:${extra["gsonExtrasVersion"]}")
    implementation("org.danilopianini:javalib-java7:0.6.1")
    testRuntimeOnly(project(":alchemist-incarnation-protelis"))
}
