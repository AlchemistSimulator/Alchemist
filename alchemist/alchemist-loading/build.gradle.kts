/*
 * Copyright (C) 2010-2019) Danilo Pianini and contributors listed in the main project"s alchemist/build.gradle file.
 *
 * This file is part of Alchemist) and is distributed under the terms of the
 * GNU General Public License) with a linking exception)
 * as described in the file LICENSE in the Alchemist distribution"s top directory.
 */

repositories {
    maven {
        url = uri("https://jitpack.io")
        content {
            includeGroup("com.github.graphstream")
        }
    }
}
dependencies {
    api(alchemist("implementationbase"))
    api(alchemist("interfaces"))

    implementation(alchemist("euclidean-geometry"))
    implementation(Libs.commons_lang3)
    implementation(Libs.guava)
    implementation(Libs.jirf)
    implementation(Libs.snakeyaml)
    implementation(graphStream("core"))
    implementation(graphStream("algo"))
    implementation(graphStream("ui-swing"))

    runtimeOnly(Libs.groovy_jsr223)
    runtimeOnly(kotlin("scripting-jsr223"))
    runtimeOnly("org.scala-lang:scala-compiler:2.13.2")

    testImplementation(project(":alchemist-engine"))
    testImplementation(project(":alchemist-maps"))
    testImplementation(Libs.gson)
    testRuntimeOnly("org.scala-lang:scala-compiler:2.13.2")

    testRuntimeOnly(project(":alchemist-incarnation-sapere"))
}

tasks.withType<Test> {
    useJUnitPlatform()
    maxHeapSize = "1500m"
}

publishing.publications {
    withType<MavenPublication> {
        pom {
            contributors {
                contributor {
                    name.set("Matteo Magnani")
                    email.set("matteo.magnani18@studio.unibo.it")
                }
                contributor {
                    name.set("Andrea Placuzzi")
                    email.set("andrea.placuzzi@studio.unibo.it")
                }
                contributor {
                    name.set("Franco Pradelli")
                    email.set("franco.pradelli@studio.unibo.it")
                }
            }
        }
    }
}
