/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
/*
 * Copyright (C) 2010-2016, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.conditions;

import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;

import org.apache.commons.math3.util.CombinatoricsUtils;

/**
 * This class implements a condition which checks if a molecule is present or
 * not.
 * @param <T> the concentration type
 */
public class GenericMoleculePresent<T extends Number> extends
        AbstractCondition<T> {

    private static final long serialVersionUID = -7400434133059391639L;
    private final Molecule molecule;
    private final T qty;

    /**
     * Builds a new condition, which checks if the molecule exists or not inside
     * the node n.
     * 
     * @param mol the molecule whose presence should be checked
     * @param n the current node
     * @param quantity the amount of molecules which should be present. Must be positive.
     */
    public GenericMoleculePresent(final Node<T> n, final Molecule mol, final T quantity) {
        super(n);
        if (quantity.doubleValue() <= 0d) {
            throw new IllegalArgumentException("The quantity of compound must be a positive number.");
        }
        molecule = mol;
        qty = quantity;
        declareDependencyOn(mol);
    }

    @Override
    public Context getContext() {
        return Context.LOCAL;
    }

    /**
     * @return true if the concentration of the molecule is higher or equal the
     *         value.
     */
    @Override
    public boolean isValid() {
        return getNode().getConcentration(molecule).doubleValue() >= qty
                .doubleValue();
    }

    @Override
    public String toString() {
        return molecule.toString() + ">=" + qty;
    }

    @Override
    public GenericMoleculePresent<T> cloneCondition(final Node<T> n, final Reaction<T> r) {
        return new GenericMoleculePresent<T>(n, molecule, qty);
    }

    /**
     * Propensity influence is computed through the binomial coefficient. See
     * Bernardo, Degano, Zavattaro - Formal Methods for Computational Systems
     * Biology for the formulae.
     * 
     * @return the propensity influence
     */
    @Override
    public double getPropensityContribution() {
        final int n = getNode().getConcentration(molecule).intValue();
        final int k = qty.intValue();
        if (k > n) {
            return 0;
        }
        return CombinatoricsUtils.binomialCoefficientDouble(n, k);
    }

    /**
     * Allows to access the threshold.
     * 
     * @return the current threshold
     */
    public T getQuantity() {
        return qty;
    }

    /**
     * Allows to access the molecule.
     * 
     * @return the current molecule
     */
    public Molecule getMolecule() {
        return molecule;
    }
}
