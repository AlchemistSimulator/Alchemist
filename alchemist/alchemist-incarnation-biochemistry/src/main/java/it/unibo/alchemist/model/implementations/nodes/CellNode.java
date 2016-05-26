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
import it.unibo.alchemist.model.interfaces.ICellNode;
import it.unibo.alchemist.model.interfaces.Molecule;

/**
 *
 */
public class CellNode extends DoubleNode implements ICellNode {

    private static final long serialVersionUID = 837704874534888283L;

    /**
     * create a new cell node.
     * @param env the environment
     */
    public CellNode(final Environment<Double> env) {
        super(env);
    }

    @Override
    protected Double createT() {
        return 0d;
    }

    @Override
    public void setConcentration(final Molecule mol, final Double c) {
        if (c > 0) {
            super.setConcentration(mol, c);
        } else {
            removeConcentration(mol);
        }
    }

}
