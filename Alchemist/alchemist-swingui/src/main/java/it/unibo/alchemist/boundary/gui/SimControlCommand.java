/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.gui;

import static it.unibo.alchemist.boundary.l10n.R.getString;

/**
 */
public enum SimControlCommand {

    /**
     * 
     */
    PLAY("/oxygen/actions/media-playback-start.png", getString("play")),
    /**
     * 
     */
    STEP("/oxygen/actions/media-skip-forward.png", getString("step")),
    /**
     * 
     */
    PAUSE("/oxygen/actions/media-playback-pause.png", getString("pause")),
    /**
     * 
     */
    STOP("/oxygen/actions/media-playback-stop.png", getString("stop"));

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
