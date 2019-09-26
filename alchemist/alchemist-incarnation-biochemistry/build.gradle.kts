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
    api(project(":alchemist-implementationbase"))
    implementation(Libs.commons_lang3)
    implementation(Libs.trove4j)
    implementation(Libs.boilerplate)
    implementation(Libs.jirf)
    runtimeOnly(Libs.antlr4_runtime)
    runtimeOnly(Libs.bcel)
    testImplementation(project(":alchemist-engine"))
    testImplementation(project(":alchemist-loading"))
    testImplementation(project(":alchemist-time"))
    testImplementation(Libs.kotlintest_runner_junit5)
}

/*
 * This is required, as the antlr configuration pulls in (and needs to use) a recent version of the antlr-runtime, not just of antlr4-runtime.
 * Such dependency then breaks Protelis, which uses Xtext that relies on an older antlr.
 * The antlr-runtime dependency is only needed for running antlr, as the runtime uses antlr4-runtime.
 */
configurations {
    all {
        if (!name.contains("antlr")) {
            exclude(module = "antlr-runtime")
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.generateGrammarSource {
    arguments = arguments + listOf("-visitor", "-package", "it.unibo.alchemist.biochemistrydsl", "-long-messages")
}

tasks.compileJava { dependsOn(tasks.generateGrammarSource) }
tasks.compileKotlin { dependsOn(tasks.generateGrammarSource) }

val sourceSetsToCheck = listOf(project.sourceSets.main.get(), project.sourceSets.test.get())
checkstyle {
    sourceSets = sourceSetsToCheck
}

pmd {
    sourceSets = sourceSetsToCheck
}

tasks.withType<Pmd> {
    exclude("**/biochemistrydsl/**")
}

spotbugs {
    sourceSets = sourceSetsToCheck
}
