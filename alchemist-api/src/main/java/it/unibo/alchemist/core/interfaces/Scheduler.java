/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.core.interfaces;

import it.unibo.alchemist.model.interfaces.Actionable;

/**
 * The type which describes the concentration of a molecule
 * 
 * This interface is meant to be implemented by the data structure(s) which must
 * manage the reactions.
 * 
 * @param <T> concentration type
 */
public interface Scheduler<T> {

    /**
     * Adds a reaction to the data structure.
     * 
     * @param reaction
     *            the reaction to be added
     */
    void addReaction(Actionable<T> reaction);

    /**
     * Allows to access the next reaction to be executed.
     * 
     * @return the next reaction to be executed
     */
    Actionable<T> getNext();

    /**
     * Removes a reaction from the structure. If the reaction is not present,
     * nothing is done and an Exception is thrown.
     * 
     * @param reaction
     *            the reaction to be removed
     */
    void removeReaction(Actionable<T> reaction);

    /**
     * Notifies the structure that the reaction r has changed. The whole
     * structure will be rearranged to ensure consistency.
     * 
     * @param reaction
     *            the reaction which has changed
     */
    void updateReaction(Actionable<T> reaction);

}
