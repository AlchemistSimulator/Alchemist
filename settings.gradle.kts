/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
plugins {
    id("com.gradle.develocity") version "4.1"
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "2.0.28"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(
    "alchemist-api",
    "alchemist-composeui",
    "alchemist-cognitive-agents",
    "alchemist-engine",
    "alchemist-euclidean-geometry",
    "alchemist-full",
    "alchemist-graphql",
    "alchemist-graphql-surrogates",
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
    "alchemist-web-renderer",
)
rootProject.name = "alchemist"

develocity {
    buildScan {
        termsOfUseUrl = "https://gradle.com/terms-of-service"
        termsOfUseAgree = "yes"
        uploadInBackground = !System.getenv("CI").toBoolean()
    }
}

gitHooks {
    commitMsg { conventionalCommits() }
    preCommit {
        tasks("ktlintCheck", "checkScalafmt", "--parallel")
    }
    createHooks(overwriteExisting = true)
}
