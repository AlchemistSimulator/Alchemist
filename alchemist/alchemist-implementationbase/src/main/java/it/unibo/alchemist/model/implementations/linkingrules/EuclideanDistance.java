/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.linkingrules;

import it.unibo.alchemist.model.interfaces.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @param <T>
 * @param <P>
 */
@Deprecated
public class EuclideanDistance<T, P extends Position<P>> extends ConnectWithinDistance<T, P> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EuclideanDistance.class);

    /**
     *
     * @param radius connection range.
     */
    public EuclideanDistance(final double radius) {
        super(radius);
        LOGGER.warn("{} has been deprecated in favor of {}. Please update your simulation configuration to use the latter",
            this.getClass().getSimpleName(),
            ConnectWithinDistance.class.getSimpleName()
        );
    }

}
