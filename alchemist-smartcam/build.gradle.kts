/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

import Libs.alchemist
import Libs.incarnation

dependencies {
    api(alchemist("api"))

    implementation(alchemist("euclidean-geometry"))
    implementation(alchemist("implementationbase"))

    testImplementation(incarnation("protelis"))
    testImplementation(alchemist("loading"))
}

publishing.publications {
    withType<MavenPublication> {
        pom {
            developers {
                developer {
                    name.set("Federico Pettinari")
                    email.set("federico.pettinari2@studio.unibo.it")
                }
            }
        }
    }
}
