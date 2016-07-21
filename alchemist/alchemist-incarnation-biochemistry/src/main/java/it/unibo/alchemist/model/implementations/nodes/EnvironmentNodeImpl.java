/*
 * Copyright (C) 2010-2016, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.nodes;

import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.EnvironmentNode;

/**
 *
 */
public class EnvironmentNodeImpl extends DoubleNode implements EnvironmentNode {

    private static final long serialVersionUID = 1880007336956353256L;

    /**
     * Create a new environment node.
     * @param env the environment
     */
    public EnvironmentNodeImpl(final Environment<Double> env) {
        super(env);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected Double createT() {
        return 0d;
    }
}
