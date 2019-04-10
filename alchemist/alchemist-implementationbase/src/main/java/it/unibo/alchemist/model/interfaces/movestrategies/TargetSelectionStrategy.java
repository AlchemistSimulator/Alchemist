/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
/**
 *
 */
package it.unibo.alchemist.model.interfaces.movestrategies;

import java.io.Serializable;

import it.unibo.alchemist.model.interfaces.Position;

/**
 * This interface models a strategy for selecting positions where to move.
 *
 * @param <P> Position type
 */
@FunctionalInterface
public interface TargetSelectionStrategy<P extends Position<? extends P>> extends Serializable {

    /**
     * @return the next target where the {@link it.unibo.alchemist.model.interfaces.Node} is directed
     */
    P getTarget();

}
