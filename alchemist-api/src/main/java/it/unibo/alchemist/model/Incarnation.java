/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model;

import org.apache.commons.math3.random.RandomGenerator;

import javax.annotation.Nullable;

/**
 * @param <T>
 *            Concentration type
 * @param <P>
 *            Concentration type
 */
public interface Incarnation<T, P extends Position<? extends P>> {

    /**
     * Given a {@link Node}, a {@link Molecule} and a property expressed as
     * a {@link String}, returns a numeric value. If a numeric value is not
     * deducible, Double.NaN is returned.
     * 
     * @param node
     *            the node
     * @param molecule
     *            the molecule to analyze
     * @param property
     *            the property to extract
     * @return a numeric value representing the property
     */
    double getProperty(Node<T> node, Molecule molecule, String property);

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
     * Creates a new concentration object of a specific concrete type.
     *
     * @return a concentration of a certain concrete type
     */
    T createConcentration();

    /**
     * @param randomGenerator
     *            the random engine
     * @param environment
     *            the environment that will host this object
     * @param parameter
     *            a {@link String} describing the object
     * @return a new {@link Node}
     */
    Node<T> createNode(RandomGenerator randomGenerator, Environment<T, P> environment, @Nullable Object parameter);

    /**
     * @param randomGenerator
     *            the random engine
     * @param environment
     *            the environment that will host this object
     * @param node
     *            the node that will host this object. If it is `null` the related reaction
     *            will not belong to a {@link Node}
     * @param parameter
     *            a {@link String} describing the object
     * @return a new {@link TimeDistribution}
     */
    TimeDistribution<T> createTimeDistribution(
        RandomGenerator randomGenerator,
        Environment<T, P> environment,
        @Nullable Node<T> node,
        @Nullable Object parameter
    );

    /**
     * @param randomGenerator
     *            the random engine
     * @param environment
     *            the environment that will host this object
     * @param node
     *            the node that will host this object
     * @param timeDistribution
     *            the time distribution of the reaction
     * @param parameter
     *            a {@link String} describing the object
     * @return a new {@link Reaction}
     */
    Reaction<T> createReaction(
        RandomGenerator randomGenerator,
        Environment<T, P> environment,
        Node<T> node,
        TimeDistribution<T> timeDistribution,
        @Nullable Object parameter
    );

    /**
     * @param randomGenerator
     *            the random engine
     * @param environment
     *            the environment that will host this object
     * @param node
     *            the node that will host this object. If it is `null` the actionable
     *            will not belong to a {@link Node}
     * @param time
     *            the time distribution of the reaction
     * @param actionable
     *            the actionable hosting this object
     * @param additionalParameters
     *            a {@link String} describing the object
     * @return a new {@link Condition}
     */
    Condition<T> createCondition(
        RandomGenerator randomGenerator,
        Environment<T, P> environment,
        @Nullable Node<T> node,
        TimeDistribution<T> time,
        Actionable<T> actionable,
        @Nullable Object additionalParameters
    );

    /**
     * @param randomGenerator
     *            the random engine
     * @param environment
     *            the environment that will host this object
     * @param node
     *            the node that will host this object. If it is `null` the actionable
     *            will not belong to a {@link Node}
     * @param time
     *            the time distribution of the reaction
     * @param actionable
     *            the actionable hosting this object
     * @param additionalParameters
     *            a {@link String} describing the object
     * @return a new {@link Action}
     */
    Action<T> createAction(
        RandomGenerator randomGenerator,
        Environment<T, P> environment,
        @Nullable Node<T> node,
        TimeDistribution<T> time,
        Actionable<T> actionable,
        @Nullable Object additionalParameters
    );
}
