/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.unibo.alchemist.boundary.gui.layouts;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;

/**
 * 
 * A vertical layout manager similar to java.awt.FlowLayout. Like FlowLayout
 * components do not expand to fill available space except when the horizontal
 * getAlignment() is <code>BOTH</code> in which case components are stretched
 * horizontally. Unlike FlowLayout, components will not wrap to form another
 * column if there isn't enough space vertically. VerticalLayout can optionally
 * getAnchor() components to the top or bottom of the display area or center
 * them between the top and bottom.
 * 
 * Revision date 12th July 2001
 * 
 *         Homepage:www.kagi.com/equitysoft - Based on 'FlexLayout' in Java
 *         class libraries Vol 2 Chan/Lee Addison-Wesley 1998
 */

public class HorizontalFlowLayout extends AFlowLayout {

    /**
     * 
     */
    private static final long serialVersionUID = 5625120689939529161L;
    private static final int DEFAULTVGAP = 5;

    /**
     * Constructs an instance of VerticalLayout with a vertical vgap of 5
     * pixels, horizontal centering and anchored to the top of the display area.
     * 
     * @param ordered 
     */
    //CHECKSTYLE:OFF
    public HorizontalFlowLayout(final boolean ordered) {
        this(DEFAULTVGAP, CENTER, TOP, ordered);
    }
    //CHECKSTYLE:ON

    /**
     * Constructs a VerticalLayout instance with horizontal centering, anchored
     * to the top with the specified vgap.
     * 
     * @param hgap An int value indicating the vertical seperation of the
     *            components
     * @param ordered true if the components must be ordered
     */
    public HorizontalFlowLayout(final int hgap, final boolean ordered) {
        this(hgap, CENTER, TOP, ordered);
    }

    /**
     * Constructs a VerticalLayout instance anchored to the top with the
     * specified hgap and horizontal alignment.
     * 
     * @param hgap An int value indicating the vertical seperation of the
     *            components
     * @param align An int value which is one of
     *            <code>RIGHT, LEFT, CENTER, BOTH</code> for the horizontal
     *            getAlignment().
     * @param ordered true if the components must be ordered
     */
    public HorizontalFlowLayout(final int hgap, final int align, final boolean ordered) {
        this(hgap, align, RIGHT, ordered);
    }

    /**
     * Constructs a VerticalLayout instance with the specified vgap, horizontal
     * getAlignment() and anchoring.
     * 
     * @param hgap An int value indicating the vertical seperation of the
     *            components
     * @param align An int value which is one of
     *            <code>RIGHT, LEFT, CENTER, BOTH</code> for the horizontal
     *            getAlignment().
     * @param anchor An int value which is one of
     *            <code>TOP, BOTTOM, CENTER</code> indicating where the
     *            components are to appear if the display area exceeds the
     *            minimum necessary.
     * @param ordered true if the components must be ordered
     */
    public HorizontalFlowLayout(final int hgap, final int align, final int anchor, final boolean ordered) {
        super(hgap, align, anchor, ordered);
    }

    @Override
    public void layoutContainer(final Container parent) {
        final Insets insets = parent.getInsets();
        synchronized (parent.getTreeLock()) {
            final int n = parent.getComponentCount();
            final Dimension pd = parent.getSize();
            // int y = 0;
            int x = 0;
            // work out the total size
            for (int i = 0; i < n; i++) {
                final Component c = isOrdered() ? getComponentsList().get(i) : parent.getComponent(i); // parent.getComponent(i);
                final Dimension d = c.getPreferredSize();
                // y += d.height + getGap();
                x += d.width + getGap();
            }
            // y -= getGap(); // otherwise there's a vgap too many
            x -= getGap();
            // Work out the getAnchor() paint
            if (getAnchor() == LEFT) {
                // y = insets.top;
                x = insets.left;
            } else if (getAnchor() == CENTER) {
                // y = (pd.height - y) / 2;
                x = (pd.width - x) / 2;
            } else {
                // y = pd.height - x - insets.bottom;
                x = pd.width - x - insets.right;
            }
            // do layout
            for (int i = 0; i < n; i++) {
                final Component c = isOrdered() ? getComponentsList().get(i) : parent.getComponent(i); // parent.getComponent(i);
                final Dimension d = c.getPreferredSize();
                // int x = insets.left;
                int y = insets.top;
                // int wid = d.width;
                int hei = d.height;
                if (getAlignment() == CENTER) {
                    // x = (pd.width - d.width) / 2;
                    y = (pd.height - d.height) / 2;
                } else if (getAlignment() == RIGHT) {
                    // x = pd.width - d.width - insets.right;
                    y = pd.height - d.height - insets.bottom;
                } else if (getAlignment() == BOTH) {
                    // wid = pd.width - insets.left - insets.right;
                    hei = pd.height - insets.top - insets.bottom;
                }
                // c.setBounds(x, y, wid, d.height);
                c.setBounds(x, y, d.width, hei);
                // y += d.height + getGap();
                x += d.width + getGap();
            }
        }
    }

    @Override
    protected Dimension layoutSize(final Container parent, final boolean minimum) {
        final Dimension dim = new Dimension(0, 0);
        Dimension d;
        synchronized (parent.getTreeLock()) {
            final int n = parent.getComponentCount();
            for (int i = 0; i < n; i++) {
                final Component c = parent.getComponent(i);
                if (c.isVisible()) {
                    d = minimum ? c.getMinimumSize() : c.getPreferredSize();
                    // dim.width = Math.max(dim.width, d.width);
                    // dim.height += d.height;
                    dim.height = Math.max(dim.height, dim.height);
                    dim.width += d.width;
                    if (i > 0) {
                        // dim.height += getGap();
                        dim.width += getGap();
                    }
                }
            }
        }
        final Insets insets = parent.getInsets();
        dim.width += insets.left + insets.right + getGap() + getGap();
        dim.height += insets.top + insets.bottom;
        return dim;
    }
}
