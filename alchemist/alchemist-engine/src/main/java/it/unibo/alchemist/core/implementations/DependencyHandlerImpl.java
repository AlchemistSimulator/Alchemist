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
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;


/**
 *         This class offers an implementation of ReactionHandler and it's
 *         suitable to build an IndexedPriorityQueue.
 * 
 * @param <T>
 */
public final class DependencyHandlerImpl<T> implements DependencyHandler<T> {

    private static final long serialVersionUID = 1L;
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
    public void addInbound(final DependencyHandler<T> r) {
        indeps.add(r);
    }

    @Override
    public void addOutbound(final DependencyHandler<T> r) {
        outdeps.add(r);
    }

    @Override
    public int compareTo(@NotNull final DependencyHandler<T> o) {
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
    public List<DependencyHandler<T>> inbound() {
        return outdeps;
    }

    @Override
    public List<DependencyHandler<T>> outbound() {
        return indeps;
    }

    @Override
    public void removeInbound(final DependencyHandler<T> rh) {
        indeps.remove(rh);
    }

    @Override
    public void removeOutbound(final DependencyHandler<T> rh) {
        outdeps.remove(rh);
    }

    @Override
    public String toString() {
        return "Handling: " + reaction.toString();
    }

}
