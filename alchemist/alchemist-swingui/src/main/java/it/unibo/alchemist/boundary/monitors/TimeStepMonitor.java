/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.monitors;

import static it.unibo.alchemist.boundary.gui.AlchemistSwingUI.DEFAULT_ICON_SIZE;
import static it.unibo.alchemist.boundary.gui.AlchemistSwingUI.loadScaledImage;

import java.awt.Color;
import java.awt.Dimension;
import java.util.concurrent.Semaphore;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * @param <T>
 *            Concentration type
 */
public class TimeStepMonitor<T> extends JPanel implements OutputMonitor<T> {

    private static final long serialVersionUID = 5818408644038869442L;
    private static final String BLANK = "", FINISHED = " (finished)";
    private static final int BORDER = 10, WIDTH = 200, HEIGHT = DEFAULT_ICON_SIZE + BORDER;
    private static final byte ICON_SIZE = DEFAULT_ICON_SIZE / 2;
    private boolean isFinished;
    private volatile boolean needsUpdate = true;
    private final JLabel s;
    private long step;
    private final JLabel t;
    private Time time = new DoubleTime();
    private Updater updater;

    private class Updater implements Runnable {
        private volatile boolean updaterAlive = true;
        private final Semaphore mutex = new Semaphore(0);
        @Override
        public void run() {
            do {
                mutex.acquireUninterruptibly();
                mutex.drainPermits();
                scheduleUpdate();
            } while (updaterAlive);
        }
        public void stop() {
            updaterAlive = false;
        }
        public void update() {
            mutex.release();
        }
    }

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
    public void finished(final Environment<T> env, final Time tt, final long cs) {
        isFinished = true;
        stepDone(env, null, tt, cs);
        updater.stop();
        updater.update();
        updater = null;
    }

    @Override
    public void initialized(final Environment<T> env) {
        isFinished = false;
        stepDone(env, null, new DoubleTime(), 0);
    }

    @Override
    public void stepDone(final Environment<T> env, final Reaction<T> r, final Time curTime, final long curStep) {
        if (updater == null) {
            updater = new Updater();
            new Thread(updater, TimeStepMonitor.class.getSimpleName() + " updater thread").start();
        }
        time = curTime;
        step = curStep;
        needsUpdate = true;
        updater.update();
    }

    private void scheduleUpdate() {
        SwingUtilities.invokeLater(() -> {
            if (needsUpdate) {
                needsUpdate = false;
                t.setText(time + (isFinished ? FINISHED : BLANK));
                s.setText(Long.toString(step) + (isFinished ? FINISHED : BLANK));
            }
        });
    }

}
