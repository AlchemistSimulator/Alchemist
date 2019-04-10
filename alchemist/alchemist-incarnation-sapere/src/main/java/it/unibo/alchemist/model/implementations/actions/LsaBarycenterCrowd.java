/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import org.apache.commons.math3.random.RandomGenerator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.implementations.molecules.LsaMolecule;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.ILsaNode;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;

import java.util.ArrayList;
import java.util.List;


/**
 */
public class LsaBarycenterCrowd<P extends Position<? extends P>> extends SAPEREMoveNodeAgent<P> {

    private static final double LIMIT = 0.1;
    private static final int MIN = 100;
    private static final long serialVersionUID = 1L;
    private final Double probMoving;
    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "All implementations are actually serializable")
    private final RandomGenerator random;

    /**
     * Behavior of agents that follow the gradient of the agent chosen to reach
     * the barycenter.
     * 
     * @param aEnvironment
     *            environment
     * @param node
     *            node
     * @param aRandom
     *            random value
     * @param p
     *            probability for an agent of follow the right direction
     */
    public LsaBarycenterCrowd(final Environment<List<ILsaMolecule>, P> aEnvironment, final ILsaNode node, final RandomGenerator aRandom, final Double p) {
        super(aEnvironment, node);
        random = aRandom;
        probMoving = p;
    }

    @Override
    public void execute() {
        final P mypos = getCurrentPosition();
        final double myx = mypos.getCartesianCoordinates()[0];
        final double myy = mypos.getCartesianCoordinates()[1];
        double x = 0;
        double y = 0;
        final Neighborhood<List<ILsaMolecule>> neigh = getLocalNeighborhood();
        final List<P> poss = new ArrayList<P>();
        double minBarycenterField = MIN;
        for (final Node<List<ILsaMolecule>> nodo : neigh.getNeighbors()) {
            final ILsaNode n = (ILsaNode) nodo;
            final P pos = getPosition(n);
            List<ILsaMolecule> barycenterList;
            try {
                barycenterList = n.getConcentration(new LsaMolecule("barycenter,V,T"));
            } catch (IndexOutOfBoundsException e) {
                barycenterList = null;
            }
            if (barycenterList != null && !barycenterList.isEmpty()) {
                double val, valMin = MIN;
                for (int i = 0; i < barycenterList.size(); i++) {
                    val = getLSAArgumentAsDouble(barycenterList.get(i), 1);
                    if (val < valMin) {
                        valMin = val;
                    }
                }
                if (valMin >= 0) {
                    final double barycenterConcentration = valMin;
                    if (barycenterConcentration == minBarycenterField) {
                        poss.add(pos);
                    } else if (barycenterConcentration < minBarycenterField) {
                        minBarycenterField = barycenterConcentration;
                        poss.clear();
                        poss.add(pos);
                    }
                }
            }

        }
        final double rnd = random.nextDouble();
        if (rnd < probMoving && !poss.isEmpty()) {
            final int intrnd = (int) (random.nextDouble() * (poss.size() - 1));
            x = poss.get(intrnd).getCartesianCoordinates()[0];
            y = poss.get(intrnd).getCartesianCoordinates()[1];
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

}
