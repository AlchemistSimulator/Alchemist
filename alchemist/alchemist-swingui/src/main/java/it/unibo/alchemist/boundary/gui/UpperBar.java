/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.gui;

import static it.unibo.alchemist.boundary.gui.AlchemistSwingUI.loadScaledImage;

import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;

import it.unibo.alchemist.boundary.gui.monitors.JMonitorsTab;
import it.unibo.alchemist.boundary.gui.tape.JTape;
import it.unibo.alchemist.boundary.gui.tape.JTapeFeatureStack;
import it.unibo.alchemist.boundary.gui.tape.JTapeGroup;
import it.unibo.alchemist.boundary.gui.tape.JTapeMainFeature;
import it.unibo.alchemist.boundary.gui.tape.JTapeSection;
import it.unibo.alchemist.boundary.gui.tape.JTapeTab;
import it.unibo.alchemist.boundary.l10n.R;
import it.unibo.alchemist.boundary.monitors.TimeStepMonitor;
import it.unibo.alchemist.core.interfaces.Simulation;

/**
 */
public final class UpperBar extends JTape {

    /**
     */
    public enum Commands {
        /**
         * 
         */
        DICE, OPEN, PARALLEL, PROCESS, RANDOM, REACTIVITY;

        /**
         * @param s
         *            string representation
         * @return true if equal to the String representation of the Command
         */
        public boolean equalsToString(final String s) {
            return this.toString().equals(s);
        }
    }

    private static final byte COLUMNS = 12;

    private static final long serialVersionUID = 7964622676457801603L;
    private static final String HOME_TAB = R.getString("home_tab");
    private static final String FLOW_TAB = R.getString("flow_tab");
    private static final String START = R.getString("start");
    private static final String RANDOM = R.getString("random");
    private static final String TIME = R.getString("time");
    private static final String OPEN = R.getString("open");
    private static final String LOAD_PARALLEL = R.getString("load_parallel");
    private static final String LOAD_SINGLE = R.getString("load_single");
    private static final String PROCESS_FILE = R.getString("process_file");
    private static final String CHANGE_RANDOM_SEED = R.getString("change_random_seed");

    private final JMonitorsTab<?> monTab;
    private final JButton open, parallel, process, dice;
    private final JTextField random = new NumericTextField();
    private final ReactivityPanel reactivity = new ReactivityPanel();
    private final SimControlPanel scp;

    private final Icon singlethread = loadScaledImage("/oxygen/actions/split.png");
    private final TimeStepMonitor<?> time = new TimeStepMonitor<>();

    /**
     * Default constructor.
     * 
     * @param control
     *            the SimControlPanel to use
     */
    public UpperBar(final SimControlPanel control) {
        super();

        final JTapeTab homeTab = new JTapeTab(HOME_TAB);
        final JTapeTab flowTab = new JTapeTab(FLOW_TAB);
        monTab = new JMonitorsTab<>();

        final JTapeGroup startGroup = new JTapeGroup(START);
        final JTapeGroup randGroup = new JTapeGroup(RANDOM);
        final JTapeGroup timeGroup = new JTapeGroup(TIME);

        final JTapeSection openMF = new JTapeMainFeature();
        final JTapeSection loadFS = new JTapeFeatureStack();
        final JTapeSection seedFS = new JTapeFeatureStack();
        final JTapeSection timeMF = new JTapeMainFeature();

        open = new OpenXML();
        open.setText(OPEN);
        open.setActionCommand(Commands.OPEN.toString());
        openMF.registerFeature(open);

        parallel = new JButton(singlethread);
        parallel.setText(LOAD_PARALLEL);
        parallel.setEnabled(false);
        parallel.setActionCommand(Commands.PARALLEL.toString());
        loadFS.registerFeature(parallel);

        final Icon processIcon = loadScaledImage("/oxygen/actions/system-reboot.png");
        process = new JButton(processIcon);
        process.setText(LOAD_SINGLE);
        process.setEnabled(false);
        process.setToolTipText(PROCESS_FILE);
        process.setActionCommand(Commands.PROCESS.toString());
        loadFS.registerFeature(process);

        final Icon diceIcon = loadScaledImage("/oxygen/status/media-playlist-shuffle.png");
        dice = new JButton(diceIcon);
        dice.setText(CHANGE_RANDOM_SEED);
        dice.setActionCommand(Commands.DICE.toString());
        random.setColumns(COLUMNS);
        dice.setEnabled(false);
        dice.setToolTipText(CHANGE_RANDOM_SEED);
        random.setEnabled(false);
        random.setActionCommand(Commands.RANDOM.toString());
        seedFS.registerFeature(dice);
        seedFS.registerFeature(random);

        startGroup.registerSection(openMF);
        startGroup.registerSection(loadFS);
        randGroup.registerSection(seedFS);
        homeTab.registerGroup(startGroup);
        homeTab.registerGroup(randGroup);

        scp = control;
        scp.setEnabled(false);
        flowTab.registerGroup(scp);
        reactivity.setActionCommand(Commands.REACTIVITY.toString());
        flowTab.registerGroup(reactivity);

        timeMF.registerFeature(time);
        timeGroup.registerSection(timeMF);
        flowTab.registerGroup(timeGroup);

        registerTab(homeTab);
        registerTab(flowTab);
        registerTab(monTab);

        setFileOK(false);
    }

