/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
/*
 * Copyright (C) 2010-2015, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import java.util.List;

import it.unibo.alchemist.model.implementations.movestrategies.routing.OnStreets;
import it.unibo.alchemist.model.implementations.movestrategies.speed.InteractWithOthers;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.ILsaNode;
import it.unibo.alchemist.model.interfaces.MapEnvironment;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Vehicle;
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy;
import static java.util.Objects.requireNonNull;


/**
 */
public class SAPEREWalkerRiseGradient extends MoveOnMap<List<ILsaMolecule>> {

    private static final long serialVersionUID = 2429200360671138611L;

    /**
     * @param environment
     *            the environment
     * @param node
     *            the node
     * @param reaction
     *            the reaction (used to compute the movement length)
     * @param speed
     *            the average speed of the node
     * @param interaction
     *            the interaction factor
     * @param range
     *            the interaction range
     * @param templateLSA
     *            the LSA template to follow
     * @param neighPos
     *            the position in the template LSA that contains the next hop
     */
    public SAPEREWalkerRiseGradient(
            final MapEnvironment<List<ILsaMolecule>> environment,
            final ILsaNode node,
            final Reaction<List<ILsaMolecule>> reaction,
            final double speed,
            final double interaction,
            final double range,
            final Molecule templateLSA,
            final int neighPos) {
        this(environment, node, reaction, SAPEREWalker.DEFAULT_INTERACTING_TAG, speed, interaction, range, templateLSA, neighPos);
    }

    /**
     * @param environment
     *            the environment
     * @param node
     *            the node
     * @param reaction
     *            the reaction (used to compute the movement length)
     * @param tag
     *            the molecule which identifies the interacting nodes
     * @param speed
     *            the average speed of the node
     * @param interaction
     *            the interaction factor
     * @param range
     *            the interaction range
     * @param templateLSA
     *            the LSA template to follow
     * @param neighPos
     *            the position in the template LSA that contains the next hop
     */
    public SAPEREWalkerRiseGradient(final MapEnvironment<List<ILsaMolecule>> environment,
            final ILsaNode node,
            final Reaction<List<ILsaMolecule>> reaction,
            final Molecule tag,
            final double speed,
            final double interaction,
            final double range,
            final Molecule templateLSA,
            final int neighPos) {
        super(environment, node,
                new OnStreets<>(environment, Vehicle.FOOT),
                new InteractWithOthers<>(environment, node, reaction, tag, speed, range, interaction),
                new NextTargetStrategy(environment, node, templateLSA, neighPos));
    }

    private static final class NextTargetStrategy implements TargetSelectionStrategy {
        /**
         * 
         */
        private static final long serialVersionUID = -618772546563562484L;
        private final MapEnvironment<List<ILsaMolecule>> environment;
        private final Node<List<ILsaMolecule>> node;
        private final ILsaMolecule template;
        private final int argPos;
        private Node<List<ILsaMolecule>> curNode;
        private Position curPos;

        NextTargetStrategy(
                final MapEnvironment<List<ILsaMolecule>> env,
                final Node<List<ILsaMolecule>> n,
                final Molecule patt,
                final int pos) {
            environment = requireNonNull(env);
            node = requireNonNull(n);
            curNode = n;
            template = requireNonNull(ensureIsSAPERE(patt));
            argPos = requireNonNull(pos);
        }

        @Override
        public Position getTarget() {
            final MapEnvironment<List<ILsaMolecule>> env = environment;
            final List<ILsaMolecule> matches = node.getConcentration(template);
            /*
             * If there is no gradient and: - there is no goal, or - the goal
             * has already been reached
             * 
             * then remain still.
             */
            final Position currentPosition = env.getPosition(node);
            if (matches.isEmpty()) {
                if (curPos == null || currentPosition.equals(curPos)) {
                    return currentPosition;
                }
                return curPos;
            }
            final int nid = ((Double) matches.get(0).getArg(argPos).getRootNodeData()).intValue();
            /*
             * If current target node has moved, destination should be
             * re-computed.
             */
            final Position curNodeActualPos = env.getPosition(curNode);
            if (curNode.equals(node)
                    || !curPos.equals(curNodeActualPos)
                    || env.getNeighborhood(node).contains(curNode)) {
                /*
                 * Update target
                 */
                curNode = env.getNodeByID(nid);
                curPos = env.getPosition(curNode);
            }
            return curPos;
        }
    }

    private static ILsaMolecule ensureIsSAPERE(final Molecule m) {
        if (m instanceof ILsaMolecule) {
            return (ILsaMolecule) m;
        }
        throw new IllegalArgumentException(m + " is not a valid SAPERE LSA");
    }

}
