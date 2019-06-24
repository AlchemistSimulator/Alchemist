/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
import org.apache.commons.math3.random.RandomGenerator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.implementations.nodes.LsaNode;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.ILsaNode;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;


/**
 *
 * @param <P> position type
 */
public class LsaMASSAgent<P extends Position<P>> extends SAPEREMoveNodeAgent<P> {

    private static final double LIMIT = 0.4;
    private static final double RANGE = 0.5;
    private static final long serialVersionUID = -4274734253286882410L;
    private static final double STEP = 0.1;
    private final ILsaMolecule fieldMol, sensor;
    private final Double probMoving;
    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "All implementations are actually serializable")
    private final RandomGenerator rand;

    /**
     * @param env
     *            the environment
     * @param node
     *            the node
     * @param field
     *            the field to follow
     * @param isSensor
     *            sensors distinctive template
     * @param random
     *            random engine
     * @param p
     *            probability to move
     */
    public LsaMASSAgent(final Environment<List<ILsaMolecule>, P> env, final ILsaNode node, final ILsaMolecule field, final ILsaMolecule isSensor, final RandomGenerator random, final Double p) {
        super(env, node);
        this.fieldMol = field;
        this.sensor = isSensor;
        this.rand = random;
        this.probMoving = p;

    }

    /*
     * (non-Javadoc)
     * 
     * @see alice.alchemist.model.interfaces.Action#execute()
     */
    @Override
    public void execute() {
        final P mypos = getCurrentPosition();
        final double myx = mypos.getCartesianCoordinates()[0];
        final double myy = mypos.getCartesianCoordinates()[1];
        double x = 0;
        double y = 0;
        final Neighborhood<List<ILsaMolecule>> neigh = getLocalNeighborhood();
        boolean up = true, down = true, left = true, right = true;
        final List<P> poss = new ArrayList<>();
        double maxField = -1.0;

        for (final Node<List<ILsaMolecule>> nodo : neigh.getNeighbors()) {
            final LsaNode n = (LsaNode) nodo;
            if (up || down || left || right) {
                if (n.getConcentration(sensor).size() == 0) {
                    final P pos = getPosition(n);
                    if (pos.getDistanceTo(mypos) < RANGE) {
                        x = pos.getCartesianCoordinates()[0];
                        y = pos.getCartesianCoordinates()[1];
                        double xdist = myx - x;
                        xdist *= xdist;
                        double ydist = myy - y;
                        ydist *= ydist;
                        if (xdist > ydist) {
                            if (x > myx) {
                                right = false;
                            } else {
                                left = false;
                            }
                        } else {
                            if (y > myy) {
                                down = false;
                            } else {
                                up = false;
                            }
                        }
                    }
                } else {
                    List<ILsaMolecule> fieldRes;
                    try {
                        fieldRes = n.getConcentration(fieldMol);
                    } catch (IndexOutOfBoundsException e) {
                        fieldRes = null;
                    }
                    if (fieldRes != null && !fieldRes.isEmpty()) {
                        double val, valMax = 0;
                        for (int i = 0; i < fieldRes.size(); i++) {
                            val = (Double) fieldRes.get(i).getArg(2).calculate(null).getValue(null);
                            if (val > valMax) {
                                valMax = val;
                            }
                        }

                        if (valMax > 0) {
                            final double fieldConcentration = valMax;
                            if (fieldConcentration == maxField) {
                                poss.add(getPosition(n));
                            } else if (fieldConcentration > maxField) {
                                maxField = fieldConcentration;
                                poss.clear();
                                poss.add(getPosition(n));

                            }
                        }
                    }
                }
            } else {
                break;
            }
        }

        if (up || down || left || right) {
            final double rnd = rand.nextDouble();
            if (rnd < probMoving && !poss.isEmpty()) {
                final int intrnd = (int) (rand.nextDouble() * (poss.size() - 1));
                x = poss.get(intrnd).getCartesianCoordinates()[0];
                y = poss.get(intrnd).getCartesianCoordinates()[1];
                double dx = x - myx;
                double dy = y - myy;
                dx = dx > 0 ? Math.min(LIMIT, dx) : Math.max(-LIMIT, dx);
                dy = dy > 0 ? Math.min(LIMIT, dy) : Math.max(-LIMIT, dy);
                final boolean moveH = dx > 0 && right || dx < 0 && left;
                final boolean moveV = dy > 0 && down || dy < 0 && up;
                if (moveH || moveV) {
                    move(getEnvironment().makePosition(moveH ? dx : 0, moveV ? dy : 0));
                }
            } else {
                double u = 0, d = 0, ri = 0, le = 0, ud = 0, lr = 0;
                if (up) {
                    u = rand.nextDouble() * LIMIT;
                }
                if (down) {
                    d = rand.nextDouble() * LIMIT;
                }
                if (left) {
                    le = rand.nextDouble() * LIMIT;
                }
                if (right) {
                    ri = rand.nextDouble() * LIMIT;
                }
                if ((u - d) > 0) {
                    ud = (u - d) + STEP;
                } else {
                    ud = (u - d) - STEP;
                }
                if ((le - ri) > 0) {
                    lr = (le - ri) + STEP;
                } else {
                    lr = (le - ri) - STEP;
                }
                move(getEnvironment().makePosition(ud, lr));
            }
        }

    }

}
