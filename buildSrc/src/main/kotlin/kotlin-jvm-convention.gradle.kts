import Libs.alchemist
import it.unibo.alchemist.build.catalog
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode

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
    id("dokka-convention")
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
    val alchemistMaintenanceTooling = alchemist("maintenance-tooling")
    if (project != alchemistApi && project != alchemistMaintenanceTooling) {
        api(alchemistApi)
        implementation(alchemistMaintenanceTooling)
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
        jvmDefault.set(JvmDefaultMode.ENABLE)
    }
}

javaQA {
    checkstyle {
        additionalConfiguration.set(rootProject.file("checkstyle-additional-config.xml").readText())
        additionalSuppressions.set(rootProject.file("checkstyle-suppressions.xml").readText())
    }
    // enable PMD when this bug is fixed: https://github.com/pmd/pmd/issues/5096
    tasks.withType<Pmd>().configureEach {
        enabled = false
    }
}
