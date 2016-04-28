/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Action;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 */
public class AddJunctionInNeighbor extends AbstractAction<Double> {

    private static final long serialVersionUID = 8670229402770243539L;

    /**
     * 
     * @param junName 
     * @param node 
     * @param env 
     */
    public AddJunctionInNeighbor(final String junName, final Node<Double> node, final Environment<Double> env) {
        super(node);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Action<Double> cloneOnNewNode(final Node<Double> n, final Reaction<Double> r) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void execute() {
        // TODO Auto-generated method stub

    }

    @Override
    public Context getContext() {
        return Context.LOCAL; // TODO this is just a stub
    }

}
