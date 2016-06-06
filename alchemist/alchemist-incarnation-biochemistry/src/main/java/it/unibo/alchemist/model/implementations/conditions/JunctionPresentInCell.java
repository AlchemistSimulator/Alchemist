/*
 * Copyright (C) 2010-2016, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.conditions;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import it.unibo.alchemist.model.implementations.molecules.Junction;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.ICellNode;
import it.unibo.alchemist.model.interfaces.Node;

/**
 */
public class JunctionPresentInCell extends AbstractNeighborCondition<Double> {

    private static final long serialVersionUID = 4213307452790768059L;

    private final ICellNode node;
    private final Junction j;
    private final Environment<Double> env;

    /**
     * 
     * @param junction the junction
     * @param n the node
     * @param e the environment
     */
    public JunctionPresentInCell(final Junction junction, final ICellNode n, final Environment<Double> e) {
        super(n, e);
        addReadMolecule(junction);
        j = junction;
        node = n;
        env = e;
    }

    @Override
    public double getPropensityConditioning() {
        return isValid() ? 1 : 0;
    }

    @Override
    public boolean isValid() {
        return node.containsJunction(j);
    }

    @Override
    public JunctionPresentInCell cloneOnNewNode(final Node<Double> n) {
        return new JunctionPresentInCell(j, (ICellNode) n, env);
    }

    @Override
    public Map<Node<Double>, Double> getValidNeighbors(final Collection<? extends Node<Double>> neighborhood) {
        final Set<ICellNode> linkedNodes = node.getNeighborsLinkWithJunction(j);
        return neighborhood.stream().filter(n -> linkedNodes.contains(n))
        .collect(Collectors.<Node<Double>, Node<Double>, Double>toMap(
                n -> n,
                n -> 1.0));
    }

    @Override
    public String toString() {
        return "junction " +  j.toString() + " present ";
    }

}
