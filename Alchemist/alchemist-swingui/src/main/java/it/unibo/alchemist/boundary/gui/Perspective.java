/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.boundary.gui.UpperBar.Commands;
import it.unibo.alchemist.boundary.gui.effects.JEffectsTab;
import it.unibo.alchemist.boundary.gui.util.GraphicalMonitorFactory;
import it.unibo.alchemist.boundary.interfaces.GraphicalOutputMonitor;
import it.unibo.alchemist.boundary.monitors.Generic2DDisplay;
import it.unibo.alchemist.boundary.monitors.TimeStepMonitor;
import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.core.interfaces.Status;
import org.apache.commons.math3.random.RandomGenerator;
import it.unibo.alchemist.language.EnvironmentBuilder;
import it.unibo.alchemist.language.EnvironmentBuilder.Result;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Environment;

import static it.unibo.alchemist.boundary.l10n.R.getString;

/**
 * @param <T>
 */
public class Perspective<T> extends JPanel implements ChangeListener, ActionListener {

    private static final long serialVersionUID = -6074331788924400019L;
    private static final FileFilter XML_FILTER = new FileNameExtensionFilter(getString("alchemist_xml"), "xml");
    private static final Logger L = LoggerFactory.getLogger(Perspective.class);
    private static final String FILE_NOT_VALID = getString("file_not_valid");
    private static final String RANDOM_REINIT_SUCCESS = getString("random_reinit_success");
    private static final String RANDOM_REINIT_FAILURE = getString("random_reinit_failure");
    private static final String NOT_AN_INTEGER = getString("not_an_integer");
    private static final String NOT_INITIALIZED_YET = getString("not_initialized_yet");

    private final UpperBar bar;

