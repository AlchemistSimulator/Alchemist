import Libs.alchemist
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

// Required by the 'shadowJar' task
project.setProperty("mainClassName", "it.unibo.alchemist.Alchemist")

plugins {
    application
    id("me.champeau.jmh") version "0.7.1"
}

group = "it.unibo.alchemist"
version = "0.1.0-archeo+68647cc14"

repositories {
    mavenCentral()
}

dependencies {
    jmhImplementation(alchemist("api"))
    jmhImplementation(alchemist("engine"))
    jmhImplementation(alchemist("loading"))
    jmhImplementation(alchemist("euclidean-geometry"))
    jmhImplementation(alchemist("implementationbase"))
    jmhImplementation(alchemist("incarnation-protelis"))
    jmhImplementation(alchemist("swingui"))
    jmhImplementation("org.openjdk.jmh:jmh-core:1.36")
    jmhImplementation("org.openjdk.jmh:jmh-generator-annprocess:1.36")
    jmhImplementation(project(mapOf("path" to ":")))
    jmh("org.apache.commons:commons-math3:3.6.1")
    runtimeOnly(rootProject)
}

tasks.withType<ShadowJar>().configureEach {
    isZip64 = true
}
