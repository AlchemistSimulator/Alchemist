/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main(project"s alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution"s top directory.
 */

dependencies {
    // API
    api(project(":alchemist-interfaces"))
    api(project(":alchemist-implementationbase"))
    api(Libs.protelis_interpreter)
    api(Libs.protelis_lang)
    // IMPLEMENTATION
    implementation(project(":alchemist-maps"))
    implementation(Libs.commons_lang3)
    // TESTING
    testImplementation(project(":alchemist-loading"))
    testImplementation(project(":alchemist-engine"))
    testImplementation(Libs.commons_io)
}

publishing.publications {
    withType<MavenPublication> {
        pom {
            developers {
                developer {
                    name.set("Danilo Pianini")
                    email.set("danilo.pianini@unibo.it")
                    url.set("http://www.danilopianini.org")
                }
            }
            contributors {
                contributor {
                    name.set("Jacob Beal")
                    email.set("jakebeal@bbn.com")
                    url.set("http://web.mit.edu/jakebeal/www/")
                }
            }
        }
    }
}
