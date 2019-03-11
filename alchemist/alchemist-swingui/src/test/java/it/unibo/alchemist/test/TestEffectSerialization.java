/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.danilopianini.io.FileUtilities;
import org.junit.Test;

import it.unibo.alchemist.boundary.gui.effects.Effect;
import it.unibo.alchemist.boundary.gui.effects.EffectFactory;
import it.unibo.alchemist.boundary.gui.effects.EffectSerializationFactory;
import it.unibo.alchemist.boundary.wormhole.implementation.Wormhole2D;

/**
 * Test for bugs in {@link Wormhole2D}.
 */
public class TestEffectSerialization {

    private static final String FILEPATH = "test.aes";
    private static final File FILE = new File(FILEPATH);
    private static final Effect E = EffectFactory.buildDefaultEffect();

    /**
     * Make sure that effects can be (de) serialized with Gson.
     * 
     * @throws IOException
     *             in case of errors
     * @throws ClassNotFoundException
     *             if some very serious bug happen
     */
    @Test
    public void testGsonEffectSerialization() throws IOException, ClassNotFoundException {
        EffectSerializationFactory.effectToFile(FILE, E);
        EffectSerializationFactory.effectsFromFile(FILE);
    }

    /**
     * Make sure that effects can be (de) serialized.
     * 
     * @throws IOException
     *             in case of errors
     * @throws ClassNotFoundException
     *             if some very serious bug happen
     */
    @Test
    public void testDefaultEffectSerialization() throws IOException, ClassNotFoundException {
        FileUtilities.objectToFile(E, FILEPATH, false);
        FileUtilities.fileToObject(FILEPATH);
    }

    /**
     * Make sure that effects can be (de) serialized even if they are binary
     * file.
     * 
     * @throws IOException
     *             in case of errors
     * @throws ClassNotFoundException
     *             if some very serious bug happen
     */
    @Test
    public void testBackwardCompatibilitySingleEffect() throws IOException, ClassNotFoundException {
        FileUtilities.objectToFile(E, FILEPATH, false);
        EffectSerializationFactory.effectsFromFile(FILE);
    }

    /**
     * Make sure that effects can be (de) serialized even if they are binary
     * file.
     * 
     * @throws IOException
     *             in case of errors
     * @throws ClassNotFoundException
     *             if some very serious bug happen
     */
    @Test
    public void testBackwardCompatibilityEffects() throws IOException, ClassNotFoundException {
        final List<Effect> effects = new ArrayList<Effect>();
        effects.add(E);
        FileUtilities.objectToFile(effects, FILE, false);
        EffectSerializationFactory.effectsFromFile(FILE);
    }
}
