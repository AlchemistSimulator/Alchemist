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
package it.unibo.alchemist.model.implementations.conditions;

import java.util.Objects;

import it.unibo.alchemist.model.interfaces.Dependency;
import org.danilopianini.util.LinkedListSet;
import org.danilopianini.util.ListSet;

import it.unibo.alchemist.model.interfaces.Condition;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;


/**
 *
 * @param <T>
 */
public abstract class AbstractCondition<T> implements Condition<T> {

    private static final long serialVersionUID = -1610947908159507754L;
    private final ListSet<Dependency> influencing = new LinkedListSet<>();
    private final Node<T> n;

    /**
     * @param node the node this Condition belongs to
     */
    public AbstractCondition(final Node<T> node) {
        this.n = Objects.requireNonNull(node);
    }

    /**
     * {@inheritDoc}
     * 
     * How to override: if you intend your condition to be potentially changed by
     * any change in the context, return null.
     */
    @Override
    public final ListSet<? extends Dependency> getInboundDependencies() {
        return influencing;
    }

    /**
     * {@inheritDoc}
     * 
     * Override if your {@link Condition} can return a more specific type of node.
     * The typical way is to cast the call to super.getNode().
     */
    @Override
    public Node<T> getNode() {
        return n;
    }

    /**
     * @param m the molecule to add
     */
    protected final void declareDependencyOn(final Dependency m) {
        influencing.add(m);
    }

    /**
     * {@inheritDoc}
     * 
     * How to override: create a new action of your concrete subtype.
     */
    @Override
    public Condition<T> cloneCondition(final Node<T> n, final Reaction<T> r) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " has no support for cloning.");
    }

    /**
     *
     * @return the simple class name
     */
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
