/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.impl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.boundary.ui.api.ViewPort;
import javafx.scene.Node;

import java.util.Objects;

/**
 * Adapter class that adapts the JavaFX {@link Node} class to a generic ViewPort for usage in
 * {@link it.unibo.alchemist.boundary.ui.api.Wormhole2D}.
 */
public class NodeViewPort implements ViewPort {
    private Node node;

    /**
     * Default  constructor.
     *
     * @param node the node to adapt
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public NodeViewPort(final Node node) {
        this.node = node;
    }

    /**
     * Getter method for the node to be adapted.
     *
     * @return the node
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Node getNode() {
        return node;
    }

    /**
     * Setter method for the node to be adapted.
     *
     * @param node the node
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public void setNode(final Node node) {
        this.node = node;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public double getWidth() {
        return node.getBoundsInParent().getWidth();
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public double getHeight() {
        return node.getBoundsInParent().getHeight();
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final NodeViewPort nvt = (NodeViewPort) o;
        return Math.abs(getWidth() - nvt.getWidth()) < Double.MIN_VALUE
                && Math.abs(getHeight() - nvt.getHeight()) < Double.MIN_VALUE;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public int hashCode() {
        return Objects.hash(getWidth(), getHeight());
    }
}