    /**
     * See {@link AbstractButton#addActionListener(ActionListener)}.
     * 
     * @param l
     *            the {@link ActionListener} to add
     */
    public void addActionListener(final ActionListener l) {
        open.addActionListener(l);
        parallel.addActionListener(l);
        random.addActionListener(l);
        dice.addActionListener(l);
        process.addActionListener(l);
        scp.addActionListener(l);
        reactivity.addActionListener(l);
    }

    /**
     * Adds a new {@link ChangeListener}.
     * 
     * @param l
     *            the {@link ChangeListener} to add
     */
    public void addChangeListener(final ChangeListener l) {
        // TODO check this, may create problems
        super.addChangeListener(l);
        if (reactivity != null) {
            reactivity.addChangeLister(l);
        }
    }

    /**
     * @return the open {@link JButton}
     */
    public JButton getOpenButton() {
        return open;
    }

    /**
     * @return the process {@link JButton}
     */
    public JButton getProcessButton() {
        return process;
    }

    /**
     * @return the random {@link JButton}
     */
    public JButton getRandomButton() {
        return dice;
    }

    /**
     * @return the random String as text
     */
    public int getRandomText() {
        return Integer.parseInt(random.getText());
    }

    /**
     * @return the reactivity level
     */
    public int getReactivity() {
        return reactivity.getUserReactivity();
    }

    /**
     * @return the reactivity status
     */
    public ReactivityPanel.Status getReactivityStatus() {
        return reactivity.getStatus();
    }

    /**
     * @param <N>
     *            the coordinates data type
     * @param <D>
     *            the distance data type
     * @param <T>
     *            the concentration data type
     * @return the {@link TimeStepMonitor}
     */
    @SuppressWarnings("unchecked")
    public <N extends Number, D extends Number, T> TimeStepMonitor<T> getTimeMonitor() {
        return (TimeStepMonitor<T>) time;
    }

    /**
     * The file has been loaded.
     * 
     * @param b
     *            value
     */
    public void setFileOK(final boolean b) {
        process.setEnabled(b);
        parallel.setEnabled(b);
        random.setEnabled(false);
        dice.setEnabled(false);
        scp.setEnabled(false);
    }

    /**
     * The play button has been pressed.
     * 
     * @param b
     *            value
     */
    public void setPlay(final boolean b) {
        scp.setButtonEnabled(SimControlCommand.PLAY, !b);
        scp.setButtonEnabled(SimControlCommand.PAUSE, b);
        scp.setButtonEnabled(SimControlCommand.STEP, !b);
        scp.setButtonEnabled(SimControlCommand.STOP, true);
    }

    /**
     * The file has been processed.
     * 
     * @param b
     *            value
     */
    public void setProcessOK(final boolean b) {
        process.setEnabled(true);
        parallel.setEnabled(true);
        random.setEnabled(b);
        dice.setEnabled(b);
        scp.setButtonEnabled(SimControlCommand.PLAY, b);
        scp.setButtonEnabled(SimControlCommand.PAUSE, false);
        scp.setButtonEnabled(SimControlCommand.STEP, b);
        scp.setButtonEnabled(SimControlCommand.STOP, false);
    }

    /**
     * Sets a new random {@link String}.
     * 
     * @param seed
     *            value
     */
    public void setRandom(final int seed) {
        random.setText(Integer.toString(seed));
    }

    /**
     * @param s
     *            simulation
     */
    public void setSimulation(final Simulation<?> s) {
        monTab.setSimulation(s);
    }
}
