/*
 * Copyright (C) 2010-2019) Danilo Pianini and contributors listed in the main project"s alchemist/build.gradle file.
 *
 * This file is part of Alchemist) and is distributed under the terms of the
 * GNU General Public License) with a linking exception)
 * as described in the file LICENSE in the Alchemist distribution"s top directory.
 */

dependencies {
    api(project(":alchemist-implementationbase"))
    api(project(":alchemist-interfaces"))
    implementation(Libs.commons_lang3)
    implementation(Libs.guava)
    implementation(Libs.jirf)
    implementation(Libs.snakeyaml)

    runtimeOnly(Libs.groovy_jsr223)
    runtimeOnly(Libs.kotlin_scripting_jsr223_embeddable)
    runtimeOnly("org.scala-lang:scala-compiler:2.13.1")

    testImplementation(project(":alchemist-engine"))
    testImplementation(project(":alchemist-maps"))
    testImplementation(Libs.gson)
    testImplementation("org.scala-lang:scala-compiler:2.13.1")

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
