/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.core.implementations;

import it.unibo.alchemist.core.interfaces.DependencyHandler;
import it.unibo.alchemist.model.interfaces.Reaction;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


/**
 *         This class offers an implementation of ReactionHandler and it's
 *         suitable to build an IndexedPriorityQueue.
 * 
 * @param <T>
 */
public final class DependencyHandlerImpl<T> implements DependencyHandler<T> {

    private static final long serialVersionUID = 3442635555170492280L;
    private static final AtomicInteger SINGLETON = new AtomicInteger();
    private final int id = SINGLETON.getAndIncrement();
    private final Reaction<T> reaction;
    private List<DependencyHandler<T>> indeps = new LinkedList<>();
    private List<DependencyHandler<T>> outdeps = new LinkedList<>();

    /**
     * Builds a new ReactionHandler<T> given a reaction. This ReactionHandler<T>
     * comes with null parent, left and right and with 0 as number of children
     * both in left and right.
     * 
     * @param r
     *            the reaction to handle
     */
    public DependencyHandlerImpl(final Reaction<T> r) {
        reaction = r;
    }

    @Override
    public void addInDependency(final DependencyHandler<T> r) {
        indeps.add(r);
    }

    @Override
    public void addOutDependency(final DependencyHandler<T> r) {
        outdeps.add(r);
    }

    @Override
    public int compareTo(final DependencyHandler<T> o) {
        return reaction.getTau().compareTo(o.getReaction().getTau());
    }

    @Override
    public List<DependencyHandler<T>> dependsOn() {
        return indeps;
    }

    @Override
    public Reaction<T> getReaction() {
        return reaction;
    }

    @Override
    public List<DependencyHandler<T>> influences() {
        return outdeps;
    }

    @Override
    public List<DependencyHandler<T>> isInfluenced() {
        return indeps;
    }

    @Override
    public void removeInDependency(final DependencyHandler<T> rh) {
        indeps.remove(rh);
    }

    @Override
    public void removeOutDependency(final DependencyHandler<T> rh) {
        outdeps.remove(rh);
    }

    @Override
    public void setInDependencies(final List<DependencyHandler<T>> dep) {
        indeps = dep;
    }

    @Override
    public void setOutDependencies(final List<DependencyHandler<T>> dep) {
        outdeps = dep;
    }

    @Override
    public String toString() {
        return "Handling: " + reaction.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof DependencyHandlerImpl<?>) {
            return id == o.hashCode();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id;
    }

}
