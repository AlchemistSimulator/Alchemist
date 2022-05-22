/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.swingui.effect.impl;

import it.unibo.alchemist.boundary.swingui.effect.api.Effect;
import it.unibo.alchemist.boundary.ui.api.Wormhole2D;
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
public abstract class AbstractDrawOnce implements Effect {

    private static final long serialVersionUID = 1L;
    @Nullable
    private Integer markerNodeID;

    /**
     * {@inheritDoc}
     */
    @Override
    public <T, P extends Position2D<P>> void apply(
            final Graphics2D graphics,
            final Node<T> node,
            final Environment<T, P> environment,
            final Wormhole2D<P> wormhole
    ) {
        if (markerNodeID != null) {
            /*
             * We want to check if the cached id belongs to a node still present in
             * the environment, thus we don't use getNodeByID to avoid exceptions
             */
            final Optional<Node<T>> markerNode = environment.getNodes().stream()
                    .filter(it -> it.getId() == markerNodeID)
                    .findFirst();
            /*
             * if marker node is no longer in the environment or it is no longer displayed, we need to change it
             */
            if (markerNode.isEmpty()
                    || !wormhole.isInsideView(wormhole.getViewPoint(environment.getPosition(markerNode.get())))) {
                markerNodeID = null;
            }
        }
        if (markerNodeID == null) {
            markerNodeID = node.getId();
        }
        if (markerNodeID == node.getId()) {
            draw(graphics, node, environment, wormhole);
        }
    }

    /**
     * Draws the effect, this method is called only for a single a node of the environment.
     * @param graphics2D Graphics2D
     * @param node Node
     * @param environment environment
     * @param wormhole wormhole
     * @param <T> concentration type
     * @param <P> position type
     */
    protected abstract <T, P extends Position2D<P>> void draw(
            Graphics2D graphics2D,
            Node<T> node,
            Environment<T, P> environment,
            Wormhole2D<P> wormhole
    );

    /**
     * @return the marker node id
     */
    @Nullable
    public Integer getMarkerNodeID() {
        return markerNodeID;
    }
}
