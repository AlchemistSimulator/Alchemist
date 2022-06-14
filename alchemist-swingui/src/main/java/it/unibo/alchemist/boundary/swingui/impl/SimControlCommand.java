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
 */
@Deprecated
public enum SimControlCommand {

    /**
     * 
     */
    PLAY("/actions/media-playback-start.png", getString("play")),
    /**
     * 
     */
    STEP("/actions/media-skip-forward.png", getString("step")),
    /**
     * 
     */
    PAUSE("/actions/media-playback-pause.png", getString("pause")),
    /**
     * 
     */
    STOP("/actions/media-playback-stop.png", getString("stop"));

    private final String icon, tt;

    SimControlCommand(final String iconPath, final String tooltip) {
        icon = iconPath;
        tt = tooltip;
    }

    /**
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
