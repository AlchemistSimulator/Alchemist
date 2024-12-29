/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

import Libs.alchemist
import Libs.incarnation

plugins {
    id("kotlin-jvm-convention")
}

dependencies {
    api(alchemist("physics"))
    implementation(rootProject)
    implementation(alchemist("implementationbase"))
    implementation(alchemist("maps"))
    testRuntimeOnly(incarnation("protelis"))
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
