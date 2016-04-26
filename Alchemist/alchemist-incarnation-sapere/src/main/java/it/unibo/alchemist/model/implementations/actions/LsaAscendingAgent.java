/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.implementations.molecules.LsaMolecule;
import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.ILsaNode;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;

import java.util.List;

/**
 */
public class LsaAscendingAgent extends SAPEREMoveNodeAgent {

    /*
     * an agent can move at most of LIMIT along each axis
     */
    private static final double LIMIT = 0.1;
    private static final long serialVersionUID = 228276533881360456L;
    private static final ILsaMolecule ACTIVE = new LsaMolecule("active");
    private final Reaction<List<? extends ILsaMolecule>> r;
    private final ILsaMolecule template;
    private final int gradDistPos;

    private boolean firstRun = true;
    private double startTimeSIMU;
    private long startTimeREAL;

    /**
     * @param reaction
     *            firing reaction
     * @param environment
     *            the current environment
     * @param node
     *            the current node
     * @param molecule
     *            the LSA to inspect once moving (typically a gradient)
     * @param pos
     *            the position in the LSA of the value to read for identifying
     *            the new position
     */
    public LsaAscendingAgent(final Reaction<List<? extends ILsaMolecule>> reaction,
            final Environment<List<? extends ILsaMolecule>> environment, final ILsaNode node,
            final LsaMolecule molecule, final int pos) {
        super(environment, node);
        this.r = reaction;
        this.template = molecule;
        this.gradDistPos = pos;
    }

    @Override
    public void execute() {
        double minGrad = Double.MAX_VALUE;

        final Position mypos = getCurrentPosition();
        final double myx = mypos.getCartesianCoordinates()[0];
        final double myy = mypos.getCartesianCoordinates()[1];
        double x = 0;
        double y = 0;
        final Neighborhood<List<? extends ILsaMolecule>> neigh = getLocalNeighborhood();
        Position targetPositions = null;
        Node<List<? extends ILsaMolecule>> bestNode = null;
        for (final Node<List<? extends ILsaMolecule>> node : neigh.getNeighbors()) {
            final ILsaNode n = (ILsaNode) node;
            final List<ILsaMolecule> gradList;
            gradList = n.getConcentration(template);
            if (!gradList.isEmpty()) {
                for (int i = 0; i < gradList.size(); i++) {
                    final double valueGrad = getLSAArgumentAsDouble(gradList.get(i), gradDistPos);
                    if (valueGrad <= minGrad) {
                        minGrad = valueGrad;
                        targetPositions = getPosition(n);
                        bestNode = n;
                    }
                }

            }
        }
        if (bestNode == null || bestNode.contains(ACTIVE)) {
            return;
        }

        if (targetPositions != null) {
            x = targetPositions.getCartesianCoordinates()[0];
            y = targetPositions.getCartesianCoordinates()[1];
            double dx = x - myx;
            double dy = y - myy;
            dx = dx > 0 ? Math.min(LIMIT, dx) : Math.max(-LIMIT, dx);
            dy = dy > 0 ? Math.min(LIMIT, dy) : Math.max(-LIMIT, dy);

            final boolean moveH = dx > 0 || dx < 0;
            final boolean moveV = dy > 0 || dy < 0;
            if (moveH || moveV) {
                move(new Continuous2DEuclidean(moveH ? dx : 0, moveV ? dy : 0));
            }
        }

    }

    /**
     * @return simulated time and real time at which agent reaches the source of
     *         gradient
     */
    protected double[] computeWalkedTime() {

        double[] walkedTime = new double[2];
        if (firstRun) {
            startTimeSIMU = r.getTau().toDouble();
            startTimeREAL = System.currentTimeMillis();
            firstRun = false;
        }
        walkedTime[0] = r.getTau().toDouble() - startTimeSIMU;
        walkedTime[1] = System.currentTimeMillis() - startTimeREAL;
        return walkedTime;
    }

}