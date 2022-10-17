/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
plugins {
    id("com.gradle.enterprise") version "3.11.1"
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "1.0.22"
}

include(
    "alchemist-api",
    "alchemist-cognitive-agents",
    "alchemist-engine",
    "alchemist-euclidean-geometry",
    "alchemist-full",
    "alchemist-grid",
    "alchemist-implementationbase",
    "alchemist-incarnation-protelis",
    "alchemist-incarnation-sapere",
    "alchemist-incarnation-scafi",
    "alchemist-incarnation-biochemistry",
    "alchemist-loading",
    "alchemist-maintenance-tooling",
    "alchemist-maps",
    "alchemist-physics",
    "alchemist-sapere-mathexp",
    "alchemist-smartcam",
    "alchemist-swingui",
    "alchemist-sapere-mathexp",
    "alchemist-smartcam",
    "alchemist-test",
    "alchemist-ui-tooling",
    "alchemist-swingui",
    "alchemist-fxui"
)
rootProject.name = "alchemist"

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        publishAlways()
    }
}

gitHooks {
    commitMsg { conventionalCommits() }
    preCommit {
        tasks("ktlintCheck", "checkScalafmt")
    }
    createHooks(overwriteExisting = true)
}
