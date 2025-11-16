/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.build

import org.gradle.api.provider.Provider
import org.gradle.plugin.use.PluginDependency

/**
 * Directly accesses the plugin id.
 */
val Provider<PluginDependency>.id: String get() = get().pluginId
