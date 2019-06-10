/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.interfaces;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * @param <T>
 *            Concentration type
 * @param <P>
 *            Concentration type
 */
public interface Incarnation<T, P extends Position<? extends P>> {

    /**
     * Given an {@link Node}, an {@link Molecule} and a property expressed as
     * a {@link String}, returns a numeric value. If a numeric value is not
     * deducible, Double.NaN is returned.
     * 
     * @param node
     *            the node
     * @param mol
     *            the molecule to analyze
     * @param prop
     *            the property to extract
     * @return a numeric value representing the property
     */
    double getProperty(Node<T> node, Molecule mol, String prop);

    /**
     * Parses a {@link String}, and provides a {@link Molecule}.
     * 
     * @param s
     *            the {@link String} to parse
     * @return an {@link Molecule} created parsing the passed {@link String}
     */
    Molecule createMolecule(String s);

    /**
     * Creates a new concentration object of a specific concrete type.
     * 
     * @param s the {@link String} to parse
     * @return a concentration of a certain concrete type
     */
    T createConcentration(String s);

    /**
     * @param rand
     *            the random engine
     * @param env
     *            the environment that will host this object
     * @param param
     *            a {@link String} describing the object
     * @return a new {@link TimeDistribution}
     */
    Node<T> createNode(RandomGenerator rand, Environment<T, P> env, String param);

    /**
     * @param rand
     *            the random engine
     * @param env
     *            the environment that will host this object
     * @param node
     *            the node that will host this object
     * @param param
     *            a {@link String} describing the object
     * @return a new {@link TimeDistribution}
     */
    TimeDistribution<T> createTimeDistribution(RandomGenerator rand, Environment<T, P> env, Node<T> node, String param);

    /**
     * @param rand
     *            the random engine
     * @param env
     *            the environment that will host this object
     * @param node
     *            the node that will host this object
     * @param time
     *            the time distribution of the reaction
     * @param param
     *            a {@link String} describing the object
     * @return a new {@link Reaction}
     */
    Reaction<T> createReaction(RandomGenerator rand, Environment<T, P> env, Node<T> node, TimeDistribution<T> time, String param);

    /**
     * @param rand
     *            the random engine
     * @param env
     *            the environment that will host this object
     * @param node
     *            the node that will host this object
     * @param time
     *            the time distribution of the reaction
     * @param reaction
     *            the reaction hosting this object
     * @param param
     *            a {@link String} describing the object
     * @return a new {@link Condition}
     */
    Condition<T> createCondition(RandomGenerator rand, Environment<T, P> env, Node<T> node, TimeDistribution<T> time, Reaction<T> reaction, String param);

    /**
     * @param rand
     *            the random engine
     * @param env
     *            the environment that will host this object
     * @param node
     *            the node that will host this object
     * @param time
     *            the time distribution of the reaction
     * @param reaction
     *            the reaction hosting this object
     * @param param
     *            a {@link String} describing the object
     * @return a new {@link Action}
     */
    Action<T> createAction(RandomGenerator rand, Environment<T, P> env, Node<T> node, TimeDistribution<T> time, Reaction<T> reaction, String param);

}
