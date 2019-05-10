/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project"s alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution"s top directory.
 */

dependencies {
    implementation(project(":alchemist-interfaces"))
    implementation(project(":alchemist-loading"))
    implementation(project(":alchemist-implementationbase"))
    implementation(project(":alchemist-engine"))
    implementation("com.google.guava:guava:${extra["guavaVersion"]}")
    implementation("commons-io:commons-io:${extra["commonsIOVersion"]}")
    implementation("org.apache.ignite:ignite-core:${extra["igniteVersion"]}")
    implementation("org.apache.ignite:ignite-spring:${extra["igniteVersion"]}")
    implementation("org.apache.ignite:ignite-indexing:${extra["igniteVersion"]}")
    testImplementation(project(":alchemist-incarnation-sapere"))
    testImplementation(project(":alchemist-time"))
}
