/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.gui.asmc;

import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 */
public class ASMCMenu extends JPanel {

    private static final long serialVersionUID = -7503763457199009074L;
    private static final int TEXT_FIELD_LENGTH = 5;
    private static final int N_COLUMNS = 3;

    private final JButton alpha, ub, lb, grain, switchView, redraw, autoScale;

    private final JTextField alphaVal, ubVal, lbVal, grainVal;

    /**
     * 
     */
    public enum Commands {
        /**
         * 
         */
        ALPHA, UB, LB, GRAIN, SWITCH_VIEW, REDRAW, AUTO_SCALE;

        // CHECKSTYLE:OFF
        public boolean equals(final String s) {
            return this.toString().equals(s);
        }
        // CHECKSTYLE:ON
    }

    /**
     * Default constructor.
     */
    public ASMCMenu() {
        super();
        final JPanel left = new JPanel();
        add(left);
        left.setLayout(new GridLayout(0, N_COLUMNS));

        left.add(new JLabel("Alpha"));
        alphaVal = new JTextField(TEXT_FIELD_LENGTH);
        left.add(alphaVal);
        alpha = new JButton("setAlpha");
        left.add(alpha);
        alpha.setEnabled(true);
        alpha.setActionCommand(Commands.ALPHA.toString());

        left.add(new JLabel("Lower bound"));
        lbVal = new JTextField(TEXT_FIELD_LENGTH);
        left.add(lbVal);
        lb = new JButton("setLB");
        left.add(lb);
        lb.setEnabled(true);
        lb.setActionCommand(Commands.LB.toString());

        left.add(new JLabel("Upper bound"));
        ubVal = new JTextField(TEXT_FIELD_LENGTH);
        left.add(ubVal);
        ub = new JButton("setUB");
        left.add(ub);
        ub.setEnabled(true);
        ub.setActionCommand(Commands.UB.toString());

        left.add(new JLabel("Grain"));
        grainVal = new JTextField(TEXT_FIELD_LENGTH);
        left.add(grainVal);
        grain = new JButton("setStep");
        left.add(grain);
        grain.setEnabled(true);
        grain.setActionCommand(Commands.GRAIN.toString());

        switchView = new JButton("switchView");
        add(switchView);
        switchView.setEnabled(true);
        switchView.setActionCommand(Commands.SWITCH_VIEW.toString());

        redraw = new JButton("redraw");
        add(redraw);
        redraw.setEnabled(true);
        redraw.setActionCommand(Commands.REDRAW.toString());

        autoScale = new JButton("auto scale");
        add(autoScale);
        autoScale.setEnabled(true);
        autoScale.setActionCommand(Commands.AUTO_SCALE.toString());

    }

    /**
     * @param l
     *            Associates an ActionListener to all interface elements
     */
    public void addActionListener(final ActionListener l) {
        alpha.addActionListener(l);
        ub.addActionListener(l);
        lb.addActionListener(l);
        grain.addActionListener(l);
        switchView.addActionListener(l);
        redraw.addActionListener(l);
        autoScale.addActionListener(l);
    }

    /**
     * @return String value of field content
     */
    public String getAlphaField() {
        return alphaVal.getText();
    }

    /**
     * @return String value of field content
     */
    public String getGrainField() {
        return grainVal.getText();
    }

    /**
     * @return String value of field content
     */
    public String getLBField() {
        return lbVal.getText();
    }

    /**
     * @return String value of field content
     */
    public String getUBField() {
        return ubVal.getText();
    }

    /**
     * Clears inserted values in uperrBound and lowerBound fields.
     */
    public void resetBoundsFields() {
        lbVal.setText("");
        ubVal.setText("");
    }
}
