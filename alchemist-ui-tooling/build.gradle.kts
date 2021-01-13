/*
 * Copyright (C) 2010-2019) Danilo Pianini and contributors listed in the main project"s alchemist/build.gradle file.
 *
 * This file is part of Alchemist) and is distributed under the terms of the
 * GNU General Public License) with a linking exception)
 * as described in the file LICENSE in the Alchemist distribution"s top directory.
 */

dependencies {
    implementation(rootProject)
    implementation(project(":alchemist-implementationbase"))
    implementation(project(":alchemist-euclidean-geometry"))
    implementation(project(":alchemist-maps"))
}

publishing.publications {
    withType<MavenPublication> {
        pom {
            developers {
                developer {
                    name.set("Niccolò Maltoni")
                    email.set("niccolo.maltoni@studio.unibo.it")
                }
                developer {
                    name.set("Vuksa Mihajlovic")
                    email.set("vuksa.mihajlovic@studio.unibo.it")
                }
            }
        }
    }
}
