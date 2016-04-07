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
import it.unibo.alchemist.boundary.gui.tape.JTapeFeatureStack;
import it.unibo.alchemist.boundary.gui.tape.JTapeGroup;
import it.unibo.alchemist.boundary.gui.tape.JTapeMainFeature;
import it.unibo.alchemist.boundary.gui.tape.JTapeSection;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeListener;

import static it.unibo.alchemist.boundary.l10n.R.getString;

/**
 */
public class ReactivityPanel extends JTapeGroup implements ItemListener {

    private static final long serialVersionUID = 6688803192091760332L;

    private static final int SLIDE_MAX = 20;
    private static final int SLIDE_SIZE = 150;

    private static final String UI_REACTIVITY = getString("ui_reactivity");
    private static final String MAX_REACTIVITY = getString("max_reactivity");
    private static final String REALTIME = getString("realtime");
    private static final String USER_SELECTED = getString("user_selected");

    private final JTapeSection stack1 = new JTapeFeatureStack();
    private final JTapeSection buttMF = new JTapeMainFeature();
    private final JTapeSection sliderMF = new JTapeMainFeature();
    private final JToggleButton btnMax;
    private final JToggleButton btnReal;
    private final JToggleButton btnUser;
    private final JSlider slider = new JSlider(0, SLIDE_MAX, SLIDE_MAX / 2);
    private final Icon max = loadScaledImage("/oxygen/status/user-online.png");
    private final Icon real = loadScaledImage("/oxygen/status/user-invisible.png");
    private final Icon user = loadScaledImage("/oxygen/status/user-offline.png");
    private Status status = Status.MAX_REACTIVITY;

    /**
     */
    public enum Status {
        /**
         * The GUI is always updated.
         */
        MAX_REACTIVITY,
        /**
         * The GUI tries to run in sync with the real time, keeping at least 25
         * frames per second.
         */
        REAL_TIME,
        /**
         * The GUI update frequency is user defined. Use the slider.
         */
        USER_SELECTED,
    }

    /**
     * 
     */
    public ReactivityPanel() {
        super(UI_REACTIVITY);
        slider.setPreferredSize(new Dimension(SLIDE_SIZE, slider.getHeight()));
        // setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        // button = new JButton(max);
        btnMax = new JToggleButton(MAX_REACTIVITY, max, true);
        btnReal = new JToggleButton(REALTIME, real, false);
        btnUser = new JToggleButton(USER_SELECTED, user, false);
        /*
         * add(button); add(slider); button.addActionListener(this);
         */
        btnMax.addItemListener(this);
        btnReal.addItemListener(this);
        btnUser.addItemListener(this);
        slider.setEnabled(false);

        stack1.registerFeature(btnMax);
        stack1.registerFeature(btnReal);
        buttMF.registerFeature(btnUser);
        sliderMF.registerFeature(slider);

        registerSection(stack1);
        registerSection(buttMF);
        registerSection(sliderMF);
    }

    /**
     * See {@link AbstractButton#addActionListener(ActionListener)}.
     * 
     * @param l
     *            the {@link ActionListener} to add
     */
    public void addActionListener(final ActionListener l) {
        btnMax.addActionListener(l);
        btnReal.addActionListener(l);
        btnUser.addActionListener(l);
    }

    /**
     * @param c
     *            the ChangeListener to add
     */
    public void addChangeLister(final ChangeListener c) {
        slider.addChangeListener(c);
    }

    /**
     * @return the current status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * @return the current reactivity set by user
     */
    public int getUserReactivity() {
        return (int) Math.pow(2, slider.getValue());
    }

    @Override
    public void itemStateChanged(final ItemEvent e) {
        final Object src = e.getSource();
        if (e.getStateChange() == ItemEvent.SELECTED) {
            if (src.equals(btnMax)) {
                status = Status.MAX_REACTIVITY;
                btnReal.setSelected(false);
                btnUser.setSelected(false);
                slider.setEnabled(false);
            } else if (src.equals(btnReal)) {
                status = Status.REAL_TIME;
                btnMax.setSelected(false);
                btnUser.setSelected(false);
                slider.setEnabled(false);
            } else if (src.equals(btnUser)) {
                status = Status.USER_SELECTED;
                btnReal.setSelected(false);
                btnMax.setSelected(false);
                slider.setEnabled(true);
            }
        }
    }

    /**
     * Sets the command name for the action event fired by this component.
     * 
     * @param c
     *            the action command
     */
    public void setActionCommand(final String c) {
        // button.setActionCommand(c);
        btnMax.setActionCommand(c);
        btnReal.setActionCommand(c);
        btnUser.setActionCommand(c);
    }

}
