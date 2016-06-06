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

import org.apache.commons.math3.random.RandomGenerator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.implementations.molecules.Junction;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.ICellNode;
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
    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "All provided RandomGenerator implementations are actually Serializable")
    private final RandomGenerator rand;
    private final ICellNode node;

    /**
     * 
     * @param junction 
     * @param n 
     * @param e 
     * @param rg 
     */
    public RemoveJunctionInNeighbor(final Junction junction, final ICellNode n, final Environment<Double> e, final RandomGenerator rg) {
        super(n, e, rg);
        jun = junction;
        node = n;
        env = e;
        rand = rg;
    }

    @Override
    public RemoveJunctionInNeighbor cloneOnNewNode(final Node<Double> n, final Reaction<Double> r) {
        return new RemoveJunctionInNeighbor(jun, (ICellNode) n, env, rand);
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
        ((ICellNode) targetNode).removeJunction(jun, node);
    }
}
