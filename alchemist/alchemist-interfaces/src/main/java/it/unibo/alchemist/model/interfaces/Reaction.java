/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.interfaces;

import java.io.Serializable;
import java.util.List;

import org.danilopianini.util.ListSet;

/**
 * @param <T>
 *            The type which describes the concentration of a molecule
 * 
 *            A generic reaction. Every reaction in the system must implement
 *            this interface.
 */
public interface Reaction<T> extends Comparable<Reaction<T>>, Serializable {

    /**
     * @return true if the reaction can be executed (namely, all the conditions
     *         are satisfied).
     */
    boolean canExecute();

    /**
     * This method allows to clone this reaction on a new node. It may result
     * useful to support runtime creation of nodes with the same reaction
     * programming, e.g. for morphogenesis.
     * 
     * @param n
     *            The node where to clone this Reaction
     * @param currentTime
     *            the time at which the clone is created (required to correctly clone the {@link TimeDistribution}s)
     * @return the cloned action
     */
    Reaction<T> cloneOnNewNode(Node<T> n, Time currentTime);

    /**
     * Executes the reactions.
     */
    void execute();

    /**
     * This method is called when the environment has completed its
     * initialization. Can be used by this reaction to compute its next
     * execution time - in case such computation requires an inspection of the
     * environment.
     * 
     * @param t
     *            the time at which the initialization of this reaction was
     *            accomplished
     * @param env
     *            the environment
     */
    void initializationComplete(Time t, Environment<T, ?> env);

    /**
     * @return The list of {@link Action}s of the {@link Reaction}. There is no
     *         specification if the list will be a copy of the internal list or
     *         a reference. It will depend on implementations. Please be careful
     *         when you modify this list.
     */
    List<Action<T>> getActions();

    /**
     * @return The list of {@link Condition}s of the {@link Reaction}. There is
     *         no specification if the list will be a copy of the internal list
     *         or a reference. It will depend on implementations. Please be
     *         careful when you modify this list.
     */
    List<Condition<T>> getConditions();

    /**
     * @return The list of {@link Dependency} whose concentration may change after the
     *         execution of this reaction.
     */
    ListSet<? extends Dependency> getOutboundDependencies();

    /**
     * @return The list of {@link Dependency}s whose concentration may affect the
     *         execution of the {@link Reaction}.
     */
    ListSet<? extends Dependency> getInboundDependencies();

    /**
     * @return The widest {@link Context} among {@link Condition}s, namely the
     *         smallest {@link Context} in which the {@link Reaction} can read
     *         informations.
     */
    Context getInputContext();

    /**
     * @return The {@link Node} in which this {@link Reaction} executes.
     */
    Node<T> getNode();

    /**
     * @return The widest {@link Context} among {@link Action}s, namely the
     *         smallest context in which the {@link Reaction} can do
     *         modifications.
     */
    Context getOutputContext();

    /**
     * Returns the speed of this {@link Reaction}. It is an average number, and
     * can potentially change during the simulation, depending on the
     * implementation.
     * 
     * @return the number of times this {@link Reaction} is triggered per time
     *         unit.
     */
    double getRate();

    /**
     * @return The global {@link Time} at which this reaction is scheduled to be
     *         executed
     */
    Time getTau();

    /**
     * @return the {@link TimeDistribution} for this {@link Reaction}
     */
    TimeDistribution<T> getTimeDistribution();

    /**
     * Sets the {@link Action}s list. Some implementations may not allow to
     * change it at runtime.
     * 
     * @param a
     *            the list of actions for this reaction
     */
    void setActions(List<Action<T>> a);

    /**
     * Sets the {@link Condition}s list. Some implementations may not allow to
     * change it at runtime.
     * 
     * @param c
     *            the list of conditions for this action
     */
    void setConditions(List<Condition<T>> c);

    /**
     * Updates the scheduling of this reaction.
     * 
     * @param executed
     *            true if the reaction have just been executed.
     * @param curTime
     *            the current {@link Time} of execution. This is mandatory in
     *            order to correctly compute the time shift of an
     *            already-scheduled reaction
     * @param env
     *            the current environment
     */
    void update(Time curTime, boolean executed, Environment<T, ?> env);

}
