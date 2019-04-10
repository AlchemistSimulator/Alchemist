/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.expressions.implementations.AST;
import it.unibo.alchemist.expressions.implementations.Expression;
import it.unibo.alchemist.expressions.implementations.NumTreeNode;
import it.unibo.alchemist.expressions.interfaces.IExpression;
import it.unibo.alchemist.model.implementations.molecules.LsaMolecule;
import it.unibo.alchemist.model.implementations.nodes.LsaNode;
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
public final class LsaAscendingGradientDist<P extends Position<P>> extends SAPERENeighborAgent<P> {

    private static final long serialVersionUID = 7719580008466360029L;
    private static final ILsaMolecule MOLGRAD = new LsaMolecule("grad, req, Type, Distance, Time");
    private static final ILsaMolecule MOLRESPONSE = new LsaMolecule("response, Req, Ser, MD, D");
    private static final int POS = 3;
    private final Environment<List<ILsaMolecule>, ?> env;

    /**
     * @param environment environment
     * @param node node
     */
    public LsaAscendingGradientDist(final Environment<List<ILsaMolecule>, P> environment, final ILsaNode node) {
        super(environment, node, MOLRESPONSE);
        this.env = environment;
    }

    @Override
    public void execute() {
        double minGrad = getLSAArgumentAsDouble(getNode().getConcentration(MOLGRAD).get(0), POS);
        final Neighborhood<List<ILsaMolecule>> neigh = env.getNeighborhood(getNode());
        final List<LsaNode> targetPositions = new ArrayList<LsaNode>();
        for (final Node<List<ILsaMolecule>> node : neigh.getNeighbors()) {
            final LsaNode n = (LsaNode) node;
            final List<ILsaMolecule> gradList;
            gradList = n.getConcentration(MOLGRAD);
            if (!gradList.isEmpty()) {
                for (int i = 0; i < gradList.size(); i++) {
                    final double valueGrad = (Double) gradList.get(i).getArg(POS).calculate(null).getValue(null);
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
            final List<IExpression> l = MOLRESPONSE.allocateVar(getMatches());
            final IExpression d = l.remove(l.size() - 1);
            final double olddistance = (Double) d.getRootNodeData();
            final double newdistance = distance + olddistance;
            l.add(new Expression(new AST(new NumTreeNode(newdistance))));
            final LsaMolecule mol = new LsaMolecule(l);
            (targetPositions.get(targetPositions.size() - 1)).setConcentration(mol);
        }

    }

}
