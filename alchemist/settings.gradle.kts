/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
import de.fayard.dependencies.bootstrapRefreshVersionsAndDependencies

include(
    "alchemist-cognitive-agents",
    "alchemist-engine",
    "alchemist-full",
    "alchemist-grid",
    "alchemist-implementationbase",
    "alchemist-incarnation-protelis",
    "alchemist-incarnation-sapere",
    "alchemist-incarnation-scafi",
    "alchemist-incarnation-biochemistry",
    "alchemist-influence-sphere",
    "alchemist-interfaces",
    "alchemist-loading",
    "alchemist-maps",
//    "alchemist-projectview",
    "alchemist-sapere-mathexp",
    "alchemist-smartcam",
    "alchemist-swingui"
)
rootProject.name = "alchemist"

buildscript {
    repositories { gradlePluginPortal() }
    dependencies.classpath("de.fayard:dependencies:+")
}

bootstrapRefreshVersionsAndDependencies()

plugins {
    id("com.gradle.enterprise") version "3.2"
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}
