/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.monitors;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map.Entry;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * @param <T>
 */
public class NodeTracker<T> extends JPanel implements OutputMonitor<T>, ActionListener {
    private static final byte MARGIN = 100;
    private static final String PROGRAM = " = Program =", CONTENT = " = Content =", POSITION = " = POSITION = ";
    private static final long serialVersionUID = -676002989218532788L;
    private static final int AREA_SIZE = 80;
    private final JTextArea txt = new JTextArea(AREA_SIZE / 2, AREA_SIZE);
    private final Node<T> n;
    private int stringLength = Byte.MAX_VALUE;

    /**
     * @param node
     *            the node to track
     * 
     */
    public NodeTracker(final Node<T> node) {
        super();
        final JScrollPane areaScrollPane = new JScrollPane(txt);
        n = node;
        setLayout(new BorderLayout(0, 0));
        txt.setEditable(false);
        add(areaScrollPane, BorderLayout.CENTER);
        areaScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        areaScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
    }

    @Override
    public void finished(final Environment<T> env, final Time time, final long step) {
        stepDone(env, null, time, step);
    }

    @Override
    public void initialized(final Environment<T> env) {
        stepDone(env, null, null, 0L);
    }

    @Override
    public void stepDone(final Environment<T> env, final Reaction<T> exec, final Time time, final long step) {
        if (exec == null || exec.getNode().equals(n)) {
            final StringBuilder sb = new StringBuilder(stringLength);
            sb.append(POSITION);
            sb.append('\n');
            sb.append(env.getPosition(n));
            sb.append("\n\n\n");
            sb.append(CONTENT);
            sb.append('\n');
            for (final Entry<Molecule, T> e : n.getContents().entrySet()) {
                sb.append(e.getKey());
                sb.append(" > ");
                sb.append(e.getValue());
                sb.append('\n');
            }
            sb.append("\n\n\n");
            sb.append(PROGRAM);
            sb.append("\n\n");
            for (final Reaction<T> r : n.getReactions()) {
                sb.append(r.toString());
                sb.append("\n\n");
            }
            stringLength = sb.length() + MARGIN;
            SwingUtilities.invokeLater(() -> {
                txt.setText(sb.toString());
            });
        }
    }
}
