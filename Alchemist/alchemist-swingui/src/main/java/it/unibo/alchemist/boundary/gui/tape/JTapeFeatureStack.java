/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.gui.tape;

import it.unibo.alchemist.boundary.gui.layouts.AFlowLayout;
import it.unibo.alchemist.boundary.gui.layouts.HorizontalFlowLayout;
import it.unibo.alchemist.boundary.gui.layouts.VerticalFlowLayout;

import java.awt.Component;
import java.util.List;

/**
 * 
 * 
 */
public class JTapeFeatureStack extends JTapeSection {

    private static final long serialVersionUID = -6600427004858078324L;

    private final Type type;

    /**
     * 
     * 
     */
    public enum Type {
        /**
         * 
         */
        HORIZONTAL_STACK,

        /**
         * 
         */
        VERTICAL_STACK;
    }

    /**
     * 
     */
    public JTapeFeatureStack() {
        this(Type.VERTICAL_STACK);
    }

    /**
     * 
     * @param t the type
     */
    public JTapeFeatureStack(final Type t) {
        super();
        type = t;
        if (type == Type.VERTICAL_STACK) {
            setLayout(new VerticalFlowLayout(0, AFlowLayout.BOTH, AFlowLayout.TOP, true));
        } else if (type == Type.HORIZONTAL_STACK) {
            setLayout(new HorizontalFlowLayout(0, AFlowLayout.BOTH, AFlowLayout.LEFT, true));
        }
    }

    @Override
    public Component add(final Component c) {
        final Component r = super.add(c);
        getLayout().addLayoutComponent("", c);
        return r;
    }

    /**
     * 
     */
    @Override
    public AFlowLayout getLayout() {
        return (AFlowLayout) super.getLayout();
    }

    /**
     * 
     * @return the ordered components
     */
    public List<Component> getOrderedComponents() {
        return getLayout().getComponentsList();
    }

    /**
     * 
     * @return the type
     */
    public Type getType() {
        return type;
    }

    @Override
    public boolean registerFeature(final Component c) {
        add(c);
        return true;
    }

    @Override
    public void remove(final Component c) {
        super.remove(c);
        getLayout().removeLayoutComponent(c);
    }

    /**
     * @param c
     *            the component
     * @param order
     *            the order
     */
    public void setComponentOrder(final Component c, final int order) {
        getLayout().setComponentOrder(c, order);
        revalidate();
    }

    @Override
    public boolean unregisterFeature(final Component c) {
        remove(c);
        return true;
    }
}
