package it.unibo.alchemist.boundary.wormhole.implementation.adapter;

import it.unibo.alchemist.boundary.wormhole.implementation.Wormhole2D;
import it.unibo.alchemist.boundary.wormhole.interfaces.ViewType;

import java.awt.*;

/**
 * Adapter class that adapts the AWT {@link Component} class to a generic View Type for usage in {@link Wormhole2D}.
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

    @Override
    public double getWidth() {
        return component.getWidth();
    }

    @Override
    public double getHeight() {
        return component.getHeight();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ComponentViewType nvt = (ComponentViewType) o;
        return getWidth() == nvt.getWidth() && getHeight() == nvt.getHeight();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Double.valueOf(getWidth()).hashCode();
        result = prime * result + Double.valueOf(getHeight()).hashCode();
        return result;
    }
}
