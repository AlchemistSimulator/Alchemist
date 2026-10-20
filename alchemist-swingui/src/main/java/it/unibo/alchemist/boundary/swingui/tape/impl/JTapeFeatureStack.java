/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.swingui.tape.impl;

import java.awt.Component;
import java.io.Serial;
import java.util.List;

/**
 * A tape section stacking its features horizontally or vertically.
 *
 * @deprecated The entire Swing UI is deprecated and planned to be replaced with a modern UI.
 */
@Deprecated
public class JTapeFeatureStack extends AbstractJTapeSection {

    @Serial
    private static final long serialVersionUID = -6600427004858078324L;

    private final Type type;

    /**
     * The stacking direction.
     */
    public enum Type {
        /**
         * Features are stacked horizontally.
         */
        HORIZONTAL_STACK,

        /**
         * Features are stacked vertically.
         */
        VERTICAL_STACK
    }

    /**
     * Builds a new vertical feature stack.
     */
    public JTapeFeatureStack() {
        this(Type.VERTICAL_STACK);
    }

    /**
     * Builds a new feature stack of the given type.
     *
     * @param t the type
     */
    public JTapeFeatureStack(final Type t) {
        super();
        type = t;
        if (type == Type.VERTICAL_STACK) {
            super.setLayout(new VerticalFlowLayout(0, AbstractFlowLayout.BOTH, AbstractFlowLayout.TOP, true));
        } else if (type == Type.HORIZONTAL_STACK) {
            super.setLayout(new HorizontalFlowLayout(0, AbstractFlowLayout.BOTH, AbstractFlowLayout.LEFT, true));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component add(final Component c) {
        final Component r = super.add(c);
        getLayout().addLayoutComponent("", c);
        return r;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractFlowLayout getLayout() {
        return (AbstractFlowLayout) super.getLayout();
    }

    /**
     * Returns the components in their layout order.
     *
     * @return the ordered components
     */
    public List<Component> getOrderedComponents() {
        return getLayout().getComponentsList();
    }

    /**
     * Returns the stacking direction.
     *
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean registerFeature(final Component c) {
        add(c);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(final Component c) {
        super.remove(c);
        getLayout().removeLayoutComponent(c);
    }

    /**
     * Sets the position of the given component in the stack.
     *
     * @param c
     *            the component
     * @param order
     *            the order
     */
    public void setComponentOrder(final Component c, final int order) {
        getLayout().setComponentOrder(c, order);
        revalidate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean unregisterFeature(final Component c) {
        remove(c);
        return true;
    }
}
