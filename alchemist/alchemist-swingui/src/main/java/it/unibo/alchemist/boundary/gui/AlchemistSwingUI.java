/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.boundary.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.danilopianini.view.ButtonTabComponent;
import org.danilopianini.view.GUIUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static it.unibo.alchemist.boundary.l10n.LocalizedResourceBundle.getString;

/**
 * The main frame for the standard Alchemist GUI.
 * 
 * 
 */
@Deprecated
public final class AlchemistSwingUI extends JFrame {

    /**
     * The default icon size.
     */
    public static final byte DEFAULT_ICON_SIZE = 16;

    /**
     * The default window dimension.
     */
    public static final int DEFAULT_WINDOW_HEIGHT = 960, DEFAULT_WINDOW_WIDTH = 1280;

    /**
     * Loads an image and scales it to the default Alchemist's icon size.
     * 
     * @param p
     *            the path where to load the image. The system resource loader
     *            is used to do so, with all its advantages
     * @return the resized icon
     */
    public static ImageIcon loadScaledImage(final String p) {
        ImageIcon res = GUIUtilities.loadScaledImage(p, DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE);
        if (res == null) {
            res = GUIUtilities.loadScaledImage("/resources" + p, DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE);
        }
        return res;
    }

    /**
     * Loads an image and scales it to the desired size.
     * 
     * @param p
     *            the path where to load the image. The system resource loader
     *            is used to do so, with all its advantages
     * 
     * @param size
     *            the size which will be used both for x and y axes
     * @return the resized icon
     */
    public static ImageIcon loadScaledImage(final String p, final int size) {
        ImageIcon res = GUIUtilities.loadScaledImage(p, size, size);
        if (res == null) {
            res = GUIUtilities.loadScaledImage("/resources" + p, size, size);
        }
        return res;
    }

}
