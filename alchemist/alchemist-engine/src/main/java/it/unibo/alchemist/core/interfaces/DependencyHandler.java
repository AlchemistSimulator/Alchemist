/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.core.interfaces;

import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.List;


/**
 * @param <T> Concentration type
 */
public interface DependencyHandler<T> extends Serializable, Comparable<DependencyHandler<T>> {

    /**
     * Adds r to those reactions whose execution implies an update of the times
     * of this reaction.
     * 
     * @param r
     *            the reaction
     */
    void addInbound(DependencyHandler<T> r);

    /**
     * Adds r to those reactions ifluenced by the execution of this reaction.
     * 
     * @param r
     *            the reaction
     */
    void addOutbound(DependencyHandler<T> r);

    @Override
    default int compareTo(@NotNull final DependencyHandler<T> o) {
        return getTau().compareTo(o.getTau());
    }

    /**
     * @return a list handlers of those reaction whose execution means an
     *         update. to this current reaction.
     */
    List<DependencyHandler<T>> dependsOn();

    /**
     * @return The reaction.
     */
    Reaction<T> getReaction();

    /**
     * @return The time at which the decorated reaction is scheduled
     */
    default Time getTau() {
        return getReaction().getTau();
    }

    /**
     * Calculates the reactions which are influenced by this one.
     * 
     * @return a list handlers of those reaction whose times needs to be updated
     *         after the execution of the current reaction.
     */
    List<DependencyHandler<T>> inbound();

    /**
     * Calculates the reactions which influence by this one.
     * 
     * @return a list handlers of those reaction whose execution implies a time
     *         update for this reaction.
     */
    List<DependencyHandler<T>> outbound();

    /**
     * Removes an in dependency. If the dependency was not present, this method
     * has no effect.
     * 
     * @param rh
     *            the reaction to be removed from the list
     */
    void removeInbound(DependencyHandler<T> rh);

    /**
     * Removes an out dependency. If the dependency was not present, this method
     * has no effect.
     * 
     * @param rh
     *            the reaction to be removed from the list
     */
    void removeOutbound(DependencyHandler<T> rh);

}
