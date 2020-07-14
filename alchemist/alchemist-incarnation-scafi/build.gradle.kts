/*
 * Copyright (C) 2010-2019) Danilo Pianini and contributors listed in the main project"s alchemist/build.gradle file.
 *
 * This file is part of Alchemist) and is distributed under the terms of the
 * GNU General Public License) with a linking exception)
 * as described in the file LICENSE in the Alchemist distribution"s top directory.
 */

plugins {
    scala
    id("com.github.maiflai.scalatest")
}

dependencies {
    api(project(":alchemist-interfaces"))
    api("it.unibo.apice.scafiteam:scafi-core_2.13:_")

    implementation(project(":alchemist-implementationbase"))
    implementation(project(":alchemist-euclidean-geometry"))
    implementation("com.github.cb372:scalacache-core_2.13:_")
    implementation("com.github.cb372:scalacache-guava_2.13:_")
    implementation("org.scala-lang:scala-compiler:2.13.1")
    implementation("org.scala-lang:scala-library:2.13.1")

    testImplementation(project(":alchemist-engine"))
    testImplementation(project(":alchemist-loading"))
    testImplementation("org.scalatest:scalatest_2.13:_")
    testImplementation("org.scalatestplus:scalatestplus-junit_2.13:_")
    testRuntimeOnly("com.vladsch.flexmark:flexmark-profile-pegdown:_")
}

tasks.withType<ScalaCompile> {
    targetCompatibility = "1.8"
}

publishing.publications {
    withType<MavenPublication> {
        pom {
            developers {
                developer {
                    name.set("Roberto Casadei")
                    email.set("roby.casadei@unibo.it")
                    url.set("https://www.unibo.it/sitoweb/roby.casadei")
                }
            }
        }
    }
}
