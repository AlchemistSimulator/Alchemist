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
import org.danilopianini.lang.util.FasterString;

import java.util.List;

/**
 * 
 */
public class CrowdSteeringService extends SAPEREMoveNodeAgent {

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
    public CrowdSteeringService(final Environment<List<? extends ILsaMolecule>> environment, final ILsaNode node, final LsaMolecule molecule, final int idPos, final int distPos) {
        super(environment, node);
        addModifiedMolecule(GRADID);
        addModifiedMolecule(FB);
        addModifiedMolecule(P);
        addModifiedMolecule(GO);
        this.template = molecule;
        this.gradDistPos = distPos;
        this.gradIdPos = idPos;
    }

    @Override
    public void execute() {
        double minGrad = Double.MAX_VALUE;
        final FasterString idValue = getNode().getConcentration(GRADID).get(0).getArg(1).getAST().toFasterString();
        final Neighborhood<List<? extends ILsaMolecule>> neigh = getLocalNeighborhood();
        Position targetPositions = null;
        Node<List<? extends ILsaMolecule>> bestNode = null;
        for (final Node<List<? extends ILsaMolecule>> node : neigh.getNeighbors()) {
            final ILsaNode n = (ILsaNode) node;
            final List<ILsaMolecule> gradList;
            gradList = n.getConcentration(template);
            if (!gradList.isEmpty() && !n.contains(new LsaMolecule("person"))) {
                for (int i = 0; i < gradList.size(); i++) {
                    if (gradList.get(i).getArg(gradIdPos).getAST().toFasterString().equals(idValue)) {
                        final double valueGrad = getLSAArgumentAsDouble(gradList.get(i), gradDistPos);
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
        final Position mypos = getCurrentPosition();
        final double myx = mypos.getCartesianCoordinates()[0];
        final double myy = mypos.getCartesianCoordinates()[1];
        double x = 0;
        double y = 0;
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