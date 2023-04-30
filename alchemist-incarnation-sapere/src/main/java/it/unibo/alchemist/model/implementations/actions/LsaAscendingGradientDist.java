/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.sapere.dsl.impl.AST;
import it.unibo.alchemist.model.sapere.dsl.impl.Expression;
import it.unibo.alchemist.model.sapere.dsl.impl.NumTreeNode;
import it.unibo.alchemist.model.sapere.dsl.IExpression;
import it.unibo.alchemist.model.implementations.molecules.LsaMolecule;
import it.unibo.alchemist.model.implementations.nodes.LsaNode;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.ILsaNode;
import it.unibo.alchemist.model.Neighborhood;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Position;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @param <P> position type
 */
public final class LsaAscendingGradientDist<P extends Position<P>> extends SAPERENeighborAgent<P> {

    private static final long serialVersionUID = 7719580008466360029L;
    private static final ILsaMolecule MOLGRAD = new LsaMolecule("grad, req, Type, Distance, Time");
    private static final ILsaMolecule MOLRESPONSE = new LsaMolecule("response, Req, Ser, MD, D");
    private static final int POS = 3;
    private final Environment<List<ILsaMolecule>, ?> environment;

    /**
     * @param environment environment
     * @param node node
     */
    public LsaAscendingGradientDist(final Environment<List<ILsaMolecule>, P> environment, final ILsaNode node) {
        super(environment, node, MOLRESPONSE);
        this.environment = environment;
    }

    @Override
    public void execute() {
        double minGrad = getLSAArgumentAsDouble(getNode().getConcentration(MOLGRAD).get(0), POS);
        final Neighborhood<List<ILsaMolecule>> neigh = environment.getNeighborhood(getNode());
        final List<LsaNode> targetPositions = new ArrayList<>();
        for (final Node<List<ILsaMolecule>> node : neigh.getNeighbors()) {
            final LsaNode n = (LsaNode) node;
            final List<ILsaMolecule> gradList;
            gradList = n.getConcentration(MOLGRAD);
            if (!gradList.isEmpty()) {
                for (final ILsaMolecule iExpressions : gradList) {
                    final double valueGrad = (Double) iExpressions.getArg(POS).calculate(null).getValue(null);
                    if (valueGrad <= minGrad) {
                        minGrad = valueGrad;
                        targetPositions.add(n);
                    }
                }

            }
        }
        if (!targetPositions.isEmpty()) {
            final P pd = getPosition(targetPositions.get(targetPositions.size() - 1));
            final double distance = getCurrentPosition().distanceTo(pd);
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
