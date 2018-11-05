/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.boundary.gui;

import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 */
@Deprecated
public abstract class AbstractMenu extends JMenu implements ActionListener {

    private static final long serialVersionUID = 5209455686362711386L;

    /**
     * @param title
     *            the menu name
     * @param items
     *            the items for this menu
     */
    public AbstractMenu(final String title, final JMenuItem[] items) {
        super(title);
        for (final JMenuItem i : items) {
            add(i);
            i.setActionCommand(i.getText());
            i.addActionListener(this);
        }
    }

}
