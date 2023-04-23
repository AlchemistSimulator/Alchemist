/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.nodes;

import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.EnvironmentNode;
import it.unibo.alchemist.model.Incarnation;
import it.unibo.alchemist.model.Molecule;
import it.unibo.alchemist.model.nodes.GenericNode;

import javax.annotation.Nonnull;

/**
 * A node with non-negative concentration.
 */
public final class EnvironmentNodeImpl extends GenericNode<Double> implements EnvironmentNode {

    private static final long serialVersionUID = 1L;

    /**
     * Create a new environment node.
     * @param incarnation the simulation incarnation
     * @param environment the environment
     */
    public EnvironmentNodeImpl(final Incarnation<Double, ?> incarnation, final Environment<Double, ?> environment) {
        super(incarnation, environment);
    }

    /**
     * Create a new environment node.
     * @param environment the environment
     */
    public EnvironmentNodeImpl(final Environment<Double, ?> environment) {
        super(environment.getIncarnation(), environment);
    }

    @Override
    public void setConcentration(@Nonnull final Molecule molecule, @Nonnull final Double concentration) {
        if (concentration < 0) {
            throw new IllegalArgumentException(
                "No negative concentrations allowed (" + molecule + " -> " + concentration + ")"
            );
        }
        if (concentration > 0) {
            super.setConcentration(molecule, concentration);
        } else {
            removeConcentration(molecule);
        }
    }
}
