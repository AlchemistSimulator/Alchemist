import Libs.alchemist

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
    implementation(Libs.trove4j)
    implementation(Libs.boilerplate)
    implementation(Libs.jirf)
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
    arguments = arguments + listOf("-visitor", "-package", "it.unibo.alchemist.biochemistrydsl", "-long-messages")
    tasks.sourcesJar.orNull?.dependsOn(this)
}

tasks.compileJava { dependsOn(tasks.generateGrammarSource) }
tasks.compileKotlin { dependsOn(tasks.generateGrammarSource) }
tasks.compileTestJava { dependsOn(tasks.generateTestGrammarSource) }
tasks.compileTestKotlin { dependsOn(tasks.generateTestGrammarSource) }

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
