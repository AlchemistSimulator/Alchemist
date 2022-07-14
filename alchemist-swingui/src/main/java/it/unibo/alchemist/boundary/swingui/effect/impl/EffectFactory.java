/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.swingui.effect.impl;

import it.unibo.alchemist.boundary.swingui.effect.api.Effect;

import java.lang.reflect.InvocationTargetException;

/**
 */
@Deprecated
public final class EffectFactory {

    /**
     * @return the effect which is inserted by default
     */
    public static Effect buildDefaultEffect() {
        return new DrawShape();
    }

    /**
     * Given a class, builds the corresponding effect.
     * 
     * @param effect
     *            the effect class
     * @return a new effect
     */
    public static Effect buildEffect(final Class<? extends Effect> effect) {
        try {
            return effect.getConstructor().newInstance();
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalArgumentException("The effect must have a parameterless constructor " + effect.getSimpleName(), e);
        }
    }

    private EffectFactory() {
    }

}
