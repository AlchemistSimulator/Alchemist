/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

import Libs.alchemist
import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.tasks.KtLintCheckTask

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
    antlr(libs.antlr4)
    api(alchemist("implementationbase"))
    api(alchemist("euclidean-geometry"))
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

tasks.generateGrammarSource {
    val destination = "it.unibo.alchemist.model.internal.biochemistry.dsl"
    arguments = arguments + listOf("-visitor", "-package", destination, "-long-messages")
    tasks.sourcesJar.orNull?.dependsOn(this)
}

tasks.generateTestGrammarSource {
    enabled = false
}

tasks {
    val needGrammarGeneration = listOf(
        withType<Detekt>(),
        withType<DokkaTask>(),
        withType<JavaCompile>(),
        withType<KotlinCompile>(),
        withType<KtLintCheckTask>(),
    )
    needGrammarGeneration.forEach {
        it.configureEach {
            dependsOn(generateGrammarSource)
        }
    }
}

val sourceSetsToCheck = listOf(project.sourceSets.main.get(), project.sourceSets.test.get())
checkstyle {
    sourceSets = sourceSetsToCheck
}

pmd {
    sourceSets = sourceSetsToCheck
}

tasks.withType<Pmd> {
    exclude("**/internal/biochemistry/dsl/**")
}

spotbugs {
    onlyAnalyze.value(sourceSetsToCheck.map { it.toString() })
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
                    url.set("http://saramontagna.apice.unibo.it/")
                }
            }
        }
    }
}
