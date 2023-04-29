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
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project"s alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution"s top directory.
 */

dependencies {
    api(alchemist("api"))
    implementation(alchemist("implementationbase"))
    implementation(alchemist("maps"))
    implementation(alchemist("physics"))
    implementation(alchemist("sapere-mathexp"))
    implementation(libs.boilerplate)
    implementation(libs.trove4j)
}

spotbugs {
    ignoreFailures.set(true)
}

pmd {
    isIgnoreFailures = true
}

publishing.publications {
    withType<MavenPublication> {
        pom {
            developers {
                developer {
                    name.set("Giacomo Pronti")
                    email.set("giacomo.pronti@studio.unibo.it")
                    url.set("http://apice.unibo.it/xwiki/bin/view/XWiki/GiacomoPronti/")
                }
            }
            contributors {
                contributor {
                    name.set("Michele Bombardi")
                    email.set("michele.bombardi@studio.unibo.it")
                    url.set("http://apice.unibo.it/xwiki/bin/view/XWiki/MicheleBombardi/")
                }
                contributor {
                    name.set("Chiara Casalboni")
                    email.set("chiara.casalboni2@studio.unibo.it")
                    url.set("http://apice.unibo.it/xwiki/bin/view/XWiki/ChiaraCasalboni2/")
                }
                contributor {
                    name.set("Enrico Galassi")
                    email.set("enrico.galassi@studio.unibo.it")
                    url.set("http://apice.unibo.it/xwiki/bin/view/XWiki/EnricoGalassi/")
                }
                contributor {
                    name.set("Sara Montagna")
                    email.set("sara.montagna@unibo.it")
                    url.set("http://saramontagna.apice.unibo.it/")
                }
                contributor {
                    name.set("Luca Nenni")
                    email.set("luca.nenni@studio.unibo.it")
                    url.set("http://apice.unibo.it/xwiki/bin/view/XWiki/LucaNenni/")
                }
            }
        }
    }
}
