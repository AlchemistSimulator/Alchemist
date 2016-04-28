/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.gui;

import it.unibo.alchemist.boundary.l10n.R;

import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;

/**
 */
public class Menu extends AbstractMenu {

    private static final long serialVersionUID = 5209455686362711386L;
    private static final JMenuItem[] ITEMS = { new JMenuItem(R.getString("open_perspective")) };
    private int n = 1;

    /**
     * Builds the File menu.
     * 
     */
    public Menu() {
        super(R.getString("perspective"), ITEMS);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource().equals(ITEMS[0])) {
            final StringBuilder sb = new StringBuilder("SAPERE Perspective");
            if (n > 1) {
                sb.append(' ');
                sb.append(n);
            }
            n++;
            final String name = sb.toString();
            AlchemistSwingUI.addTab(new Perspective<>(), name, name);
        }
    }

}
