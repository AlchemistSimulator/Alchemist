/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.wormhole.implementation.adapter;

import it.unibo.alchemist.boundary.wormhole.interfaces.ViewType;
import javafx.scene.Node;

/**
 * Adapter class that adapts the JavaFX {@link Node} class to a generic View Type for usage in {@link Wormhole2D}.
 */
public class NodeViewType implements ViewType {
    private Node node;

    /**
     * Default  constructor.
     *
     * @param node the node to adapt
     */
    public NodeViewType(final Node node) {
        this.node = node;
    }

    /**
     * Getter method for the node to be adapted.
     *
     * @return the node
     */
    public Node getNode() {
        return node;
    }

    /**
     * Setter method for the node to be adapted.
     *
     * @param node the node
     */
    public void setNode(final Node node) {
        this.node = node;
    }

    /**
     * @inheritDocs
     */
    @Override
    public double getWidth() {
        return node.getBoundsInParent().getWidth();
    }

    /**
     * @inheritDocs
     */
    @Override
    public double getHeight() {
        return node.getBoundsInParent().getHeight();
    }

    /**
     * @inheritDocs
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final NodeViewType nvt = (NodeViewType) o;
        return Math.abs(getWidth() - nvt.getWidth()) < Double.MIN_VALUE && Math.abs(getHeight() - nvt.getHeight()) < Double.MIN_VALUE;
    }

    /**
     * @inheritDocs
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Double.valueOf(getWidth()).hashCode();
        result = prime * result + Double.valueOf(getHeight()).hashCode();
        return result;
    }
}
