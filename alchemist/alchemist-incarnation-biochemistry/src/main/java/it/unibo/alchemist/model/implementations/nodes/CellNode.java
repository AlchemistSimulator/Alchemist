/*
 * Copyright (C) 2010-2016, Danilo Pianini and contributors

 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import it.unibo.alchemist.model.implementations.molecules.Junction;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.ICellNode;
import it.unibo.alchemist.model.interfaces.Molecule;

/**
 *
 */
public class CellNode extends DoubleNode implements ICellNode {

    private static final long serialVersionUID = 837704874534888283L;

    private final List<Junction> junctionList = new ArrayList<>(0);

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

    /**
     * 
     * @return a unmodifiable list of junctions contained in this node.
     */
    public List<Junction> getJunctions() {
        return Collections.unmodifiableList(junctionList);
    }

    /**
     * Add a junction to the current node.
     * @param j the junction
     */
    public void addJunction(final Junction j) {
        junctionList.add(j);
    }

    /**
     * Return true if a junction is present in the current node, false otherwise.
     * Note: a junction is considered present if the method junction.equals(j) return true. 
     * The neighbor node should NOT be considered in this comparison, depend on the implementation of junction.
     * See {@link Junction#equals(Object)} for more details. 
     * 
     * @param j the junction
     * @return true if the junction is present, false otherwise.
     */
    public boolean containsJunction(final Junction j) {
        return junctionList.contains(j);
    }

    /**
     * Remove a junction from this node. If the junction is not present do nothing
     * @param j the junction to remove
     * @param neighbor the node at the other side.
     */
    public void removeJunction(final Junction j, final CellNode neighbor) {
        final Iterator<Junction> it = junctionList.iterator();
        for (int i = 0; it.hasNext(); i++) {
            final Junction jun = it.next();
            if (jun.equals(j) && jun.getNeighborNode().get().equals(neighbor)) {
                junctionList.remove(i);
                return;
            }
        }
    }

}
