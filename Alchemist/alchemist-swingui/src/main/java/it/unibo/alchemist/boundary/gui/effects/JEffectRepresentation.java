/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.gui.effects;

import it.unibo.alchemist.boundary.gui.tape.JTapeFeatureStack;
import it.unibo.alchemist.boundary.interfaces.GraphicalOutputMonitor;

import java.awt.Color;
import java.awt.ItemSelectable;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;

import org.danilopianini.view.ObjectModFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Representation of an {@link Effect}. Useful to let the user interact with an
 * effect (create/edit). Effects are relative to a
 * {@link GraphicalOutputMonitor} instance.
 * 
 * @param <T>
 *            is the type for the concentration
 */
public class JEffectRepresentation<T> extends JTapeFeatureStack implements ItemSelectable {

    private static final long serialVersionUID = -6875167656425950159L;
    private static final Logger L = LoggerFactory.getLogger(JEffectRepresentation.class);
    private final Effect effect;
    private final GraphicalOutputMonitor<T> monitor;
    private final JLabel info;
    private boolean selected;
    private final transient List<ItemListener> itemListeners = new LinkedList<>();
    private final transient ItemListener itemListener = new ItemListener() {

        @Override
        public void itemStateChanged(final ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                setBorder(new LineBorder(Color.BLUE, 2));
            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                setBorder(new LineBorder(UIManager.getColor("Panel.background")));
            }

        }
    };

    private final transient MouseAdapter mouseAdapter = new MouseAdapter() {

        @Override
        public void mouseClicked(final java.awt.event.MouseEvent e) {
            if (!selected && isEnabled()) {
                setSelected(true);
                try {
                    final ObjectModFrame mod = new ObjectModFrame(effect);
                    mod.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(final ActionEvent e) {
                            updateColor();
                            monitor.repaint();
                        }
                    });
                    final Point p = getLocation();
                    SwingUtilities.convertPointToScreen(p, JEffectRepresentation.this);
                    mod.setLocation(p);
                    mod.setVisible(true);
                } catch (final IllegalAccessException e1) {
                    L.error("Cannot modify the frame target object", e1);
                }
            } else {
                setSelected(false);
            }
        }

        @Override
        public void mouseEntered(final java.awt.event.MouseEvent e) {
            if (!selected && isEnabled()) {
                setBorder(new LineBorder(Color.CYAN, 2));
            }
        }

        @Override
        public void mouseExited(final java.awt.event.MouseEvent e) {
            if (!selected && isEnabled()) {
                setBorder(new LineBorder(UIManager.getColor("Panel.background")));
            }
        }
    };

    private static String getActualClassUpperLetters(final Object o) {
        final StringBuilder sb = new StringBuilder();
        final String name = o.getClass().getSimpleName();
        for (final char c : name.toCharArray()) {
            if (Character.isUpperCase(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Creates a new representation for the effect and monitor in input.
     * 
     * @param e
     *            is the {@link Effect} to represent
     * @param main
     *            is the {@link GraphicalOutputMonitor} that will use the effect
     */
    public JEffectRepresentation(final Effect e, final GraphicalOutputMonitor<T> main) {
        super();
        effect = e;
        info = new JLabel();
        this.monitor = main;
        registerFeature(info);
        updateColor();
        addMouseListener(mouseAdapter);
        addItemListener(itemListener);
    }

    @Override
    public final void addItemListener(final ItemListener l) {
        itemListeners.add(l);
    }

    /**
     * Gets the represented effects.
     * 
     * @return an {@link Effect}
     */
    public Effect getEffect() {
        return effect;
    }

    @Override
    public Object[] getSelectedObjects() {
        if (selected) {
            return new Object[] { this };
        } else {
            return null;
        }
    }

    /**
     * Check if the representation is selected.
     * 
     * @return a <code>boolean</code> value
     */
    public boolean isSelected() {
        return selected;
    }

    private void notifySelection() {
        for (final ItemListener l : itemListeners) {
            l.itemStateChanged(new ItemEvent(this, 0, this, selected ? ItemEvent.SELECTED : ItemEvent.DESELECTED));
        }
    }

    @Override
    public void removeItemListener(final ItemListener l) {
        itemListeners.remove(l);
    }

    @Override
    public void setEnabled(final boolean value) {
        super.setEnabled(value);
        if (!value) {
            selected = false;
        }
    }

    /**
     * Changes the {@link GraphicalOutputMonitor} for this representation.
     * 
     * @param main
     *            is the new {@link GraphicalOutputMonitor}
     */
//    public void setMonitor(final GraphicalOutputMonitor<T> main) {
//        monitor = main;
//    }

    /**
     * (Un)selects this representation.
     * 
     * @param s
     *            a <code>boolean</code>
     */
    public void setSelected(final boolean s) {
        selected = s;
        notifySelection();
    }

    private void updateColor() {
        final Color c = effect.getColorSummary();
        final float brightness = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null)[2];
        final Color neg = brightness < 0.5f ? Color.WHITE : Color.BLACK;
        setBackground(c);
        info.setForeground(neg);
        info.setText(getActualClassUpperLetters(effect));
    }

}
