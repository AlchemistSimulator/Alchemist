/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.wormhole.implementation.adapter;

import java.awt.Component;

import it.unibo.alchemist.boundary.wormhole.interfaces.ViewType;

import static it.unibo.alchemist.HashesKt.murmur3Hash32;

/**
 * Adapter class that adapts the AWT {@link Component} class to a generic View Type for usage in
 * {@link it.unibo.alchemist.boundary.wormhole.implementation.AbstractWormhole2D}.
 */
public class ComponentViewType implements ViewType {
    private Component component;

    /**
     * Default  constructor.
     *
     * @param component the component to adapt
     */
    public ComponentViewType(final Component component) {
        this.component = component;
    }

    /**
     * Getter method for the component to be adapted.
     *
     * @return the component
     */
    public Component getComponent() {
        return component;
    }

    /**
     * Setter method for the component to be adapted.
     *
     * @param component the component
     */
    public void setComponent(final Component component) {
        this.component = component;
    }

    /**
     * @inheritDocs
     */
    @Override
    public double getWidth() {
        return component.getWidth();
    }

    /**
     * @inheritDocs
     */
    @Override
    public double getHeight() {
        return component.getHeight();
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
        final ComponentViewType nvt = (ComponentViewType) o;
        return Math.abs(getWidth() - nvt.getWidth()) < Double.MIN_VALUE
                && Math.abs(getHeight() - nvt.getHeight()) < Double.MIN_VALUE;
    }

    /**
     * @inheritDocs
     */
    @Override
    public int hashCode() {
        return murmur3Hash32(getWidth(), getHeight());
    }
}
