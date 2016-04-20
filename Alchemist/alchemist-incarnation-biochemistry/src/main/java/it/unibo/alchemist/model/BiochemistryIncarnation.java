/*
 * Copyright (C) 2010-2016, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.math3.random.RandomGenerator;

import it.unibo.alchemist.model.implementations.actions.ChangeBiomolConcentrationInCell;
import it.unibo.alchemist.model.implementations.conditions.BiomolPresentInCell;
import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.implementations.nodes.CellNode;
import it.unibo.alchemist.model.implementations.reactions.ChemicalReaction;
import it.unibo.alchemist.model.implementations.timedistributions.ExponentialTime;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.TimeDistribution;
import it.unibo.alchemist.model.interfaces.Action;
import it.unibo.alchemist.model.interfaces.Condition;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Incarnation;

/**
 */
public class BiochemistryIncarnation implements Incarnation<Double> {

    @Override
    public double getProperty(final Node<Double> node, final Molecule mol, final String prop) {
        return (Double) node.getConcentration(mol);
    }

    @Override
    public Biomolecule createMolecule(final String s) {
        return new Biomolecule(s);
    }

    @Override
    public Node<Double> createNode(final RandomGenerator rand, final Environment<Double> env, final String param) {
        // TODO it is just a test
        final CellNode n = new CellNode(env);
        n.setConcentration(new Biomolecule("H2O"), 1000d);
        return n;
    }

    @Override
    public TimeDistribution<Double> createTimeDistribution(final RandomGenerator rand, final Environment<Double> env,
            final Node<Double> node, final String param) {
        try {
            final double rate = Double.parseDouble(param);
            return new ExponentialTime<>(rate, rand);
        } catch (NumberFormatException e) { // TODO exponential time with rate 1?
            return new ExponentialTime<>(1.0, rand);
        }
    }

    @Override
    public Reaction<Double> createReaction(final RandomGenerator rand, final Environment<Double> env, final Node<Double> node,
            final TimeDistribution<Double> time, final String param) {
        final ChemicalReaction<Double> reaction = new ChemicalReaction<>(node, time);
        final Biomolecule h2o = new Biomolecule("H2O");
        final Biomolecule h3o = new Biomolecule("H3O");
        final Biomolecule oh = new Biomolecule("OH");
        final double delta = 2.0;
        // set the conditions (molecule a is present)
        if (param.replaceAll("\n+", "").equals("w->ions")) {
            reaction.setConditions(new ArrayList<>(Arrays.asList(new BiomolPresentInCell(h2o, delta, (CellNode) node))));
            reaction.setActions(new ArrayList<>(Arrays.asList(
                    new ChangeBiomolConcentrationInCell(h3o, 1.0, (CellNode) node),
                    new ChangeBiomolConcentrationInCell(oh, 1.0, (CellNode) node),
                    new ChangeBiomolConcentrationInCell(h2o, -delta, (CellNode) node))));
        } else {
            reaction.setConditions(new ArrayList<>(Arrays.asList(
                    new BiomolPresentInCell(oh, 1.0, (CellNode) node), 
                    new BiomolPresentInCell(h3o, 1.0, (CellNode) node))));
            reaction.setActions(new ArrayList<>(Arrays.asList(
                    new ChangeBiomolConcentrationInCell(h3o, -1.0, (CellNode) node),
                    new ChangeBiomolConcentrationInCell(oh, -1.0, (CellNode) node),
                    new ChangeBiomolConcentrationInCell(h2o, delta, (CellNode) node))));
        }
        // TODO (is just a test method)
        return reaction;
    }

    @Override
    public Condition<Double> createCondition(final RandomGenerator rand, final Environment<Double> env, final Node<Double> node,
            final TimeDistribution<Double> time, final Reaction<Double> reaction, final String param) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Action<Double> createAction(final RandomGenerator rand, final Environment<Double> env, final Node<Double> node,
            final TimeDistribution<Double> time, final Reaction<Double> reaction, final String param) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double createConcentration(final String s) {
        if (s == null) {
            throw new IllegalArgumentException("The concentration must be a number");
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
