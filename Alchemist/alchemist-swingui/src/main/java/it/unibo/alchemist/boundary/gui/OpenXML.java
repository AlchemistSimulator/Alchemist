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

import javax.swing.JButton;

/**
 */
public class OpenXML extends JButton {

    private static final long serialVersionUID = 20671260008783881L;

    /**
     * Default constructor.
     */
    public OpenXML() {
        super(loadScaledImage("/oxygen/actions/document-open.png"));
        setToolTipText("Open an AlchemistXML model");
    }

}
