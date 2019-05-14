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
    api("it.unibo.apice.scafiteam:scafi-core_${extra["scalaMajorVersion"]}:${extra["scafiVersion"]}")

    implementation("com.github.cb372:scalacache-core_${extra["scalaMajorVersion"]}:${extra["scalaCacheVersion"]}")
    implementation("com.github.cb372:scalacache-guava_${extra["scalaMajorVersion"]}:${extra["scalaCacheVersion"]}")
    implementation("org.scala-lang:scala-compiler:${extra["scalaVersion"]}")
    implementation("org.scala-lang:scala-library:${extra["scalaVersion"]}")
    implementation(project(":alchemist-implementationbase"))
    implementation(project(":alchemist-time"))

    testImplementation(project(":alchemist-engine"))
    testImplementation(project(":alchemist-loading"))
    testImplementation("org.scalatest:scalatest_${extra["scalaMajorVersion"]}:${extra["scalatestVersion"]}")
    testImplementation("org.pegdown:pegdown:${extra["pegdownVersion"]}")
}
