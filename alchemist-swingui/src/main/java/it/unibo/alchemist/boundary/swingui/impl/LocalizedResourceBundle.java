/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
/**
 * 
 */
package it.unibo.alchemist.boundary.swingui.impl;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Shorthand for getting resources.
 */
public final class LocalizedResourceBundle {

    private LocalizedResourceBundle() {
    }

    /**
     * @return a ResourceBundle using the current {@link Locale} (if available),
     *         falling back to {@link Locale#US} in case no localized bundle is
     *         available.
     */
    public static ResourceBundle get() {
        return get("it.unibo.alchemist.l10n.UIStrings");
    }

    /**
     * @param resourceBundle
     *            the resource bundle to load (fully qualified name)
     * 
     * @return a ResourceBundle using the current {@link Locale} (if available),
     *         falling back to {@link Locale#US} in case no localized bundle is
     *         available.
     */
    public static ResourceBundle get(final String resourceBundle) {
        try {
            return ResourceBundle.getBundle(resourceBundle);
        } catch (MissingResourceException e) {
            return ResourceBundle.getBundle(resourceBundle, Locale.US);
        }
    }

    /**
     * Looks up on the property files and returns the correct String.
     * 
     * @param key
     *            the key
     * @return the String
     */
    public static String getString(final String key) {
        return get().getString(key);
    }

}
