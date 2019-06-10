/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.implementations.molecules.LsaMolecule;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.ILsaNode;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position2D;
import org.danilopianini.lang.HashString;

import java.util.List;

/**
 * @param <P>
 */
public final class CrowdSteeringService<P extends Position2D<P>> extends SAPEREMoveNodeAgent<P> {

    /*
     * an agent can move at most of LIMIT along each axis
     */
    private static final double LIMIT = 0.1;
    private static final long serialVersionUID = 228276533881360456L;
    private static final ILsaMolecule GRADID = new LsaMolecule("gradId, GRADID");
    private static final ILsaMolecule FB = new LsaMolecule("feedback, ID");
    private static final ILsaMolecule P = new LsaMolecule("person");
    private static final ILsaMolecule GO = new LsaMolecule("go");
    private final ILsaMolecule template;
    private final int gradDistPos;
    private final int gradIdPos;


    /**
     * @param environment
     *            the current environment
     * @param node
     *            the current node
     * @param molecule final LsaMolecule molecule, 
     *            the LSA to inspect once moving (typically a gradient)
     * @param idPos
     *            the position in the LSA of the value to read for identifying
     *            the gradient to consider
     * @param distPos
     *               the position in the LSA of the distance value 
     *               to be read for identifying the direction of movement
     */
    public CrowdSteeringService(final Environment<List<ILsaMolecule>, P> environment, final ILsaNode node, final LsaMolecule molecule, final int idPos, final int distPos) {
        super(environment, node);
        declareDependencyTo(GRADID);
        declareDependencyTo(FB);
        declareDependencyTo(P);
        declareDependencyTo(GO);
        this.template = molecule;
        this.gradDistPos = distPos;
        this.gradIdPos = idPos;
    }

    @Override
    public void execute() {
        double minGrad = Double.MAX_VALUE;
        final HashString idValue = getNode().getConcentration(GRADID).get(0).getArg(1).getAST().toHashString();
        final Neighborhood<List<ILsaMolecule>> neigh = getLocalNeighborhood();
        P targetPositions = null;
        Node<List<ILsaMolecule>> bestNode = null;
        for (final Node<List<ILsaMolecule>> node : neigh.getNeighbors()) {
            final ILsaNode n = (ILsaNode) node;
            final List<ILsaMolecule> gradList;
            gradList = n.getConcentration(template);
            if (!gradList.isEmpty() && !n.contains(new LsaMolecule("person"))) {
                for (ILsaMolecule aGradList : gradList) {
                    if (aGradList.getArg(gradIdPos).getAST().toHashString().equals(idValue)) {
                        final double valueGrad = getLSAArgumentAsDouble(aGradList, gradDistPos);
                        if (valueGrad <= minGrad) {
                            minGrad = valueGrad;
                            targetPositions = getPosition(n);
                            bestNode = n;
                        }
                    }
                }

            }
        }
        if (bestNode == null) {
            return;
        }
        if (targetPositions != null) {
            final P mypos = getCurrentPosition();
            final double myx = mypos.getX();
            final double myy = mypos.getY();
            double x = targetPositions.getX();
            double y = targetPositions.getY();
            double dx = x - myx;
            double dy = y - myy;
            dx = dx > 0 ? Math.min(LIMIT, dx) : Math.max(-LIMIT, dx);
            dy = dy > 0 ? Math.min(LIMIT, dy) : Math.max(-LIMIT, dy);
            final boolean moveH = dx > 0 || dx < 0;
            final boolean moveV = dy > 0 || dy < 0;
            if (moveH || moveV) {
                move(getEnvironment().makePosition(moveH ? dx : 0, moveV ? dy : 0));
            } else {
                if (minGrad != 0) {
                    getNode().setConcentration(GO);
                }
                getNode().setConcentration(new LsaMolecule("feedback, " + idValue));
                getNode().removeConcentration(GRADID);
                if (minGrad == 0) {
                    getNode().removeConcentration(P);
                }
            }
        }

    }
}
