/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.loader.shapes;

import it.unibo.alchemist.model.interfaces.Position;

/**
 * A Shape, representing an partition of the space where a {@link Position} may
 * lie in.
 *
 * @param <P> position type
 *
 */
@FunctionalInterface
public interface Shape<P extends Position<P>> {

    /**
     * @param position
     *            the position
     * @return true if the position is inside the {@link Shape}.
     */
    boolean contains(P position);

}
