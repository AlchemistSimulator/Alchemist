/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.gui.effects;

/**
 * Simple factory to build {@link Effect effects}.
 */
public final class EffectFactoryFX {

    /**
     * @return the effect which is inserted by default
     */
    public static EffectFX buildDefaultEffect() {
        return new DrawShapeFX();
    }

    /**
     * Given a class, builds the corresponding effect.
     * 
     * @param effect
     *            the effect class
     * @return a new effect
     * @throws IllegalArgumentException
     *             if it can't instantiate it by reflection for some reason
     */
    public static EffectFX buildEffect(final Class<? extends EffectFX> effect) {
        try {
            return effect.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Default, empty, constructor, as it's an utility class.
     */
    private EffectFactoryFX() {
        // Empty constructor
    };

}
