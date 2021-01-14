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

/**
 * @param <T>
 *            The type of the concentration
 * 
 *            This interface must be implemented in every realization of node
 */
public interface Node<T> extends Serializable, Iterable<Reaction<T>>, Comparable<Node<T>> {

    /**
     * Adds a reaction to this node.
     * 
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
     * This method is usefult to know how many different chemical species may be
     * contained in this node.
     * 
     * @return the number of chemical species in this node
     */
    int getChemicalSpecies();

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

}
