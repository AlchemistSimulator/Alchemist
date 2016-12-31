/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.monitors;

import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Semaphore;

import org.apache.commons.math3.util.FastMath;
import org.danilopianini.lang.RangedInteger;
import org.danilopianini.view.ExportForGUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <T>
 */
@ExportInspector
@Deprecated
public abstract class EnvironmentInspector<T> implements OutputMonitor<T> {

    private static final long serialVersionUID = -6609357608585315L;
    private static final int OOM_RANGE = 24;
    private static final Logger L = LoggerFactory.getLogger(EnvironmentInspector.class);

    /**
     * The sampling mode.
     * 
     */
    public enum Mode {
        /**
         * 
         */
        TIME, STEP
    }

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.getDefault());
    private PrintStream writer;
    private String fpCache;
    private double lastUpdate = Double.NEGATIVE_INFINITY;
    private long lastStep = Long.MIN_VALUE;
    private final Semaphore mutex = new Semaphore(1);

    @ExportForGUI(nameToExport = "File path")
    private String filePath = System.getProperty("user.home") + System.getProperty("file.separator")
            + sdf.format(new Date()) + "-alchemist_report.log";
    @ExportForGUI(nameToExport = "Value separator")
    private String separator = " ";
    @ExportForGUI(nameToExport = "Report time")
    private boolean logTime = true;
    @ExportForGUI(nameToExport = "Report step")
    private boolean logStep = true;
    @ExportForGUI(nameToExport = "Sampling mode")
    private Mode mode = Mode.TIME;
    @ExportForGUI(nameToExport = "Sample order of magnitude")
    private RangedInteger intervaloom = new RangedInteger(-OOM_RANGE, OOM_RANGE, 0);
    @ExportForGUI(nameToExport = "Sample space")
    private RangedInteger interval = new RangedInteger(1, 100, 1);

    @Override
    public void finished(final Environment<T> env, final Time time, final long step) {
        if (writer != null) {
            writer.close();
        }
        writer = null;
        lastUpdate = Double.NEGATIVE_INFINITY;
        lastStep = Long.MIN_VALUE;
        fpCache = null;
    }

    @Override
    public void initialized(final Environment<T> env) {
        stepDone(env, null, new DoubleTime(), 0);
    }

    @Override
    public void stepDone(final Environment<T> env, final Reaction<T> r, final Time time, final long step) {
        mutex.acquireUninterruptibly();
        if (System.identityHashCode(fpCache) != System.identityHashCode(filePath)) {
            fpCache = filePath;
            if (writer != null) {
                writer.close();
            }
            try {
                writer = new PrintStream(new File(fpCache), StandardCharsets.UTF_8.name());
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                L.error("Could create a PrintStream", e);
            }
        }
        final double sample = interval.getVal() * FastMath.pow(10, intervaloom.getVal());
        final boolean log = mode.equals(Mode.TIME) ? time.toDouble() - lastUpdate >= sample : step - lastStep >= sample;
        if (log) {
            lastUpdate = time.toDouble();
            lastStep = step;
            writeData(env, r, time, step);
        }
        mutex.release();
    }

    private void writeData(final Environment<T> env, final Reaction<T> r, final Time time, final long step) {
        if (writer == null) {
            throw new IllegalStateException("Error initializing the file writer in " + getClass().getCanonicalName());
        }
        if (logTime) {
            writer.print(time.toDouble());
            writer.print(separator);
        }
        if (logStep) {
            writer.print(step);
            writer.print(separator);
        }
        for (final double d : extractValues(env, r, time, step)) {
            writer.print(d);
            writer.print(separator);
        }
        writer.println();
    }

    /**
     * @return file path
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * @param fp
     *            file path
     */
    public void setFilePath(final String fp) {
        this.filePath = fp;
    }

    /**
     * @return separator
     */
    public String getSeparator() {
        return separator;
    }

    /**
     * @param s
     *            separator
     */
    public void setSeparator(final String s) {
        this.separator = s;
    }

    /**
     * @return true if the {@link EnvironmentInspector} is logging time
     */
    public boolean isLoggingTime() {
        return logTime;
    }

    /**
     * @param lt
     *            true if you want to log time
     */
    public void setLogTime(final boolean lt) {
        this.logTime = lt;
    }

    /**
     * @return true if the {@link EnvironmentInspector} is logging steps
     */
    public boolean isLoggingStep() {
        return logStep;
    }

    /**
     * @param ls
     *            true if you want to log steps
     */
    public void setLogStep(final boolean ls) {
        this.logStep = ls;
    }

    /**
     * @return current mode
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * @param m
     *            the mode
     */
    public void setMode(final Mode m) {
        this.mode = m;
    }

    /**
     * @return order of magnitude of the sapling interval
     */
    public RangedInteger getIntervalOrderOfMagnitude() {
        return intervaloom;
    }

    /**
     * @param ioom
     *            order of magnitude of the sapling interval
     */
    public void setIntervaloom(final RangedInteger ioom) {
        this.intervaloom = ioom;
    }

    /**
     * @return sampling interval
     */
    public RangedInteger getInterval() {
        return interval;
    }

    /**
     * @param i
     *            sampling interval
     */
    public void setInterval(final RangedInteger i) {
        this.interval = i;
    }

    /**
     * This method extracts data values from an environment snapshot.
     * 
     * @param env
     *            environment
     * @param r
     *            reaction executed
     * @param time
     *            time
     * @param step
     *            step
     * @return an array of data values
     */
    protected abstract double[] extractValues(
            Environment<T> env,
            Reaction<T> r,
            Time time,
            long step);

}
