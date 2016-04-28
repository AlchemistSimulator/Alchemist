/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.gui.layouts;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * 
 *
 */
public abstract class AFlowLayout implements LayoutManager, Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    /**
     * The horizontal alignment constant that designates centering. Also used to
     * designate center anchoring.
     */
    public static final int CENTER = 0;
    /**
     * The horizontal alignment constant that designates right justification.
     */
    public static final int RIGHT = 1;
    /**
     * The horizontal alignment constant that designates left justification.
     */
    public static final int LEFT = 2;
    /**
     * The horizontal alignment constant that designates stretching the
     * component horizontally.
     */
    public static final int BOTH = 3;

    /**
     * The anchoring constant that designates anchoring to the top of the
     * display area.
     */
    public static final int TOP = 1;
    /**
     * The anchoring constant that designates anchoring to the bottom of the
     * display area.
     */
    public static final int BOTTOM = 2;

    private int gap; // the vertical vgap between components...defaults to 5
    private int alignment; // LEFT, RIGHT, CENTER or BOTH...how the components
                            // are justified
    private int anchor; // TOP, BOTTOM or CENTER ...where are the components
                        // positioned in an overlarge space
    private List<Component> compOrder;

    /**
     * 
     * @param hgap the vertical gap between components
     * @param alignment how the components are justified
     * @param anchor where the components are positioned
     * @param ordered true if the components must be ordered
     */
    public AFlowLayout(final int hgap, final int alignment, final int anchor, final boolean ordered) {
        this.gap = hgap;
        this.alignment = alignment;
        this.anchor = anchor;
        if (ordered) {
            compOrder = new ArrayList<>();
        }
    }

    @Override
    public void addLayoutComponent(final String name, final Component comp) {
        if (isOrdered()) {
            getComponentsList().add(comp);
        }
    }

    /**
     * 
     * @return the current alignment
     */
    protected int getAlignment() {
        return alignment;
    }

    /**
     * 
     * @return the current anchor
     */
    protected int getAnchor() {
        return anchor;
    }

    /**
     * 
     * @param c the component you want to know the order
     * @return the order
     */
    public int getComponentOrder(final Component c) {
        if (isOrdered()) {
            return compOrder.indexOf(c);
        } else {
            final int n = c.getParent().getComponentCount();
            final Component[] components = c.getParent().getComponents();
            for (int i = 0; i < n; i++) {
                if (components[i].equals(c)) {
                    return i;
                }
            }
            return -1;
        }
    }

    /**
     * 
     * @return a list with the ordered components
     */
    public List<Component> getComponentsList() {
        return compOrder;
    }

    /**
     * 
     * @return the current gap
     */
    protected int getGap() {
        return gap;
    }

    /**
     * 
     * @return true if the components are ordered
     */
    public boolean isOrdered() {
        return compOrder != null;
    }

    @Override
    public abstract void layoutContainer(final Container parent);

    /**
     * Calculates the size dimensions for the specified container, given the components it contains.
     * 
     * @param parent the component to be laid out.
     * @param minimum true if the returned dimension is the minimum one
     * @return a Dimension with the desired size
     */
    protected abstract Dimension layoutSize(final Container parent, final boolean minimum);

    @Override
    public Dimension minimumLayoutSize(final Container parent) {
        return layoutSize(parent, false);
    }

    @Override
    public Dimension preferredLayoutSize(final Container parent) {
        return layoutSize(parent, false);
    }

    @Override
    public void removeLayoutComponent(final Component comp) {
        if (isOrdered()) {
            getComponentsList().remove(comp);
        }
    }

    /**
     * 
     * @param alignment the new alignment
     */
    protected void setAlignment(final int alignment) {
        this.alignment = alignment;
    }

    /**
     * 
     * @param anchor the new anchor
     */
    protected void setAnchor(final int anchor) {
        this.anchor = anchor;
    }

    /**
     * 
     * @param c the component you want to order
     * @param order the position of the component
     * @throws IllegalStateException 
     */
    public void setComponentOrder(final Component c, final int order) throws IllegalStateException {
        if (!isOrdered()) {
            throw new IllegalStateException();
        }
        final Component temp = compOrder.get(order);
        final int old = compOrder.indexOf(c);
        compOrder.set(order, c);
        compOrder.set(old, temp);
    }

    /**
     * 
     * @param gap the new gap
     */
    protected void setGap(final int gap) {
        this.gap = gap;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[gap=" + gap + " align=" + alignment + " anchor=" + anchor + "]";
    }
}
