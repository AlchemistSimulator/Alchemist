/*
 * Copyright (C) 2010-2016, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions;

import org.apache.commons.math3.random.RandomGenerator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.implementations.molecules.Junction;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.CellNode;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 * Represent the action of add a junction between the current node and a neighbor. <br/>
 * This action only create the junction reference inside this node, the neighbor totally ignore 
 * that a junction has been created. <br/>
 * This is a part of the junction creation process. <br/>
 * See {@link AddJunctionInNeighbor} for the other part of the process
 */
public class AddJunctionInCell extends AbstractNeighborAction<Double> {

    private static final long serialVersionUID = -7074995950043793067L;

    private final Junction jun;
    private final Environment<Double> env;
    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "All provided RandomGenerator implementations are actually Serializable")
    private final RandomGenerator rand;
    private final CellNode node;

    /**
     * @param j the junction
     * @param n the current node
     * @param e the current environment
     * @param rg the random generator
     */
    public AddJunctionInCell(final Environment<Double> e, final CellNode n, final Junction j, final RandomGenerator rg) {
        super(n, e, rg);
        addModifiedMolecule(j);
        jun = j;
        node = n;
        env = e;
        rand = rg;
    }

    @Override
    public AddJunctionInCell cloneOnNewNode(final Node<Double> n, final Reaction<Double> r) {
        return new AddJunctionInCell(env, (CellNode) n, jun, rand);
    }

    /**
     * If no target node is given DO NOTHING. The junction can not be created.
     * @throws UnsupportedOperationException if this method is called.
     */
    @Override
    public void execute() {
        throw new UnsupportedOperationException("A junction CAN NOT be created without a target node.");
    }

    @Override
    public Context getContext() {
        return Context.NEIGHBORHOOD; // TODO try with local
    }

    /**
     * Create the junction that links the node where this action is executed and the target node. 
     */
    @Override
    public void execute(final Node<Double> targetNode) { 
        node.addJunction(jun, (CellNode) targetNode);
    }

    @Override 
    public String toString() {
        return "add junction " + jun.toString() + " in node";
    }
}