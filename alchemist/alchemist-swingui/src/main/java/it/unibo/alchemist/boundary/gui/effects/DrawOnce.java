/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.gui.effects;

import it.unibo.alchemist.boundary.wormhole.interfaces.IWormhole2D;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position2D;

import javax.annotation.Nullable;
import java.awt.Graphics2D;
import java.util.Optional;

/**
 * Effects are normally applied for each node, this is a base
 * class for effects that do not need to be redrawn for each node.
 * In other words, this effect will be applied for a single node
 * instead of redrawing for all of them.
 */
public abstract class DrawOnce implements Effect {

    private static final long serialVersionUID = 1L;
    @Nullable
    private Integer markerNodeID;

    /**
     * {@inheritDoc}
     */
    @Override
    public <T, P extends Position2D<P>> void apply(final Graphics2D g, final Node<T> n, final Environment<T, P> env, final IWormhole2D<P> wormhole) {
        if (markerNodeID != null) {
            /*
             * We want to check if the cached id belongs to a node still present in
             * the environment, thus we don't use getNodeByID to avoid exceptions
             */
            final Optional<Node<T>> markerNode = env.getNodes().stream()
                    .filter(node -> node.getId() == markerNodeID)
                    .findFirst();
            /*
             * if marker node is no longer in the environment or it is no longer displayed, we need to change it
             */
            if (markerNode.isEmpty() || !wormhole.isInsideView(wormhole.getViewPoint(env.getPosition(markerNode.get())))) {
                markerNodeID = null;
            }
        }
        if (markerNodeID == null) {
            markerNodeID = n.getId();
        }
        if (markerNodeID == n.getId()) {
            draw(g, n, env, wormhole);
        }
    }

    /**
     * Draws the effect, this method is called only for a single a node of the environment.
     * @param g Graphics2D
     * @param n Node
     * @param env environment
     * @param wormhole wormhole
     * @param <T> concentration type
     * @param <P> position type
     */
    protected abstract <T, P extends Position2D<P>> void draw(Graphics2D g, Node<T> n, Environment<T, P> env, IWormhole2D<P> wormhole);

    /**
     * @return the marker node id
     */
    @Nullable
    public Integer getMarkerNodeID() {
        return markerNodeID;
    }
}
