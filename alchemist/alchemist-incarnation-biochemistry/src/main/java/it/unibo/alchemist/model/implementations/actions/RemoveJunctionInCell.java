/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.CellNode;

import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;

import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.implementations.molecules.Junction;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 * Represent the action of removing a junction between the current node and a neighbor. <br/>
 * This action only remove the junction reference inside this node, the neighbor totally ignore 
 * that a junction has been removed. <br/>
 * This is a part of the junction removal process. <br/>
 * See {@link RemoveJunctionInNeighbor} for the other part of the process
 */
public final class RemoveJunctionInCell extends AbstractNeighborAction<Double> { // TODO try local

    private static final long serialVersionUID = 3565077605882164314L;

    private final Junction jun;
    private final Environment<Double, ?> env;

    /**
     * 
     * @param junction the junction
     * @param n the node where the action is performed
     * @param e the environment
     * @param rg the random generator
     */
    public RemoveJunctionInCell(final Environment<Double, ?> e, final Node<Double> n, final Junction junction, final RandomGenerator rg) {
        super(n, e, rg);
        if (n instanceof CellNode) {
            declareDependencyTo(junction);
            for (final Map.Entry<Biomolecule, Double> entry : junction.getMoleculesInCurrentNode().entrySet()) {
                declareDependencyTo(entry.getKey());
            }
            jun = junction;
            env = e;
        } else {
            throw new UnsupportedOperationException("This Action can be set only in CellNodes");
        }
    }

    @Override
    public RemoveJunctionInCell cloneAction(final Node<Double> n, final Reaction<Double> r) {
        return new RemoveJunctionInCell(env, n, jun, getRandomGenerator());
    }

    /**
     * If no target node is given DO NOTHING. The junction can not be removed.
     */
    @Override
    public void execute() { }

    /**
     * Removes the junction that links the node where this action is executed and the target node. 
     */
    @Override
    public void execute(final Node<Double> targetNode) { 
        if (targetNode instanceof CellNode) {
            getNode().removeJunction(jun, (CellNode<?>) targetNode);
        } else {
            throw new UnsupportedOperationException("Can't remove Junction in a node that it's not a CellNode");
        }
    }

    @Override 
    public String toString() {
        return "remove junction " + jun.toString() + " in cell";
    }

    @Override
    public CellNode<?> getNode() {
        return (CellNode<?>) super.getNode();
    }
}
