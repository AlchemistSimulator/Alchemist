/*
 * Copyright (C) 2010-2016, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
/**
 * 
 */
package it.unibo.alchemist.model.implementations.conditions;

import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;

import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.commons.math3.util.FastMath;

/**
 * 
 * @param <T> the concentration type
 */
public class GenericMoleculePresentInNeighborhood<T extends Number> extends GenericMoleculePresent<T> {

    private static final long serialVersionUID = 5472803590433997104L;
    private final Environment<T> env;
    private final int intQty;

    /**
     * @param environment
     *            the environment
     * @param mol
     *            the molecule
     * @param n
     *            the node
     * @param quantity
     *            how many molecules
     */
    public GenericMoleculePresentInNeighborhood(final Environment<T> environment, final Molecule mol, final Node<T> n, final T quantity) {
        super(mol, n, quantity);
        this.env = environment;
        intQty = (int) FastMath.round(quantity.doubleValue());
    }

    @Override
    public GenericMoleculePresentInNeighborhood<T> cloneOnNewNode(final Node<T> n) {
        return new GenericMoleculePresentInNeighborhood<T>(env, getMolecule(), getNode(), getQuantity());
    }

    @Override
    public Context getContext() {
        return Context.NEIGHBORHOOD;
    }

    /**
     * @return the current environment
     */
    public Environment<T> getEnvironment() {
        return env;
    }

    @Override
    public double getPropensityConditioning() {
        double res = 0;
        for (final Node<T> n : env.getNeighborhood(getNode()).getNeighbors()) {
            res += CombinatoricsUtils.binomialCoefficientDouble(n.getConcentration(getMolecule()).intValue(), intQty);
        }
        return res;
    }

    /**
     * Always prefer {@link GenericMoleculePresent#getQuantity()} if your
     * computation does not strictly require integers.
     * 
     * @return a int version of the molecule quantity, to be used for internal
     *         computations.
     */
    protected int getIntQuantity() {
        return intQty;
    }

    @Override
    public boolean isValid() {
        double q = 0;
        for (final Node<T> n : env.getNeighborhood(getNode()).getNeighbors()) {
            q += n.getConcentration(getMolecule()).doubleValue();
        }
        return q >= getQuantity().doubleValue();
    }

    @Override
    public String toString() {
        return super.toString() + " in neighborhood";
    }

}
