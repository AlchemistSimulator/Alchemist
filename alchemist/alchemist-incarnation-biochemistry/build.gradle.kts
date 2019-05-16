/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project"s alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution"s top directory.
 */

plugins {
    antlr
}

dependencies {
    antlr(Libs.antlr4)

    implementation(project(":alchemist-implementationbase"))
    implementation(Libs.trove4j)
    implementation(Libs.boilerplate)
    implementation(Libs.jirf)

    testImplementation(project(":alchemist-engine"))
    testImplementation(project(":alchemist-loading"))
    testImplementation(project(":alchemist-time"))
    testImplementation(Libs.kotlintest_runner_junit5)

    runtimeOnly(Libs.antlr4_runtime)
}

configurations {
    all {
        if (!name.contains("antlr")) {
            exclude(group = "org.antlr", module = "antlr-runtime")
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.generateGrammarSource {
    arguments = arguments + listOf("-visitor", "-package", "it.unibo.alchemist.biochemistrydsl", "-long-messages")
}

tasks.compileKotlin {
    dependsOn("generateGrammarSource")
}

val sourceSetsToCheck = listOf(project.sourceSets.main.get(), project.sourceSets.test.get())
checkstyle {
    sourceSets = sourceSetsToCheck
}

pmd {
    sourceSets = sourceSetsToCheck
}

spotbugs {
    sourceSets = sourceSetsToCheck
}
