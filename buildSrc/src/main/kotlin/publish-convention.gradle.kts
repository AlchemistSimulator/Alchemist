/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

import it.unibo.alchemist.build.checkNoFatJarIn
import it.unibo.alchemist.build.isInCI
import org.danilopianini.gradle.mavencentral.tasks.ZipMavenCentralPortalPublication

plugins {
    id("org.danilopianini.publish-on-central")
}

if (isInCI) {
    signing {
        val signingKey: String? by project
        val signingPassword: String? by project
        useInMemoryPgpKeys(signingKey, signingPassword)
    }
}

val repoSlug = "AlchemistSimulator/Alchemist"
publishOnCentral {
    projectDescription.set(extra["projectDescription"].toString())
    projectLongName.set(extra["projectLongName"].toString())
    projectUrl.set("https://github.com/$repoSlug")
    licenseName.set("GPL 3.0 with linking exception")
    licenseUrl.set(projectUrl.map { "$it/blob/master/LICENSE.md" })
    scmConnection.set("git:git@github.com:$repoSlug.git")
    repository("https://maven.pkg.github.com/${repoSlug.lowercase()}") {
        user.set("DanySK")
        password.set(System.getenv("GITHUB_TOKEN"))
    }
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom {
        developers {
            developer {
                name.set("Danilo Pianini")
                email.set("danilo.pianini@unibo.it")
                url.set("https://www.danilopianini.org")
                roles.set(mutableSetOf("architect", "developer"))
            }
        }
    }
}

tasks.withType<ZipMavenCentralPortalPublication>().configureEach {
    val bundle = archiveFile
    doLast { checkNoFatJarIn(bundle.get().asFile) }
}
