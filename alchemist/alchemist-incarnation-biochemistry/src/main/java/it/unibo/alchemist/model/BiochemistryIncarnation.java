/*
 * Copyright (C) 2010-2016, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model;


import org.apache.commons.math3.random.RandomGenerator;

import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.implementations.nodes.CellNode;
import it.unibo.alchemist.model.implementations.reactions.BiochemicalReactionBuilder;
import it.unibo.alchemist.model.implementations.timedistributions.ExponentialTime;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.TimeDistribution;
import it.unibo.alchemist.model.interfaces.Action;
import it.unibo.alchemist.model.interfaces.Condition;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.ICellNode;
import it.unibo.alchemist.model.interfaces.Incarnation;

/**
 */
public class BiochemistryIncarnation implements Incarnation<Double> {

    @Override
    public double getProperty(final Node<Double> node, final Molecule mol, final String prop) {
        return node.getConcentration(mol);
    }

    @Override
    public Biomolecule createMolecule(final String s) {
        return new Biomolecule(s);
    }

    @Override
    public ICellNode createNode(final RandomGenerator rand, final Environment<Double> env, final String param) {
        return new CellNode(env);
    }

    @Override
    public TimeDistribution<Double> createTimeDistribution(final RandomGenerator rand, final Environment<Double> env,
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
            final Environment<Double> env, 
            final Node<Double> node,
            final TimeDistribution<Double> time, 
            final String param) {
        return new BiochemicalReactionBuilder(this, (CellNode) node, env)
                .randomGenerator(rand)
                .timeDistribution(time)
                .program(param)
                .build();
    }

    @Override
    public Condition<Double> createCondition(final RandomGenerator rand, final Environment<Double> env, final Node<Double> node,
            final TimeDistribution<Double> time, final Reaction<Double> reaction, final String param) {
        return null;
    }

    @Override
    public Action<Double> createAction(final RandomGenerator rand, final Environment<Double> env, final Node<Double> node,
            final TimeDistribution<Double> time, final Reaction<Double> reaction, final String param) {
        return null;
    }

    @Override
    public Double createConcentration(final String s) {
        if (s == null) { // default value
            return 1d;
        }
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The concentration must be a number");
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
