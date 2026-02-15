/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.positionfilters

import it.unibo.alchemist.model.Position2D
import java.awt.geom.Rectangle2D
import kotlin.math.abs
import kotlin.math.min

/**
 * A Rectangle.
 *
 * @param x
 * start x point
 * @param y
 * start y point
 * @param w
 * width
 * @param h
 * height
 * @param <P> position type
</P> */
class Rectangle<P : Position2D<P>>(x: Double, y: Double, w: Double, h: Double) :
    Abstract2DShape<P>(Rectangle2D.Double(min(x, x + w), min(y, y + h), abs(w), abs(h)))
