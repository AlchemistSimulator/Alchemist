/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.konf

import com.uchuhimo.konf.source.DefaultLoaders
import com.uchuhimo.konf.source.Loader
import com.uchuhimo.konf.source.toml
import com.uchuhimo.konf.source.xml
import com.uchuhimo.konf.source.yaml

enum class SupportedSpecType(private val konfLoader: DefaultLoaders.() -> Loader) {
    YAML({ yaml }),
    XML({ xml }),
    JSON({ json }),
    TOML({ toml });

    val DefaultLoaders.selectedSpecType get(): Loader = konfLoader()
}
