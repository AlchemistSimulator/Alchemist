/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import it.unibo.alchemist.boundary.swingui.effect.api.Effect;
import it.unibo.alchemist.boundary.swingui.effect.impl.EffectFactory;
import it.unibo.alchemist.boundary.swingui.effect.impl.EffectSerializationFactory;
import it.unibo.alchemist.util.ClassPathScanner;
import org.danilopianini.io.FileUtilities;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test effect serialization.
 */
class TestEffectSerialization {

    private static final String FILEPATH;
    private static final Effect E = EffectFactory.buildDefaultEffect();

    static {
        try {
            FILEPATH = Files.createTempDirectory("alchemist").toAbsolutePath() + File.separator + "test.aes";
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static final File FILE = new File(FILEPATH);

    /**
     * Make sure that effects can be (de) serialized with Gson.
     * 
     * @throws IOException
     *             in case of errors
     * @throws ClassNotFoundException
     *             if some very serious bug happen
     */
    @Test
    void testGsonEffectSerialization() throws IOException, ClassNotFoundException {
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
    void testDefaultEffectSerialization() throws IOException, ClassNotFoundException {
        FileUtilities.objectToFile(E, FILEPATH, false);
        FileUtilities.fileToObject(FILEPATH);
    }

    @Test
    void loadExisting() throws IOException, ClassNotFoundException {
        final var resources = ClassPathScanner.resourcesMatching(".*sample-effect.json");
        assertEquals(1, resources.size());
        final var file = new File(resources.get(0).getFile());
        assertTrue(file.exists());
        assertNotNull(EffectSerializationFactory.effectsFromFile(file));
    }

}
