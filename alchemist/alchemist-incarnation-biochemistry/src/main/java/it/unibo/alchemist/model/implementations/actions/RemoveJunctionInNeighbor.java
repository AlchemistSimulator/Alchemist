/*
 * Copyright (C) 2010-2016, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.interfaces.Context;

import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;

import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.implementations.molecules.Junction;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.CellNode;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 * Represent the action of removing a junction between a neighbor and the current node. <br/>
 * This action only remove the junction reference inside the neighbor node, the current one totally ignore 
 * that a junction has been removed. <br/>
 * This is a part of the junction remotion process. <br/>
 * See {@link RemoveJunctionInCell} for the other part of the process.
 */
public class RemoveJunctionInNeighbor extends AbstractNeighborAction<Double> {

    private static final long serialVersionUID = -5033532863301442377L;

    private final Junction jun;
    private final Environment<Double> env;

    /**
     * 
     * @param junction 
     * @param n 
     * @param e 
     * @param rg 
     */
    public RemoveJunctionInNeighbor(final Environment<Double> e, final CellNode n, final Junction junction, final RandomGenerator rg) {
        super(n, e, rg);
        addModifiedMolecule(junction);
        for (final Map.Entry<Biomolecule, Double> entry : junction.getMoleculesInCurrentNode().entrySet()) {
            addModifiedMolecule(entry.getKey());
        }
        jun = junction;
        env = e;
    }

    @Override
    public RemoveJunctionInNeighbor cloneOnNewNode(final Node<Double> n, final Reaction<Double> r) {
        return new RemoveJunctionInNeighbor(env, (CellNode) n, jun, getRandomGenerator());
    }

    /**
     * If no target node is given DO NOTHING. The junction can not be removed.
     */
    @Override
    public void execute() { }

    @Override
    public Context getContext() {
        return Context.NEIGHBORHOOD; // TODO try local
    }

    @Override
    public void execute(final Node<Double> targetNode) {
        ((CellNode) targetNode).removeJunction(jun, getNode());
    }

    @Override 
    public String toString() {
        return "remove junction " + jun.toString() + " in neighbor";
    }

    @Override
    public CellNode getNode() {
        return (CellNode) super.getNode();
    }

}