    private File currentDirectory = new File(System.getProperty("user.home"));
    private GraphicalOutputMonitor<T> main;
    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "All the random engines provided by Apache are Serializable")
    private RandomGenerator rand;
    private final SimControlPanel scp = SimControlPanel.createControlPanel(null);
    private JEffectsTab<T> effectsTab;
    private transient Simulation<T> sim;
    private final StatusBar status;
    private File xml;


    /**
     * Builds a new SAPERE perspective.
     */
    public Perspective() {
        super();
        setLayout(new BorderLayout());
        bar = new UpperBar(scp);
        add(bar, BorderLayout.NORTH);
        bar.addActionListener(this);
        bar.addChangeListener(this);
        status = new StatusBar();
        status.setText(getString("perspective"));
        add(status, BorderLayout.SOUTH);
        setMainDisplay(new Generic2DDisplay<T>());
    }

    private void makeEffects() {
        final JEffectsTab<T> effects = new JEffectsTab<>(main, true);
        if (effectsTab != null) {
            bar.deregisterTab(effectsTab);
            effects.setEffects(effectsTab.getEffects());
            effects.setEnabled(effectsTab.isEnabled());
        } else {
            effects.setEnabled(false);
        }
        effectsTab = effects;
        bar.registerTab(effectsTab);
    }

    private void dispose() {
        if (main != null) {
            if (sim != null) {
                sim.removeOutputMonitor(main);
            }
            remove((Component) main);
        }
        main = null;
        sim = null;
        effectsTab = null;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (Commands.OPEN.equalsToString(e.getActionCommand())) {
            openXML();
        } else if (Commands.PARALLEL.equalsToString(e.getActionCommand())) {
            process(true);
        } else if (Commands.PROCESS.equalsToString(e.getActionCommand())) {
            process(false);
        } else if (Commands.DICE.equalsToString(e.getActionCommand())) {
            setRandom();
        } else if (SimControlCommand.PLAY.equalsToString(e.getActionCommand())) {
            sim.addCommand(new Engine.StateCommand<T>().run().build());
            bar.setPlay(true);
        } else if (SimControlCommand.PAUSE.equalsToString(e.getActionCommand())) {
            sim.addCommand(new Engine.StateCommand<T>().pause().build());
            bar.setPlay(false);
        } else if (SimControlCommand.STEP.equalsToString(e.getActionCommand())) {
            sim.addCommand(new Engine.StateCommand<T>().run().build());
            sim.addCommand(new Engine.StateCommand<T>().pause().build());
        } else if (SimControlCommand.STOP.equalsToString(e.getActionCommand())) {
            sim.addCommand(new Engine.StateCommand<T>().stop().build());
            bar.setFileOK(true);
        } else if (Commands.REACTIVITY.equalsToString(e.getActionCommand())) {
            switch (bar.getReactivityStatus()) {
            case MAX_REACTIVITY:
                main.setStep(1);
                main.setRealTime(false);
                break;
            case REAL_TIME:
                main.setRealTime(true);
                main.setStep(1);
                break;
            case USER_SELECTED:
                main.setStep(bar.getReactivity());
                main.setRealTime(false);
                break;
            default:
                break;
            }
        } else {
            dispose();
        }
    }

    private void createMonitor() {
        final GraphicalOutputMonitor<T> display = GraphicalMonitorFactory.createMonitor(sim, e -> L.error("Cannot create monitor", e));
        setMainDisplay(display);
    }

    private void openXML() {
        final JFileChooser fc = new JFileChooser();
        fc.setMultiSelectionEnabled(false);
        fc.setFileFilter(XML_FILTER);
        fc.setCurrentDirectory(currentDirectory);
        final int returnVal = fc.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            xml = fc.getSelectedFile();
            currentDirectory = fc.getSelectedFile().getParentFile();
            if (xml.exists() && xml.getName().endsWith("xml")) {
                status.setText(getString("ready_to_process") + " " + xml.getAbsolutePath());
                status.setOK();
                if (sim != null) {
                    sim.addCommand(new Engine.StateCommand<T>().stop().build());
                }
                bar.setFileOK(true);
            } else {
                status.setText(FILE_NOT_VALID + " " + xml.getAbsolutePath());
                status.setNo();
                bar.setFileOK(false);
            }
        }
    }

    private void process(final boolean parallel) {
        if (sim != null) {
            sim.addCommand(new Engine.StateCommand<T>().stop().build());
            sim.waitFor(Status.STOPPED, 0, TimeUnit.MILLISECONDS);
        }
        try {
            sim = null;
            final Future<Result<T>> fenv = EnvironmentBuilder.build(new FileInputStream(xml));
            final Environment<T> env = fenv.get().getEnvironment();
            rand = fenv.get().getRandomGenerator();
            sim = new Engine<>(env, new DoubleTime(Double.POSITIVE_INFINITY), parallel);
            bar.setSimulation(sim);
            scp.setSimulation(sim);
            final Thread simThread = new Thread(sim);
            createMonitor();
            simThread.start();
            final TimeStepMonitor<T> tm = bar.getTimeMonitor();
            sim.addOutputMonitor(tm);
            bar.setFileOK(true);
            bar.setProcessOK(true);
            effectsTab.setEnabled(true);
            status.setOK();
            status.setText(getString("file_processed") + ": " + xml.getAbsolutePath());
        } catch (Exception e) {
            processError(e);
        }
    }

    private void processError(final Throwable e) {
        SwingUtilities.invokeLater(() -> {
            bar.setFileOK(false);
            bar.setProcessOK(false);
            status.setText(FILE_NOT_VALID + " " + xml.getAbsolutePath());
            status.setNo();
            L.error("Process error", e);
        });
    }

    private void setMainDisplay(final GraphicalOutputMonitor<T> gom) {
        if (main != null) {
            sim.removeOutputMonitor(main);
            gom.setStep(main.getStep());
            gom.setRealTime(main.isRealTime());
            remove((Component) main);
        }
        main = gom;
        if (sim != null) {
            new Thread(() -> sim.addOutputMonitor(main)).start();
        }
        add((Component) main, BorderLayout.CENTER);
        makeEffects();
        revalidate();
    }

    private void setRandom() {
        if (rand != null) {
            try {
                rand.setSeed(bar.getRandomText());
                status.setOK();
                status.setText(RANDOM_REINIT_SUCCESS);
            } catch (final NumberFormatException e) {
                status.setNo();
                status.setText(RANDOM_REINIT_FAILURE + ": " + NOT_AN_INTEGER);
            }
        } else {
            status.setNo();
            status.setText(RANDOM_REINIT_FAILURE + ": RandomGenerator " + NOT_INITIALIZED_YET);
        }
    }

    @Override
    public void stateChanged(final ChangeEvent e) {
        if (bar.getReactivityStatus().equals(it.unibo.alchemist.boundary.gui.ReactivityPanel.Status.USER_SELECTED)) {
            main.setStep(bar.getReactivity());
        }
    }

    @Override
    protected void finalize() throws Throwable {
        if (sim != null) {
            sim.addCommand(new Engine.StateCommand<T>().stop().build());
        }
        super.finalize();
    }

}
