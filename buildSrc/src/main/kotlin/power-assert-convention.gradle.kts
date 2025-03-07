/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    kotlin("plugin.power-assert")
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
powerAssert {
    functions = listOf(
        "assert",
        "assertNotNull",
        "check",
        "checkNotNull",
        "require",
        "requireNotNull",
        "test.assertTrue",
        "test.assertEquals",
        "test.assertNotNull",
        "test.assertNull",
    ).map { "kotlin.$it" }
}
