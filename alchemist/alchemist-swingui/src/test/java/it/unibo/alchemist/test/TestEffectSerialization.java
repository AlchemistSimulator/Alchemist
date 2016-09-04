package it.unibo.alchemist.test;

import java.io.File;
import java.io.IOException;

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
     */
    @Test
    public void testGsonEffectSerialization() throws IOException {
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

}
