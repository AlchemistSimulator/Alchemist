/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

/**
 * 
 */
package it.unibo.alchemist.model;

import java.io.Serializable;

/**
 * This interface is a wrapper for concentrations.
 * 
 * @param <T> 
 */
@FunctionalInterface
public interface Concentration<T> extends Serializable {

    /**
     * Allows to access the content of the concentration.
     * 
     * @return the actual content of the concentration
     */
    T getContent();

}
