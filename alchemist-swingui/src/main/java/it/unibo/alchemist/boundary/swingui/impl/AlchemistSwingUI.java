/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.swingui.impl;

import org.danilopianini.view.GUIUtilities;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import java.io.Serial;

/**
 * The main frame for the standard Alchemist GUI.
 *
 * @deprecated The entire Swing UI is deprecated and scheduled for replacement with a modern UI.
 */
@Deprecated
@SuppressWarnings("PMD")
public final class AlchemistSwingUI extends JFrame {

    /**
     * The default icon size.
     */
    public static final byte DEFAULT_ICON_SIZE = 16;

    @Serial
    private static final long serialVersionUID = -1447042371954686755L;

    /**
     * Loads an image and scales it to the default Alchemist's icon size.
     *
     * @param p
     *            the path where to load the image. The system resource loader
     *            is used to do so, with all its advantages
     * @return the resized icon
     */
    public static ImageIcon loadScaledImage(final String p) {
        return loadScaledImage(p, DEFAULT_ICON_SIZE);
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
            res = GUIUtilities.loadScaledImage(
                    "/icons/oxygen/32x32" + p,
                    size, size
            );
        }
        return res;
    }

}
