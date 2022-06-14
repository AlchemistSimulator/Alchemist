/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.swingui.monitor.impl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Actionable;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;

import javax.annotation.Nonnull;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.stream.Collectors;

/**
 *
 * @param <P> position type
 * @param <T> concentration type
 */
@Deprecated
@SuppressFBWarnings(justification = "This class is deprecated anyway")
public final class NodeTracker<T, P extends Position<? extends P>> extends JPanel implements OutputMonitor<T, P>, ActionListener {
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
    public void finished(@Nonnull final Environment<T, P> environment, @Nonnull final Time time, final long step) {
        stepDone(environment, null, time, step);
    }

    @Override
    public void initialized(@Nonnull final Environment<T, P> environment) {
        stepDone(environment, null, DoubleTime.ZERO, 0L);
    }

    @Override
    public void stepDone(
        @Nonnull final Environment<T, P> environment,
        final Actionable<T> reaction,
        @Nonnull final Time time,
        final long step
    ) {
        if (reaction == null || reaction instanceof Reaction && ((Reaction<T>) reaction).getNode().equals(n)) {
            final StringBuilder sb = new StringBuilder(stringLength)
                .append(POSITION)
                .append('\n')
                .append(environment.getPosition(n))
                .append("\n\n\n")
                .append(CONTENT)
                .append('\n')
                .append(n.getContents().entrySet().stream()
                    .map(e -> e.getKey().getName() + " > " + e.getValue() + '\n')
                    .sorted()
                    .collect(Collectors.joining())
                )
                .append("\n\n\n")
                .append(PROGRAM)
                .append("\n\n");
            for (final Reaction<T> r : n.getReactions()) {
                sb.append(r.toString()).append("\n\n");
            }
            stringLength = sb.length() + MARGIN;
            SwingUtilities.invokeLater(() -> txt.setText(sb.toString()));
        }
    }
}
