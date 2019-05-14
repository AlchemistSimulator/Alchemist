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
    implementation("com.github.ben-manes.caffeine:caffeine:${extra["caffeineVersion"]}")
    implementation("com.google.code.gson:gson:${extra["gsonVersion"]}")
    implementation("com.google.guava:guava:${extra["guavaVersion"]}")
    implementation("com.javadocmd:simplelatlng:${extra["latlngVersion"]}")
    implementation("com.graphhopper:graphhopper-core:${extra["graphhopperVersion"]}")
    implementation("com.graphhopper:graphhopper-reader-osm:${extra["graphhopperVersion"]}") {
        exclude(module = "slf4j-log4j12")
    }
    implementation("commons-codec:commons-codec:${extra["codecVersion"]}")
    implementation("commons-io:commons-io:${extra["commonsIOVersion"]}")
    implementation("io.jenetics:jpx:${extra["jpxVersion"]}")
    implementation("net.sf.trove4j:trove4j:${extra["troveVersion"]}")
    implementation("org.danilopianini:boilerplate:${extra["boilerplateVersion"]}")
    implementation("org.apache.commons:commons-lang3:${extra["lang3Version"]}")
}

tasks.withType<Test> {
    maxHeapSize = "4g"
}
