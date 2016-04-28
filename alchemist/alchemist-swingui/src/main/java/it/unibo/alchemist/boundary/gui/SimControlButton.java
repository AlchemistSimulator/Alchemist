/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.gui;

import javax.swing.JButton;

/**
 */
public class SimControlButton extends JButton {

    private static final long serialVersionUID = 5772261651038729446L;

    /**
     * Builds a new Button.
     * 
     * @param imagePath
     *            Path to the image to use
     * @param command
     *            the command for this button
     * @param tooltip
     *            the tooltip message
     */
    public SimControlButton(final String imagePath, final Enum<?> command, final String tooltip) {
        super(AlchemistSwingUI.loadScaledImage(imagePath));
        setActionCommand(command.toString());
        setToolTipText(tooltip);
        // TODO find a better way to do this
        setText(tooltip);
    }

}
