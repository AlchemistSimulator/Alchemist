/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.gui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

/**
 */
public abstract class AbstractMenu extends Menu implements EventHandler<ActionEvent> {

    /**
     * @param title
     *            the menu name
     * @param items
     *            the items for this menu
     */
    public AbstractMenu(final String title, final MenuItem[] items) {
        super(title);
        for (final MenuItem i : items) {
            getItems().add(i);
            i.setOnAction(this);
        }
    }

}
