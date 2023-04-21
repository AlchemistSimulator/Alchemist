/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.interfaces;

import it.unibo.alchemist.model.Obstacle;
import it.unibo.alchemist.model.geometry.Vector2D;

import java.awt.Shape;

/**
 * An {@link Obstacle} in a bidimensional space.
 *
 * @param <V> the vector type for the space in which this obstacle is placed.
 */
public interface Obstacle2D<V extends Vector2D<V>> extends Obstacle<V>, Shape {
}
