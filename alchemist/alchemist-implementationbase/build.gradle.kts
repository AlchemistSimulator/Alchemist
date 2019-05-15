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
    api(Libs.java_quadtree)
    implementation(project(":alchemist-time"))
    implementation(Libs.caffeine)
    implementation(Libs.rtree)
    implementation(Libs.concurrentlinkedhashmap_lru)
    implementation(Libs.classgraph)
    implementation(Libs.trove4j)
    implementation(Libs.boilerplate)
    implementation(Libs.scala_compiler)
    implementation(Libs.scala_library)
}
