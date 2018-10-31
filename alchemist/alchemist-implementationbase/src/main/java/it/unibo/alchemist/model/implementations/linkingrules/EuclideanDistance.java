/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.model.implementations.linkingrules;

import it.unibo.alchemist.model.interfaces.Position;

@Deprecated
public class EuclideanDistance<T, P extends Position<P>> extends ConnectWithinDistance<T, P> {

    public EuclideanDistance(final double radius) {
        super(radius);
    }

}
