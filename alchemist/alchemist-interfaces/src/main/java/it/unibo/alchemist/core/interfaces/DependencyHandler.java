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
    void addInDependency(DependencyHandler<T> r);

    /**
     * Adds r to those reactions ifluenced by the execution of this reaction.
     * 
     * @param r
     *            the reaction
     */
    void addOutDependency(DependencyHandler<T> r);

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
     * Calculates the reactions which are influenced by this one.
     * 
     * @return a list handlers of those reaction whose times needs to be updated
     *         after the execution of the current reaction.
     */
    List<DependencyHandler<T>> influences();

    /**
     * Calculates the reactions which influence by this one.
     * 
     * @return a list handlers of those reaction whose execution implies a time
     *         update for this reaction.
     */
    List<DependencyHandler<T>> isInfluenced();

    /**
     * Removes an in dependency. If the dependency was not present, this method
     * has no effect.
     * 
     * @param rh
     *            the reaction to be removed from the list
     */
    void removeInDependency(DependencyHandler<T> rh);

    /**
     * Removes an out dependency. If the dependency was not present, this method
     * has no effect.
     * 
     * @param rh
     *            the reaction to be removed from the list
     */
    void removeOutDependency(DependencyHandler<T> rh);

    /**
     * Allows to change the in dependencies for this handler. No soundness is
     * guaranteed for this change. Be careful.
     * 
     * @param dep
     *            the list of the in dependencies.
     */
    void setInDependencies(List<DependencyHandler<T>> dep);

    /**
     * Allows to change the in dependencies for this handler. No soundness is
     * guaranteed for this change. Be careful.
     * 
     * @param dep
     *            the list of the out dependencies
     */
    void setOutDependencies(List<DependencyHandler<T>> dep);



}
