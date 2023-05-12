/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model;

import org.danilopianini.util.ListSet;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * @param <T>
 *            The type which describes the concentration of a molecule
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
     * @param node
     *            The node where to clone this {@link Action}
     * @param reaction
     *            The reaction to which the CURRENT action is assigned
     * @return the cloned action
     */
    Action<T> cloneAction(Node<T> node, Reaction<T> reaction);

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
    @Nonnull
    ListSet<? extends Dependency> getOutboundDependencies();

}
