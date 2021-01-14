/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.nodes;

import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.EnvironmentNode;
import it.unibo.alchemist.model.interfaces.Molecule;

/**
 *
 */
public final class EnvironmentNodeImpl extends DoubleNode implements EnvironmentNode {

    private static final long serialVersionUID = 1L;

    /**
     * Create a new environment node.
     * @param env the environment
     */
    public EnvironmentNodeImpl(final Environment<Double, ?> env) {
        super(env);
    }

    @Override
    public void setConcentration(final Molecule mol, final Double c) {
        if (c < 0) {
            throw new IllegalArgumentException("No negative concentrations allowed (" + mol + " -> " + c + ")");
        }
        if (c > 0) {
            super.setConcentration(mol, c);
        } else {
            removeConcentration(mol);
        }
    }
}
