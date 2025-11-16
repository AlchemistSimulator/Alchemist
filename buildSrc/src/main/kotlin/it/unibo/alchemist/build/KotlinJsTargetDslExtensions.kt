/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.build

import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

/**
 * Configure a Dev Server for Web targets.
 */
fun KotlinJsTargetDsl.devServer() {
    browser {
        commonWebpackConfig {
            devServer = (devServer ?: KotlinWebpackConfig.DevServer())
        }
    }
}

/**
 *  Common configuration for Web targets.
 */
fun KotlinJsTargetDsl.webCommonConfiguration() {
    outputModuleName.set(project.name)
    browser { commonWebpackConfig { outputFileName = "${project.name}.js" } }
    binaries.executable()
}
