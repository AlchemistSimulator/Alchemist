/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.variables

import it.unibo.alchemist.boundary.DependentVariable

/**
 * A constant [value], expressed as a variable to promote code reuse in Alchemist specifications.
 */
class Constant<V>(val value: V) : DependentVariable<V> {
    override fun getWith(variables: Map<String, Any>): V = value
}
