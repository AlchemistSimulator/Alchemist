/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.gui.monitors;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ItemSelectable;
import java.awt.Point;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import org.danilopianini.view.ObjectModFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.boundary.l10n.R;

/**
 * @param <T>
 */
public class JOutputMonitorRepresentation<T> extends JPanel implements ItemSelectable {
    /**
     * 
     */
    private static final long serialVersionUID = 5590060251090393414L;
    private static final Logger L = LoggerFactory.getLogger(JOutputMonitorRepresentation.class);
    private final OutputMonitor<T> monitor;
    private boolean selected;
    private final transient List<ItemListener> itemListeners = new LinkedList<>();
    private final transient MouseAdapter mouseAdapter = new MouseAdapter() {

        @Override
        public void mouseClicked(final java.awt.event.MouseEvent e) {
            if (!selected && isEnabled()) {
                setSelected(true);
                try {
                    final JFrame f = new ObjectModFrame(monitor);
                    final Point p = getLocation();
                    SwingUtilities.convertPointToScreen(p, JOutputMonitorRepresentation.this);
                    f.setLocation((int) p.getX(), (int) p.getY());
                    f.setVisible(true);
                } catch (IllegalAccessException e1) {
                    L.error(R.getString("cannot_access_object"), e1);
                }
            } else {
                setSelected(false);
            }
        }

        @Override
        public void mouseEntered(final java.awt.event.MouseEvent e) {
            if (!selected && isEnabled()) {
                setBorder(new LineBorder(Color.CYAN, 1, true));
            }
        }

        @Override
        public void mouseExited(final java.awt.event.MouseEvent e) {
            if (!selected && isEnabled()) {
                setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
            }
        }
    };

    /**
     * @param mon the {@link OutputMonitor}
     */
    public JOutputMonitorRepresentation(final OutputMonitor<T> mon) {
        super();
        setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
        setLayout(new BorderLayout(0, 0));

        final JLabel infoLabel = new JLabel();
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(infoLabel, BorderLayout.CENTER);
        monitor = mon;
        infoLabel.setText(monitor.getClass().getSimpleName());

        addMouseListener(mouseAdapter);
        addItemListener((e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                setBorder(new LineBorder(Color.BLUE, 2, true));
            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
            }
        });
    }

    @Override
    public void addItemListener(final ItemListener l) {
        itemListeners.add(l);
    }

    /**
     * @return the monitor
     */
    public OutputMonitor<T> getMonitor() {
        return monitor;
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
     * @return true if it is selected
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

    /**
     * @param val
     *            if the component is selected
     */
    public void setSelected(final boolean val) {
        selected = val;
        notifySelection();
    }

}
