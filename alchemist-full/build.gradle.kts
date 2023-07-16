/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main(project"s alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution"s top directory.
 */

import Libs.alchemist
import Util.isMultiplatform

plugins {
    application
}

dependencies {
    runtimeOnly(rootProject)
    rootProject.subprojects.filterNot { it == project }.forEach {
        if (it.isMultiplatform) {
            runtimeOnly(project(path = ":${it.name}", configuration = "default"))
        } else {
            runtimeOnly(it)
        }
    }
    testImplementation(rootProject)
    testImplementation(alchemist("physics"))
}

application {
    mainClass.set("it.unibo.alchemist.Alchemist")
}

tasks.withType<AbstractArchiveTask> {
    duplicatesStrategy = DuplicatesStrategy.WARN
}

publishing.publications {
    withType<MavenPublication> {
        pom {
            contributors {
                contributor {
                    name.set("Angelo Filaseta")
                    email.set("angelo.filaseta@studio.unibo.it")
                }
            }
        }
    }
}
