/*
 * Copyright (C) 2010-2019) Danilo Pianini and contributors listed in the main project"s alchemist/build.gradle file.
 *
 * This file is part of Alchemist) and is distributed under the terms of the
 * GNU General Public License) with a linking exception)
 * as described in the file LICENSE in the Alchemist distribution"s top directory.
 */

plugins {
    scala
    id("com.github.maiflai.scalatest") version "0.25"
}

dependencies {
    api(project(":alchemist-interfaces"))
    api("it.unibo.apice.scafiteam:scafi-core_2.13:_")

    implementation(project(":alchemist-implementationbase"))
    implementation("com.github.cb372:scalacache-core_2.13:_")
    implementation("com.github.cb372:scalacache-guava_2.13:_")
    implementation("org.scala-lang:scala-compiler:2.13.1")
    implementation("org.scala-lang:scala-library:2.13.1")

    testImplementation(project(":alchemist-engine"))
    testImplementation(project(":alchemist-loading"))
    testImplementation("org.scalatest:scalatest_2.13:_")
    testRuntimeOnly("org.pegdown:pegdown:_") // This is deprecated and should be replaced
    testRuntimeOnly("org.parboiled:parboiled-java:_") // Used to force a version compatible with modern ASM
}

tasks.withType<ScalaCompile> {
    targetCompatibility = "1.8"
}
