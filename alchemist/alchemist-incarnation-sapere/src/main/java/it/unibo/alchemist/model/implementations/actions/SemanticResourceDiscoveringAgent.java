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

import it.unibo.alchemist.model.implementations.nodes.LsaNode;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.ILsaNode;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;


/**
 * 
 */
public class SemanticResourceDiscoveringAgent<P extends Position<P>> extends SAPERENeighborAgent<P> {

    private static final long serialVersionUID = 3931150573326003357L;
    private final ILsaMolecule grad;
    private final int graDistPos;
    private final ILsaMolecule resp;

    /**
     * @param env the environment
     * @param node the node
     * @param response the response
     * @param gradient the gradient
     * @param distArgPosition the distance argument position
     */
    public SemanticResourceDiscoveringAgent(final Environment<List<ILsaMolecule>, P> env, final ILsaNode node, final ILsaMolecule response, final ILsaMolecule gradient, final int distArgPosition) {
        super(env, node, response);
        resp = response;
        grad = gradient;
        graDistPos = distArgPosition;
    }

    @Override
    public void execute() {
        double minGrad = getLSAArgumentAsDouble(getNode().getConcentration(grad).get(0), graDistPos);
        final Neighborhood<List<ILsaMolecule>> neigh = getLocalNeighborhood();
        final List<LsaNode> targetPositions = new ArrayList<LsaNode>();

        for (final Node<List<ILsaMolecule>> node : neigh.getNeighbors()) {
            final LsaNode n = (LsaNode) node;
            final List<ILsaMolecule> gradList;
            gradList = n.getConcentration(grad);
            if (!gradList.isEmpty()) {
                for (int i = 0; i < gradList.size(); i++) {
                    final double valueGrad = getLSAArgumentAsDouble(gradList.get(i), graDistPos);
                    if (valueGrad <= minGrad) {
                        minGrad = valueGrad;
                        targetPositions.add(n);
                    }
                }

            }
        }
        if (!targetPositions.isEmpty()) {
            final P pd = getPosition(targetPositions.get(targetPositions.size() - 1));
            final double distance = getCurrentPosition().getDistanceTo(pd);
            final int pos = resp.size() - 1;
            final double olddistance = getLSAArgumentAsDouble(resp, pos);
            final double newdistance = distance + olddistance;
            final ILsaMolecule mol = setLSAArgument(resp, newdistance, pos);
            inject(targetPositions.get(targetPositions.size() - 1), mol);
        }
    }

}
