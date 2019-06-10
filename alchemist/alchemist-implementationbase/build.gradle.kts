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
    api(Libs.commons_math3)
    api(Libs.java_quadtree)
    api(Libs.guava)
    implementation(project(":alchemist-time"))
    implementation(Libs.boilerplate)
    implementation(Libs.caffeine)
    implementation(Libs.classgraph)
    implementation(Libs.commons_lang3)
    implementation(Libs.concurrentlinkedhashmap_lru)
    implementation(Libs.rtree)
    implementation(Libs.scala_compiler)
    implementation(Libs.scala_library)
    implementation(Libs.trove4j)
}

configurations {
    apiElements {
        val compileScala = tasks.compileScala.get()
        outgoing.variants["classes"].artifact(mapOf(
                "file" to compileScala.destinationDir,
                "type" to ArtifactTypeDefinition.JVM_CLASS_DIRECTORY,
                "builtBy" to compileScala)
        )
    }
}
