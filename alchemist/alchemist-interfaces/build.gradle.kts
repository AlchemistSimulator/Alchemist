/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

dependencies {
    api(Libs.listset)
    api(Libs.jool_java_8)
    api(Libs.commons_math3)
}

publishing.publications {
    withType<MavenPublication> {
        pom {
            contributors {
                contributor {
                    name.set("Sara Montagna")
                    email.set("sara.montagna@unibo.it")
                    url.set("http://saramontagna.apice.unibo.it/")
                }
                contributor {
                    name.set("Lorenzo Paganelli")
                    email.set("lorenzo.paganelli3@studio.unibo.it")
                }
                contributor {
                    name.set("Federico Pettinari")
                    email.set("federico.pettinari2@studio.unibo.it")
                }
                contributor {
                    name.set("Franco Pradelli")
                    email.set("franco.pradelli@studio.unibo.it")
                }
                contributor {
                    name.set("Giacomo Pronti")
                    email.set("giacomo.pronti@studio.unibo.it")
                    url.set("http://apice.unibo.it/xwiki/bin/view/XWiki/GiacomoPronti/")
                }
                contributor {
                    name.set("Giacomo Scaparrotti")
                    email.set("giacomo.scaparrotti@studio.unibo.it")
                    url.set("https://www.linkedin.com/in/giacomo-scaparrotti-0aa77569")
                }
            }
        }
    }
}
