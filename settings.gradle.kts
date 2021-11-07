/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
import org.danilopianini.VersionAliases.justAdditionalAliases
plugins {
    id("com.gradle.enterprise") version "3.6.4"
    id("de.fayard.refreshVersions") version "0.10.1"
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.danilopianini:refreshversions-aliases:+")
    }
}

include(
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
    "alchemist-interfaces",
    "alchemist-loading",
    "alchemist-maps",
    "alchemist-physical-agents",
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

refreshVersions {
    extraArtifactVersionKeyRules = justAdditionalAliases
    featureFlags {
        enable(de.fayard.refreshVersions.core.FeatureFlag.LIBS)
    }
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        publishAlways()
    }
}

enableFeaturePreview("VERSION_CATALOGS")

with(File(rootProject.projectDir, ".git/hooks/commit-msg")) {
    if (!exists()) {
        parentFile.mkdirs()
        runCatching {
            val github = "https://raw.githubusercontent.com"
            val script = java.net.URL("$github/DanySK/conventional-pre-commit/main/conventional-pre-commit.sh")
            writeText(script.readText())
            setExecutable(true)
        }.onFailure { println("Warning: the commit hook could not be downloaded!") }
    }
}
with(File(rootProject.projectDir, ".git/hooks/pre-commit")) {
    if (!exists()) {
        parentFile.mkdirs()
        writeText("#!/bin/sh\n./gradlew ktlintCheck || exit 1\n")
    }
    setExecutable(true)
}
