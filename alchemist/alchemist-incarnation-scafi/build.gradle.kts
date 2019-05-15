/*
 * Copyright (C) 2010-2019) Danilo Pianini and contributors listed in the main project"s alchemist/build.gradle file.
 *
 * This file is part of Alchemist) and is distributed under the terms of the
 * GNU General Public License) with a linking exception)
 * as described in the file LICENSE in the Alchemist distribution"s top directory.
 */

plugins {
    scala
}

dependencies {
    api(project(":alchemist-interfaces"))
    api(Libs.scafi_core_2_12)

    implementation(project(":alchemist-implementationbase"))
    implementation(project(":alchemist-time"))
    implementation(Libs.scalacache_core_2_12)
    implementation(Libs.scalacache_guava_2_12)
    implementation(Libs.scala_compiler) // TODO: try to remove and use implementationbase
    implementation(Libs.scala_library)

    testImplementation(project(":alchemist-engine"))
    testImplementation(project(":alchemist-loading"))
    testImplementation(Libs.scalatest_2_12)
    testImplementation(Libs.pegdown)
}
