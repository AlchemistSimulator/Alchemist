/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

import Libs.alchemist

plugins {
    id("kotlin-jvm-convention")
}

dependencies {
    rootProject.subprojects.filter { "incarnation" in it.name }.forEach {
        implementation(it)
    }
    implementation(alchemist("cognitive-agents"))
    implementation(alchemist("loading"))
    implementation(kotlin("script-runtime"))
    implementation(kotlin("scripting-common"))
    implementation(kotlin("scripting-jvm"))
    implementation(kotlin("scripting-jvm-host"))
}
