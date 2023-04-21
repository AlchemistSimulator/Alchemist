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
import it.unibo.alchemist.boundary.OutputMonitor;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.Actionable;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Position;
import it.unibo.alchemist.model.Time;

import javax.annotation.Nonnull;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.util.concurrent.atomic.AtomicBoolean;

import static it.unibo.alchemist.boundary.swingui.impl.AlchemistSwingUI.DEFAULT_ICON_SIZE;
import static it.unibo.alchemist.boundary.swingui.impl.AlchemistSwingUI.loadScaledImage;

/**
 *
 * @param <P> position type
 * @param <T>
 *            Concentration type
 */
@Deprecated
@SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "This class is not meant to get serialized")
public final class TimeStepMonitor<T, P extends Position<? extends P>> extends JPanel implements OutputMonitor<T, P> {

    private static final long serialVersionUID = 5818408644038869442L;
    private static final String BLANK = "", FINISHED = " (finished)";
    private static final int BORDER = 10, WIDTH = 200, HEIGHT = DEFAULT_ICON_SIZE + BORDER;
    private static final byte ICON_SIZE = DEFAULT_ICON_SIZE / 2;
    private boolean isFinished;
    private final JLabel s;
    private volatile long step;
    private final JLabel t;
    private volatile Time time = new DoubleTime();

    private final AtomicBoolean updateIsScheduled = new AtomicBoolean(false);

    /**
     * Constructor.
     */
    public TimeStepMonitor() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        final Icon tIcon = loadScaledImage("/oxygen/apps/clock.png", ICON_SIZE);
        final Icon sIcon = loadScaledImage("/oxygen/mimetypes/application-x-executable.png", ICON_SIZE);
        t = new JLabel(BLANK, tIcon, SwingConstants.LEADING);
        s = new JLabel(BLANK, sIcon, SwingConstants.LEADING);
        add(Box.createVerticalGlue());
        add(t);
        add(Box.createVerticalGlue());
        add(s);
        add(Box.createVerticalGlue());
        setBorder(new LineBorder(Color.GRAY));
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
    }

    @Override
    public void finished(@Nonnull final Environment<T, P> environment, @Nonnull final Time tt, final long cs) {
        isFinished = true;
        stepDone(environment, null, tt, cs);
        scheduleUpdate();
    }

    @Override
    public void initialized(@Nonnull final Environment<T, P> environment) {
        isFinished = false;
        stepDone(environment, null, new DoubleTime(), 0);
        scheduleUpdate();
    }

    @Override
    public void stepDone(
        @Nonnull final Environment<T, P> environment,
        final Actionable<T> reaction,
        @Nonnull final Time curTime,
        final long curStep
    ) {
        time = curTime;
        step = curStep;
        if (!updateIsScheduled.get()) {
            updateIsScheduled.set(true);
            scheduleUpdate();
        }
    }

    private void scheduleUpdate() {
        SwingUtilities.invokeLater(() -> {
            if (updateIsScheduled.getAndSet(false)) {
                t.setText(time + (isFinished ? FINISHED : BLANK));
                s.setText(step + (isFinished ? FINISHED : BLANK));
            }
        });
    }
}
