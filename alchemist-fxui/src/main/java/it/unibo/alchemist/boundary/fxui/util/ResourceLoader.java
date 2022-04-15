/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.util;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * This utility class provides a working file loading everywhere in the project,
 * and both in source running and jar running.
 */
public final class ResourceLoader {
    /**
     * Resource bundle.
     */
    private static final String RESOURCE_BUNDLE = "it.unibo.alchemist.l10n.FXUIStrings";

    /**
     * Empty, private, constructor, as this is an utility class.
     */
    private ResourceLoader() {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    /**
     * Looks up on the property files and returns the correct String.
     *
     * @param key the key
     * @return the string
     * @throws NullPointerException     if key is null
     * @throws java.util.MissingResourceException if no object for the given key or no resource bundle can be found
     * @throws ClassCastException       if the object found for the given key is not a string
     */
    public static String getStringRes(final String key) {
        return ResourceBundle.getBundle(RESOURCE_BUNDLE).getString(key);
    }

    /**
     * Looks up on the property files and returns the correct String.
     *
     * @param key    the key
     * @param locale the locale
     * @return the string
     * @throws NullPointerException     if key or locale is null
     * @throws java.util.MissingResourceException if no object for the given key or no resource bundle can be found
     * @throws ClassCastException       if the object found for the given key is not a string
     */
    public static String getStringRes(final String key, final Locale locale) {
        return ResourceBundle.getBundle(RESOURCE_BUNDLE, locale).getString(key);
    }
}
