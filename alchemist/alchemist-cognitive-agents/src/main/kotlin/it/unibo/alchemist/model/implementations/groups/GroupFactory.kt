/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.groups

/**
 * Utility for shorter loading of groups within the Yaml files.
 */
object GroupFactory {
    fun family(): Family<Any, Nothing, Nothing> = Family()
    fun friends(): Friends<Any, Nothing, Nothing> = Friends()
}
