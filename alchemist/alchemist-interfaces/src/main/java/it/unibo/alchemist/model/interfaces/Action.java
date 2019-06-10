/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.interfaces;

import java.io.Serializable;

import org.danilopianini.util.ListSet;

/**
 * @param <T>
 *            The type which describes the concentration of a molecule
 * 
 *            The interface of an action. Every action must implement this
 *            interface.
 * 
 */
public interface Action<T> extends Serializable {

    /**
     * This method allows to clone this action on a new node. It may result
     * useful to support runtime creation of nodes with the same reaction
     * programming, e.g. for morphogenesis.
     * 
     * @param n
     *            The node where to clone this {@link Action}
     * @param r
     *            The reaction to which the CURRENT action is assigned
     * @return the cloned action
     */
    Action<T> cloneAction(Node<T> n, Reaction<T> r);

    /**
     * Effectively executes this action.
     */
    void execute();

    /**
     * @return The context for this action.
     */
    Context getContext();

    /**
     * @return The list of the dependencies that this action generates.
     */
    ListSet<? extends Dependency> getOutboundDependencies();

}
