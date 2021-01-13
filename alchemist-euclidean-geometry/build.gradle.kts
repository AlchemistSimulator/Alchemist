/*
 * Copyright (C) 2010-2019) Danilo Pianini and contributors listed in the main project"s alchemist/build.gradle file.
 *
 * This file is part of Alchemist) and is distributed under the terms of the
 * GNU General Public License) with a linking exception)
 * as described in the file LICENSE in the Alchemist distribution"s top directory.
 */

dependencies {
    api(alchemist("interfaces"))
    api(alchemist("implementationbase"))
    implementation(jgrapht("core"))
    implementation(Libs.boilerplate)
    implementation(Libs.caffeine)
    implementation(Libs.classgraph)
    implementation(Libs.commons_lang3)
    implementation(Libs.concurrentlinkedhashmap_lru)
    implementation(Libs.rtree)
    implementation(Libs.trove4j)
    testImplementation(alchemist("loading"))
    testRuntimeOnly(alchemist("incarnation-protelis"))
}

publishing.publications {
    withType<MavenPublication> {
        pom {
            developers {
                developer {
                    name.set("Lorenzo Paganelli")
                    email.set("lorenzo.paganelli3@studio.unibo.it")
                }
                developer {
                    name.set("Federico Pettinari")
                    email.set("federico.pettinari2@studio.unibo.it")
                }
            }
            contributors {
                contributor {
                    name.set("Diego Mazzieri")
                    email.set("diego.mazzieri@studio.unibo.it")
                }
            }
        }
    }
}
