/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.sapere.actions;

import it.unibo.alchemist.model.implementations.actions.MoveOnMap;
import it.unibo.alchemist.model.sapere.dsl.ITreeNode;
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule;
import it.unibo.alchemist.model.implementations.movestrategies.routing.OnStreets;
import it.unibo.alchemist.model.movestrategies.speed.InteractWithOthers;
import it.unibo.alchemist.model.implementations.movestrategies.target.FollowTrace;
import it.unibo.alchemist.model.implementations.routingservices.GraphHopperOptions;
import it.unibo.alchemist.model.implementations.routingservices.GraphHopperRoutingService;
import it.unibo.alchemist.model.sapere.ILsaAction;
import it.unibo.alchemist.model.sapere.ILsaMolecule;
import it.unibo.alchemist.model.sapere.ILsaNode;
import it.unibo.alchemist.model.interfaces.MapEnvironment;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;
import org.danilopianini.lang.HashString;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

/**
 */
public final class SAPEREWalker
    extends MoveOnMap<List<ILsaMolecule>, GraphHopperOptions, GraphHopperRoutingService>
    implements ILsaAction {

    /**
     * The default molecule that identifies an interacting object.
     */
    public static final ILsaMolecule DEFAULT_INTERACTING_TAG = new LsaMolecule("person");
    private static final long serialVersionUID = 8533918846332597708L;

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
     */
    public SAPEREWalker(
        final MapEnvironment<List<ILsaMolecule>, GraphHopperOptions, GraphHopperRoutingService> environment,
        final ILsaNode node,
        final Reaction<List<ILsaMolecule>> reaction,
        final double speed,
        final double interaction,
        final double range
    ) {
        this(environment, node, reaction, DEFAULT_INTERACTING_TAG, speed, interaction, range);
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
     */
    public SAPEREWalker(
            final MapEnvironment<List<ILsaMolecule>, GraphHopperOptions, GraphHopperRoutingService> environment,
            final ILsaNode node,
            final Reaction<List<ILsaMolecule>> reaction,
            final ILsaMolecule tag,
            final double speed,
            final double interaction,
            final double range
    ) {
        super(
            environment,
            node,
            new OnStreets<>(environment, GraphHopperOptions.Companion.getDefaultOptions()),
            new InteractWithOthers<>(environment, node, reaction, tag, speed, range, interaction),
            new FollowTrace<>(reaction)
        );
    }

    @Override
    public SAPEREWalker cloneAction(final Node<List<ILsaMolecule>> node, final Reaction<List<ILsaMolecule>> reaction) {
        return null;
    }

    @Nonnull
    @Override
    public ILsaNode getNode() {
        return (ILsaNode) super.getNode();
    }

    @Override
    public void setExecutionContext(final Map<HashString, ITreeNode<?>> matches, final List<ILsaNode> nodes) {
    }
}
