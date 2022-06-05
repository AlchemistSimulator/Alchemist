/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.interfaces;

import java.io.Serializable;

/**
 * 
 * Interface for static layer, containing a substance or a molecule with a
 * spatial distribution.
 *
 * @param <T>
 *            the value that measure the substance in a point.
 * @param <P>
 *            Concentration type
 */
@FunctionalInterface
public interface Layer<T, P extends Position<? extends P>> extends Serializable {

    /**
     * 
     * @param p
     *            the {@link Position}.
     * @return the value in the requested {@link Position}.
     */
    T getValue(P p);

}
