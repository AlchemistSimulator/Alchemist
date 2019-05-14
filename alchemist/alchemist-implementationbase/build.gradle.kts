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
    api("org.danilopianini:java-quadtree:${extra["quadtreeVersion"]}")
    api(project(":alchemist-interfaces"))
    implementation(project(":alchemist-time"))
    implementation("com.github.ben-manes.caffeine:caffeine:${extra["caffeineVersion"]}")
    implementation("com.github.davidmoten:rtree:${extra["rtreeVersion"]}")
    implementation("com.google.guava:guava:${extra["guavaVersion"]}")
    implementation("com.googlecode.concurrentlinkedhashmap:concurrentlinkedhashmap-lru:${extra["concurrentlinkedhashmapVersion"]}")
    implementation("io.github.classgraph:classgraph:${extra["classgraphVersion"]}")
    implementation("net.sf.trove4j:trove4j:${extra["troveVersion"]}")
    implementation("org.danilopianini:boilerplate:${extra["boilerplateVersion"]}")
    implementation("org.scala-lang:scala-compiler:${extra["scalaVersion"]}")
    implementation("org.scala-lang:scala-library:${extra["scalaVersion"]}")
}
//configurations {
//    apiElements {
//        val compileScala = tasks.compileScala.get()
//        outgoing.variants["classes"].artifact(mapOf(
//            "file" to compileScala.destinationDir,
//            "type" to ArtifactTypeDefinition.JVM_CLASS_DIRECTORY,
//            "builtBy" to compileScala)
//        )
//    }
//}

