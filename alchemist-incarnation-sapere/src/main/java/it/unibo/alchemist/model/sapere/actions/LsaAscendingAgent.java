/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.sapere.actions;

import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Neighborhood;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Position2D;
import it.unibo.alchemist.model.Reaction;
import it.unibo.alchemist.model.sapere.ILsaMolecule;
import it.unibo.alchemist.model.sapere.ILsaNode;
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule;

import java.io.Serial;
import java.util.List;

/**
 * @param <P> {@link it.unibo.alchemist.model.Position} type
 */
public final class LsaAscendingAgent<P extends Position2D<? extends P>> extends AbstractSAPEREMoveNodeAgent<P> {

    /*
     * an agent can move at most of LIMIT along each axis
     */
    private static final double LIMIT = 0.1;
    @Serial
    private static final long serialVersionUID = 228276533881360456L;
    private static final ILsaMolecule ACTIVE = new LsaMolecule("active");
    private final ILsaMolecule template;
    private final int gradDistPos;

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
    public LsaAscendingAgent(final Reaction<List<ILsaMolecule>> reaction,
            final Environment<List<ILsaMolecule>, P> environment, final ILsaNode node,
            final LsaMolecule molecule, final int pos) {
        super(environment, node);
        this.template = molecule;
        this.gradDistPos = pos;
    }

    @Override
    public void execute() {
        double minGrad = Double.MAX_VALUE;
        final Neighborhood<List<ILsaMolecule>> neigh = getLocalNeighborhood();
        P targetPositions = null;
        Node<List<ILsaMolecule>> bestNode = null;
        for (final Node<List<ILsaMolecule>> node : neigh.getNeighbors()) {
            final ILsaNode n = (ILsaNode) node;
            final List<ILsaMolecule> gradList;
            gradList = n.getConcentration(template);
            if (!gradList.isEmpty()) {
                for (final ILsaMolecule grad : gradList) {
                    final double valueGrad = getLSAArgumentAsDouble(grad, gradDistPos);
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
        final P mypos = getCurrentPosition();
        final double myx = mypos.getX();
        final double myy = mypos.getY();
        final double x = targetPositions.getX();
        final double y = targetPositions.getY();
        double dx = x - myx;
        double dy = y - myy;
        dx = dx > 0 ? Math.min(LIMIT, dx) : Math.max(-LIMIT, dx);
        dy = dy > 0 ? Math.min(LIMIT, dy) : Math.max(-LIMIT, dy);
        final boolean moveH = dx > 0 || dx < 0;
        final boolean moveV = dy > 0 || dy < 0;
        if (moveH || moveV) {
            move(getEnvironment().makePosition(moveH ? dx : 0, moveV ? dy : 0));
        }
    }

}
