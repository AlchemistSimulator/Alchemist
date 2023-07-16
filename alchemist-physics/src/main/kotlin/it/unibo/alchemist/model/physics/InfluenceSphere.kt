/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.physics

import it.unibo.alchemist.model.Node

/**
 * Area inside which nodes exert an influence on each other.
 */
interface InfluenceSphere<T> {
    /**
     * List of influential nodes. (e.g. nodes withing a field of view).
     */
    fun influentialNodes(): List<Node<T>>
}
