/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.swingui.impl;

import static it.unibo.alchemist.boundary.swingui.impl.LocalizedResourceBundle.getString;

/**
 * The commands that can be issued to a simulation from the GUI.
 *
 * @deprecated The entire Swing UI is deprecated and planned to be replaced with a modern UI.
 */
@Deprecated
public enum SimControlCommand {

    /**
     * Starts the simulation.
     */
    PLAY("/actions/media-playback-start.png", getString("play")),
    /**
     * Executes a single simulation step.
     */
    STEP("/actions/media-skip-forward.png", getString("step")),
    /**
     * Pauses the simulation.
     */
    PAUSE("/actions/media-playback-pause.png", getString("pause")),
    /**
     * Stops the simulation.
     */
    STOP("/actions/media-playback-stop.png", getString("stop"));

    private final String icon;
    private final String tt;

    SimControlCommand(final String iconPath, final String tooltip) {
        icon = iconPath;
        tt = tooltip;
    }

    /**
     * Creates a button issuing this command.
     *
     * @return a new {@link SimControlButton} for this enum
     */
    public SimControlButton createButton() {
        return new SimControlButton(icon, this, tt);
    }

    /**
     * Compares this enum to a String.
     *
     * @param s
     *            the String
     * @return true if the String representation of this enum is equal to the
     *         String
     */
    public boolean equalsToString(final String s) {
        return toString().equals(s);
    }

}
