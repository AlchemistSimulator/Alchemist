/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
plugins {
    id("com.gradle.enterprise") version "3.13.1"
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "1.1.7"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

include(
    "alchemist-api",
    "alchemist-benchmark",
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
    "alchemist-multivesta-adapter",
    "alchemist-physics",
    "alchemist-sapere-mathexp",
    "alchemist-smartcam",
    "alchemist-swingui",
    "alchemist-sapere-mathexp",
    "alchemist-smartcam",
    "alchemist-test",
    "alchemist-ui-tooling",
    "alchemist-swingui",
    "alchemist-fxui",
    "alchemist-web-renderer",
)
rootProject.name = "alchemist"

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        publishOnFailure()
    }
}

gitHooks {
    commitMsg { conventionalCommits() }
    preCommit {
        tasks("ktlintCheck", "checkScalafmt", "--parallel")
    }
    createHooks(overwriteExisting = true)
}
include("alchemist-benchmark")
