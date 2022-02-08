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
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @param <T>
 *            The type of the concentration
 * 
 *            This interface must be implemented in every realization of node
 */
public interface Node<T> extends Serializable, Iterable<Reaction<T>>, Comparable<Node<T>> {

    /**
     * Adds a reaction to this node.
     * The reaction is added only in the node,
     * but not in the {@link Simulation} scheduler, so it will never be executed.
     * To add the reaction also in the scheduler (and start to execute it),
     * you have to call also the method {@link Simulation#reactionAdded(Reaction)}.
     * @param r
     *            the reaction to be added
     */
    void addReaction(Reaction<T> r);

    /**
     * Creates a new Node which is a clone of the current Node. The new Node
     * will have all the current Node's properties, such as reactions and
     * molecules, but it will also have a different ID.
     * 
     * @param currentTime
     *            the time at which the cloning operation happens
     * 
     * @return A new Node which is a clone of the current one.
     * 
     * @throws UnsupportedOperationException
     *             if the implementation does not support node cloning.
     */
    Node<T> cloneNode(Time currentTime);

    /**
     * Tests whether a node contains a {@link Molecule}.
     * 
     * @param mol
     *            the molecule to check
     * @return true if the molecule is present, false otherwise
     */
    boolean contains(Molecule mol);

    /**
     * Calculates the concentration of a molecule.
     * 
     * @param mol
     *            the molecule whose concentration will be returned
     * @return the concentration of the molecule
     */
    T getConcentration(Molecule mol);

    /**
     * @return the molecule corresponding to the i-th position
     */
    Map<Molecule, T> getContents();

    /**
     * @return an univocal id for this node in the environment
     */
    int getId();

    /**
     * @return the count of different molecules in this node
     */
    int getMoleculeCount();

    /**
     * This method allows to access all the reaction of the node.
     * 
     * @return the list of rections belonging to this node
     */
    List<Reaction<T>> getReactions();

    @Override
    int hashCode();

    /**
     * @param mol the molecule that should be removed
     */
    void removeConcentration(Molecule mol);

    /**
     * Removes a reaction from this node.
     * The reaction is removed only in the node,
     * but not in the {@link Simulation} scheduler,
     * so the scheduler will continue to execute the reaction.
     * To remove the reaction also in the scheduler (and stop to execute it),
     * you have to call also the method {@link Simulation#reactionRemoved(Reaction)}.
     * 
     * @param r
     *            the reaction to be removed
     */
    void removeReaction(Reaction<T> r);

    /**
     * Sets the concentration of mol to c.
     * 
     * @param mol
     *            the molecule you want to set the concentration
     * @param c
     *            the concentration you want for mol
     */
    void setConcentration(Molecule mol, T c);

    /**
     * Adds a capability to the node.
     * @param capability the capability you want to add to the node
     */
    void addCapability(Capability capability);

    /**
     * @param type the type of capability to retrieve
     * @return an optional with a capability of the requested type
     */
    Optional<Capability> asCapability(Class<? extends Capability> type);

    /**
     * @return a set of the capabilities added to the node
     */
    Set<Capability> getCapabilities();

}
