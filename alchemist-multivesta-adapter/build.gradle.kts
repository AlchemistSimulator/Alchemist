/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
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
    implementation(rootProject)
    implementation(alchemist("loading"))
    with(libs.apache.commons) {
        implementation(cli)
        implementation(io)
    }
    implementation(libs.apache.commons.cli)
    implementation(libs.guava)
    implementation(libs.logback)
}
