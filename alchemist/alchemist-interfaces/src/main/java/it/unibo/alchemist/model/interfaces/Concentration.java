/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/

/**
 * 
 */
package it.unibo.alchemist.model.interfaces;

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
