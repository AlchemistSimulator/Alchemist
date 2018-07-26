/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.expressions.interfaces.ITreeNode;
import org.apache.commons.math3.random.RandomGenerator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.implementations.molecules.LsaMolecule;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.ILsaNode;
import it.unibo.alchemist.model.interfaces.MapEnvironment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Route;

import java.util.List;

/**
 */
public class LsaRandomNeighborAction extends LsaStandardAction {
    private static final long serialVersionUID = -7128058274012426458L;
    private final Environment<List<ILsaMolecule>, ?> env;
    private final MapEnvironment<List<ILsaMolecule>> menv;
    private final boolean initO, initD, initNeigh, initRoute, mapEnv;
    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "All provided RandomGenerator implementations are actually Serializable")
    private final RandomGenerator randomEngine;

    /**
     * Builds a new action in a neighborhood. es: +<id,X,n> This class extend
     * LsaAbstractAction.
     * 
     * 
     * @param environment
     *            The environment to use
     * @param node
     *            The source node
     * @param molecule
     *            The IlsaMolecule instance you want to add to neighbor lsa
     *            space.
     * @param random
     *            the random engine
     * 
     */
    @SuppressWarnings("unchecked")
    public LsaRandomNeighborAction(final ILsaNode node, final ILsaMolecule molecule, final Environment<List<ILsaMolecule>, ?> environment, final RandomGenerator random) {
        super(molecule, node);
        final String molString = molecule.toString();
        initO = molString.contains(LsaMolecule.SYN_O);
        initD = molString.contains(LsaMolecule.SYN_D);
        initNeigh = molString.contains(LsaMolecule.SYN_NEIGH);
        initRoute = molString.contains(LsaMolecule.SYN_ROUTE);
        env = environment;
        mapEnv = environment instanceof MapEnvironment;
        menv = mapEnv ? (MapEnvironment<List<ILsaMolecule>>) env : null;
        randomEngine = random;
    }

    @Override
    public LsaRandomNeighborAction cloneAction(final Node<List<ILsaMolecule>> n, final Reaction<List<ILsaMolecule>> r) {
        return new LsaRandomNeighborAction((ILsaNode) n, getMolecule(), getEnvironment(), randomEngine);
    }

    @Override
    public void execute() {
        final List<ILsaNode> nodes = getNodes();
        if (!nodes.isEmpty()) {
            final ITreeNode<?> nodeId = getMatches().get(LsaMolecule.SYN_SELECTED);
            if (nodeId == null) {
                /*
                 * Just choose a random neighbor among those valid
                 */
                final ILsaNode node = nodes.get(Math.abs(randomEngine.nextInt() % nodes.size()));
                setSynthectics(node);
                setConcentration(node);
            } else {
                /*
                 * There was an operation that fixed a single neighbor
                 */
                final int id = ((Double) nodeId.getData()).intValue();
                for (final ILsaNode node : nodes) {
                    if (node.getId() == id) {
                        setSynthectics(node);
                        setConcentration(node);
                        return;
                    }
                }
                throw new IllegalStateException("there is probably a bug in " + getClass().getName() + "\nMatches: "
                        + getMatches() + "\nNodes: " + getNodes());
            }
        }
    }

    @Override
    public Context getContext() {
        return Context.NEIGHBORHOOD;
    }

    /**
     * @return the current environment
     */
    protected Environment<List<ILsaMolecule>, ?> getEnvironment() {
        return env;
    }

    /**
     * @return the current RandomGenerator
     */
    protected RandomGenerator getRandomGenerator() {
        return randomEngine;
    }

    /**
     * Sets the synthetic variables.
     * 
     * @param node
     *            the node to use as reference (e.g. for computing the distance)
     */
    protected void setSynthectics(final ILsaNode node) {
        /*
         * #D and #ROUTE
         */
        double d = initD || initRoute ? computeDistance(node) : Double.NaN;
        if (initD) {
            d = computeDistance(node);
            setSyntheticD(d);
        }
        if (initRoute) {
            if (mapEnv) {
                final Route<?> route = menv.computeRoute(getNode(), node);
                if (route != null) {
                    final double dist = route.length();
                    d = Math.max(d, dist);
                }
            }
            setSyntheticRoute(d);
        }
        /*
         * #NEIGH
         */
        if (initNeigh) {
            setSyntheticNeigh(env.getNeighborhood(node).getNeighbors());
        }
        /*
         * #O
         */
        if (initO) {
            setSyntheticO();
        }
    }

    private double computeDistance(final ILsaNode node) {
        return env.getDistanceBetweenNodes(getNode(), node);
    }

    @Override
    public String toString() {
        return "+" + getMolecule().toString();
    }

}
