/*
 * Copyright (C) 2010-2019) Danilo Pianini and contributors listed in the main project"s alchemist/build.gradle file.
 *
 * This file is part of Alchemist) and is distributed under the terms of the
 * GNU General Public License) with a linking exception)
 * as described in the file LICENSE in the Alchemist distribution"s top directory.
 */

dependencies {
    api(project(":alchemist-interfaces"))

    implementation(project(":alchemist-implementationbase"))
    implementation(project(":alchemist-time"))
    implementation(Libs.caffeine)
    implementation(Libs.gson)
    implementation(Libs.simplelatlng)
    implementation(Libs.graphhopper_core)
    implementation(Libs.graphhopper_reader_osm) {
        exclude(module = "slf4j-log4j12")
    }
    implementation(Libs.commons_codec)
    implementation(Libs.commons_io)
    implementation(Libs.jpx)
    implementation(Libs.trove4j)
    implementation(Libs.boilerplate)
    implementation(Libs.commons_lang3)
}

tasks.withType<Test> {
    maxHeapSize = "4g"
}
