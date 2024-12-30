/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

import Libs.alchemist
import Util.allVerificationTasks
import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.jvm.tasks.Jar
import org.jetbrains.dokka.gradle.tasks.DokkaBaseTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.tasks.KtLintCheckTask
import org.jlleitschuh.gradle.ktlint.tasks.KtLintFormatTask

plugins {
    antlr
    id("kotlin-jvm-convention")
}

dependencies {
    antlr(libs.antlr4)
    api(alchemist("implementationbase"))
    api(alchemist("euclidean-geometry"))
    api(alchemist("physics"))
    implementation(libs.apache.commons.lang3)
    implementation(libs.boilerplate)
    implementation(libs.jirf)
    implementation(libs.trove4j)
    runtimeOnly(libs.antlr4.runtime)
    testImplementation(alchemist("engine"))
    testImplementation(alchemist("loading"))
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

tasks.generateGrammarSource.configure {
    val destination = "it.unibo.alchemist.model.biochemistry.dsl"
    arguments = arguments + listOf("-visitor", "-package", destination, "-long-messages")
}

tasks.generateTestGrammarSource {
    enabled = false
}

// Ensure that the grammar is generated before any task that needs it
tasks {
    listOf(
        withType<Detekt>(),
        withType<JavaCompile>(),
        withType<KotlinCompile>(),
        withType<KtLintCheckTask>(),
        withType<KotlinCompilationTask<*>>(),
        withType<DokkaBaseTask>(),
        withType<Jar>(),
        withType<KtLintFormatTask>(),
    ).forEach {
        it.configureEach { dependsOn(generateGrammarSource) }
    }
}

spotbugs {
    val sourcesToAnalyze =
        project.sourceSets.main.flatMap { main ->
            project.sourceSets.test.map { test ->
                listOf(main, test).map { it.toString() }
            }
        }
    onlyAnalyze.set(sourcesToAnalyze)
}

tasks.allVerificationTasks.configureEach {
    dependsOn(tasks.generateGrammarSource)
}

publishing.publications {
    withType<MavenPublication> {
        pom {
            developers {
                developer {
                    name.set("Luca Giuliani")
                    email.set("luca.giuliani10@studio.unibo.it")
                }
                developer {
                    name.set("Gabriele Graffieti")
                    email.set("gabriele.graffieti@studio.unibo.it")
                }
                developer {
                    name.set("Franco Pradelli")
                    email.set("franco.pradelli@studio.unibo.it")
                }
            }
            contributors {
                contributor {
                    name.set("Sara Montagna")
                    email.set("sara.montagna@unibo.it")
                    url.set("https://www.unibo.it/sitoweb/sara.montagna")
                }
            }
        }
    }
}
