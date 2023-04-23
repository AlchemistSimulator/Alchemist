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
