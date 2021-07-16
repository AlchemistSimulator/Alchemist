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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.boundary.wormhole.interfaces.ViewPort;

import static it.unibo.alchemist.HashesKt.murmur3Hash32;

/**
 * Adapter class that adapts the AWT {@link Component} class to a generic ViewPort for usage in
 * {@link it.unibo.alchemist.boundary.wormhole.implementation.AbstractWormhole2D}.
 */
public class ComponentViewPort implements ViewPort {
    private Component component;

    /**
     * Default  constructor.
     *
     * @param component the component to adapt
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "This is intentional")
    public ComponentViewPort(final Component component) {
        this.component = component;
    }

    /**
     * Getter method for the component to be adapted.
     *
     * @return the component
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "This is intentional")
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
     * {@inheritDoc}
     */
    @Override
    public double getWidth() {
        return component.getWidth();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getHeight() {
        return component.getHeight();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ComponentViewPort nvt = (ComponentViewPort) o;
        return Math.abs(getWidth() - nvt.getWidth()) < Double.MIN_VALUE
                && Math.abs(getHeight() - nvt.getHeight()) < Double.MIN_VALUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return murmur3Hash32(getWidth(), getHeight());
    }
}
