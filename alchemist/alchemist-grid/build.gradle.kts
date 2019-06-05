/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project"s alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution"s top directory.
 */

dependencies {
    api(Libs.ignite_core)
    implementation(project(":alchemist-interfaces"))
    implementation(project(":alchemist-loading"))
    implementation(project(":alchemist-implementationbase"))
    implementation(project(":alchemist-engine"))
    implementation(Libs.guava)
    implementation(Libs.ignite_spring)
    implementation(Libs.ignite_indexing)
    implementation(Libs.commons_io)

    testImplementation(project(":alchemist-incarnation-sapere"))
    testImplementation(project(":alchemist-time"))
}
