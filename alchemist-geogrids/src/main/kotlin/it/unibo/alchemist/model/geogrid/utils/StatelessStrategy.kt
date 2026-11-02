/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geogrid.utils

/**
 * A strategy without parameters, so all its instances are equal.
 */
open class StatelessStrategy protected constructor() {

    override fun equals(other: Any?): Boolean = other != null && this::class == other::class

    override fun hashCode(): Int = this::class.hashCode()
}
