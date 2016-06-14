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
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.ICellNode;

import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.implementations.molecules.Junction;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 * Represent the action of removing a junction between the current node and a neighbor. <br/>
 * This action only remove the junction reference inside this node, the neighbor totally ignore 
 * that a junction has been removed. <br/>
 * This is a part of the junction remotion process. <br/>
 * See {@link RemoveJunctionInNeighbor} for the other part of the process
 */
public class RemoveJunctionInCell extends AbstractNeighborAction<Double> {

    private static final long serialVersionUID = 3565077605882164314L;

    private final Junction jun;
    private final Environment<Double> env;
    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "All provided RandomGenerator implementations are actually Serializable")
    private final RandomGenerator rand;
    private final ICellNode node;

    /**
     * 
     * @param junction the junction
     * @param n the node where the action is performed
     * @param e the environment
     * @param rg the random generator
     */
    public RemoveJunctionInCell(final Junction junction, final ICellNode n, final Environment<Double> e, final RandomGenerator rg) {
        super(n, e, rg);
        addModifiedMolecule(junction);
        for (final Map.Entry<Biomolecule, Double> entry : junction.getMoleculesInCurrentNode().entrySet()) {
            addModifiedMolecule(entry.getKey());
        }
        jun = junction;
        node = n;
        env = e;
        rand = rg;
    }

    @Override
    public RemoveJunctionInCell cloneOnNewNode(final Node<Double> n, final Reaction<Double> r) {
        return new RemoveJunctionInCell(jun, (ICellNode) n, env, rand);
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

    /**
     * Removes the junction that links the node where this action is executed and the target node. 
     */
    @Override
    public void execute(final Node<Double> targetNode) { 
        if (!env.getNeighborhood(node).contains(targetNode)) {
            throw new IllegalStateException("Remove Junction in cell - current node " + node.getId() + " target node " + targetNode.getId());
        }
        node.removeJunction(jun, (ICellNode) targetNode);
    }

    @Override 
    public String toString() {
        return "remove junction " + jun.toString() + " in cell";
    }

}
