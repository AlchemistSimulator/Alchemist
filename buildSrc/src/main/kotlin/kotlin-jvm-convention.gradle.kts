import Libs.alchemist

/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
plugins {
    kotlin("jvm")
    id("common-static-analysis-convention")
    id("dokka-convention")
    id("java-static-analysis-convention")
    id("kotlin-static-analysis-convention")
    id("org.danilopianini.gradle-java-qa")
    id("power-assert-convention")
}

dependencies {
    val jsr305 by catalog
    val slf4j by catalog
    val `spotbugs-annotations` by catalog
    val resourceloader by catalog

    val alchemistApi = alchemist("api")
    if (project != alchemistApi) {
        api(alchemistApi)
    }

    compileOnly(jsr305)
    compileOnly(`spotbugs-annotations`)

    implementation(slf4j)
    implementation(resourceloader)

    testCompileOnly(jsr305)
    testCompileOnly(`spotbugs-annotations`)

    testImplementation(alchemist("test"))
}

java {
    withJavadocJar()
    withSourcesJar()
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xjvm-default=all") // Enable default methods in Kt interfaces
    }
}
