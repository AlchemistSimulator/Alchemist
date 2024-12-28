/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

import com.github.spotbugs.snom.SpotBugsTask
import de.aaschmid.gradle.plugins.cpd.Cpd

dependencies {
    implementation(libs.boilerplate)
}

listOf(Pmd::class, Checkstyle::class, SpotBugsTask::class, Cpd::class).forEach {
    tasks.withType(it).configureEach {
        enabled = false
        ignoreFailures = true
    }
}

tasks.javadoc {
    setFailOnError(false)
}

publishing.publications {
    withType<MavenPublication> {
        pom {
            developers {
                developer {
                    name.set("Giacomo Pronti")
                    email.set("giacomo.pronti@studio.unibo.it")
                }
            }
        }
    }
}
