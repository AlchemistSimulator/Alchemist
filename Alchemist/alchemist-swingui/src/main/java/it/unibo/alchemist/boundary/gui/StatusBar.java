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

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 */
public class StatusBar extends JLabel {

    private static final long serialVersionUID = -7528209649098499107L;
    private static final byte ICON_SIZE = 16;
    private final Icon ok = loadScaledImage("/oxygen/emotes/opinion-okay.png", ICON_SIZE);
    private final Icon no = loadScaledImage("/oxygen/emotes/opinion-no.png", ICON_SIZE);

    /**
     * 
     */
    public StatusBar() {
        super("Init OK", SwingConstants.LEADING);
        setIcon(ok);
    }

    /**
     * 
     */
    public void setNo() {
        setIcon(no);
    }

    /**
     * 
     */
    public void setOK() {
        setIcon(ok);
    }

}
