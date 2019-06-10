/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model;


import org.apache.commons.math3.random.RandomGenerator;

import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.implementations.nodes.CellNodeImpl;
import it.unibo.alchemist.model.implementations.reactions.BiochemicalReactionBuilder;
import it.unibo.alchemist.model.implementations.timedistributions.ExponentialTime;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.TimeDistribution;
import it.unibo.alchemist.model.interfaces.Action;
import it.unibo.alchemist.model.interfaces.Condition;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.CellNode;
import it.unibo.alchemist.model.interfaces.Incarnation;

/**
 * @param <P>
 */
public final class BiochemistryIncarnation<P extends Position<P>> implements Incarnation<Double, P> {

    @Override
    public double getProperty(final Node<Double> node, final Molecule mol, final String prop) {
        return node.getConcentration(mol);
    }

    @Override
    public Biomolecule createMolecule(final String s) {
        return new Biomolecule(s);
    }

    @Override
    public CellNode<P> createNode(final RandomGenerator rand, final Environment<Double, P> env, final String param) {
        if (param == null || param.isEmpty()) {
            return new CellNodeImpl<>(env);
        }
        return new CellNodeImpl<>(env, Double.parseDouble(param));
    }

    @Override
    public TimeDistribution<Double> createTimeDistribution(final RandomGenerator rand, final Environment<Double, P> env,
            final Node<Double> node, final String param) {
        if (param == null || param.isEmpty()) {
            return new ExponentialTime<>(1.0, rand);
        }
        try {
            final double rate = Double.parseDouble(param);
            return new ExponentialTime<>(rate, rand);
        } catch (NumberFormatException e) {
            return new ExponentialTime<>(1.0, rand);
        }
    }

    @Override
    public Reaction<Double> createReaction(final RandomGenerator rand, 
            final Environment<Double, P> env, 
            final Node<Double> node,
            final TimeDistribution<Double> time, 
            final String param) {
        return new BiochemicalReactionBuilder<>(this, node, env)
                .randomGenerator(rand)
                .timeDistribution(time)
                .program(param)
                .build();
    }

    @Override
    public Condition<Double> createCondition(final RandomGenerator rand, final Environment<Double, P> env, final Node<Double> node,
            final TimeDistribution<Double> time, final Reaction<Double> reaction, final String param) {
        return null;
    }

    @Override
    public Action<Double> createAction(final RandomGenerator rand, final Environment<Double, P> env, final Node<Double> node,
            final TimeDistribution<Double> time, final Reaction<Double> reaction, final String param) {
        return null;
    }

    @Override
    public Double createConcentration(final String s) {
        if (s == null) { // default value
            return 1d;
        }
        return Double.parseDouble(s);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
