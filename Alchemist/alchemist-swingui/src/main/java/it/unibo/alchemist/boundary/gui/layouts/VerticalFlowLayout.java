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
 * alignment is <code>BOTH</code> in which case components are stretched
 * horizontally. Unlike FlowLayout, components will not wrap to form another
 * column if there isn't enough space vertically. VerticalLayout can optionally
 * anchor components to the top or bottom of the display area or center them
 * between the top and bottom.
 * 
 * Revision date 12th July 2001
 * 
 *         Homepage:www.kagi.com/equitysoft - Based on 'FlexLayout' in Java
 *         class libraries Vol 2 Chan/Lee Addison-Wesley 1998
 */

public class VerticalFlowLayout extends AFlowLayout {
    /**
     * 
     */
    private static final long serialVersionUID = -4226183812841326639L;
    private static final int DEFAULTVGAP = 5;

    /**
     * Constructs an instance of VerticalLayout with a vertical vgap of 5
     * pixels, horizontal centering and anchored to the top of the display area.
     * 
     * @param ordered true if the components must be ordered
     */
    public VerticalFlowLayout(final boolean ordered) {
        this(DEFAULTVGAP, CENTER, TOP, ordered);
    }

    /**
     * Constructs a VerticalLayout instance with horizontal centering, anchored
     * to the top with the specified vgap.
     * 
     * @param vgap An int value indicating the vertical seperation of the components
     * @param ordered true if the components must be ordered
     */
    public VerticalFlowLayout(final int vgap, final boolean ordered) {
        this(vgap, CENTER, TOP, ordered);
    }

    /**
     * Constructs a VerticalLayout instance anchored to the top with the
     * specified vgap and horizontal alignment.
     * 
     * @param vgap
     *            An int value indicating the vertical seperation of the
     *            components
     * @param alignment
     *            An int value which is one of
     *            <code>RIGHT, LEFT, CENTER, BOTH</code> for the horizontal
     *            alignment.
     * @param ordered true if the components must be ordered
     */
    public VerticalFlowLayout(final int vgap, final int alignment, final boolean ordered) {
        this(vgap, alignment, TOP, ordered);
    }

    /**
     * Constructs a VerticalLayout instance with the specified vgap, horizontal
     * alignment and anchoring.
     * 
     * @param vgap
     *            An int value indicating the vertical seperation of the
     *            components
     * @param alignment
     *            An int value which is one of
     *            <code>RIGHT, LEFT, CENTER, BOTH</code> for the horizontal
     *            alignment.
     * @param anchor
     *            An int value which is one of <code>TOP, BOTTOM, CENTER</code>
     *            indicating where the components are to appear if the display
     *            area exceeds the minimum necessary.
     * @param ordered true if the components must be ordered
     */
    public VerticalFlowLayout(final int vgap, final int alignment, final int anchor, final boolean ordered) {
        super(vgap, alignment, anchor, ordered);
    }

    /**
     * Lays out the container.
     */
    @Override
    public void layoutContainer(final Container parent) {
        final Insets insets = parent.getInsets();
        synchronized (parent.getTreeLock()) {
            final int n = parent.getComponentCount();
            final Dimension pd = parent.getSize();
            int y = 0;
            // work out the total size
            for (int i = 0; i < n; i++) {
                final Component c = isOrdered() ? getComponentsList().get(i) : parent.getComponent(i); // parent.getComponent(i);
                final Dimension d = c.getPreferredSize();
                y += d.height + getGap();
            }
            y -= getGap(); // otherwise there's a vgap too many
            // Work out the anchor paint
            if (getAnchor() == TOP) {
                y = insets.top;
            } else if (getAnchor() == CENTER) {
                y = (pd.height - y) / 2;
            } else {
                y = pd.height - y - insets.bottom;
            }
            // do layout
            for (int i = 0; i < n; i++) {
                final Component c = isOrdered() ? getComponentsList().get(i) : parent.getComponent(i); // parent.getComponent(i);
                final Dimension d = c.getPreferredSize();
                int x = insets.left;
                int wid = d.width;
                if (getAlignment() == CENTER) {
                    x = (pd.width - d.width) / 2;
                } else if (getAlignment() == RIGHT) {
                    x = pd.width - d.width - insets.right;
                } else if (getAlignment() == BOTH) {
                    wid = pd.width - insets.left - insets.right;
                }
                c.setBounds(x, y, wid, d.height);
                y += d.height + getGap();
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
                final Component c = /*
                                     * isOrdered() ? getCompOrder().get(i) :
                                     * parent.getComponent(i);
                                     */parent.getComponent(i);
                if (c.isVisible()) {
                    d = minimum ? c.getMinimumSize() : c.getPreferredSize();
                    dim.width = Math.max(dim.width, d.width);
                    dim.height += d.height;
                    if (i > 0) {
                        dim.height += getGap();
                    }
                }
            }
        }
        final Insets insets = parent.getInsets();
        dim.width += insets.left + insets.right;
        dim.height += insets.top + insets.bottom + getGap() + getGap();
        return dim;
    }
}
