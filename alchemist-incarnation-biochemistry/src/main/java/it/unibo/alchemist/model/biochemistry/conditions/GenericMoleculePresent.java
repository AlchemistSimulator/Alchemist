/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.conditions;

import it.unibo.alchemist.model.Context;
import it.unibo.alchemist.model.Molecule;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;
import it.unibo.alchemist.model.conditions.AbstractCondition;
import org.apache.commons.math3.util.CombinatoricsUtils;

import java.io.Serial;

/**
 * This class implements a condition which checks if a molecule is present or
 * not.
 *
 * @param <T> the concentration type
 */
public class GenericMoleculePresent<T extends Number> extends
    AbstractCondition<T> {

    @Serial
    private static final long serialVersionUID = -7400434133059391639L;
    private final Molecule molecule;
    private final T qty;

    /**
     * Builds a new condition, which checks if the molecule exists or not inside
     * the node n.
     *
     * @param mol the molecule whose presence should be checked
     * @param n the current node
     * @param quantity the number of molecules which should be present. Must be positive.
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return molecule.toString() + ">=" + qty;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GenericMoleculePresent<T> cloneCondition(final Node<T> newNode, final Reaction<T> newReaction) {
        return new GenericMoleculePresent<>(newNode, molecule, qty);
    }

    /**
     * Propensity influence is computed through the binomial coefficient. See
     * <a href="https://doi.org/10.1007/978-3-540-68894-5">
     * Bernardo, Degano, Zavattaro - Formal Methods for Computational Systems Biology
     * </a>.
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
     * Allows accessing the threshold.
     *
     * @return the current threshold
     */
    public T getQuantity() {
        return qty;
    }

    /**
     * Allows accessing the molecule.
     *
     * @return the current molecule
     */
    public Molecule getMolecule() {
        return molecule;
    }
}
