/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geometry

import java.awt.Shape

/**
 * Anything which can be represented as a [java.awt.Shape].
 */
interface AwtShapeCompatible {

    /**
     * @return a copy of itself in form of a [java.awt.Shape].
     */
    fun asAwtShape(): Shape
}
