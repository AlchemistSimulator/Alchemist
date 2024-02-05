/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

import Libs.alchemist

/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main(project"s alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution"s top directory.
 */

dependencies {
    // API
    api(alchemist("api"))
    api(alchemist("implementationbase"))
    api(libs.bundles.protelis)
    // IMPLEMENTATION
    implementation(alchemist("maps"))
    implementation(alchemist("physics"))
    implementation(libs.apache.commons.lang3)
    // TESTING
    testImplementation(alchemist("loading"))
    testImplementation(alchemist("engine"))
    testImplementation(libs.apache.commons.io)
}

publishing.publications {
    withType<MavenPublication> {
        pom {
            developers {
                developer {
                    name.set("Danilo Pianini")
                    email.set("danilo.pianini@unibo.it")
                    url.set("https://www.danilopianini.org")
                }
            }
            contributors {
                contributor {
                    name.set("Jacob Beal")
                    email.set("jakebeal@bbn.com")
                    url.set("https://jakebeal.github.io/")
                }
            }
        }
    }
}
