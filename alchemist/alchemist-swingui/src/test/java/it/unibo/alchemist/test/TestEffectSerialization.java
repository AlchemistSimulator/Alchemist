package it.unibo.alchemist.test;

import java.io.IOException;

import org.danilopianini.io.FileUtilities;
import org.junit.Test;

import it.unibo.alchemist.boundary.gui.effects.Effect;
import it.unibo.alchemist.boundary.gui.effects.EffectFactory;
import it.unibo.alchemist.boundary.wormhole.implementation.Wormhole2D;

/**
 * Test for bugs in {@link Wormhole2D}.
 */
public class TestEffectSerialization {

    private static final String FILEPATH = "test.aes";

    /**
     * Make sure that effects can be (de) serialized.
     * 
     * @throws IOException in case of errors
     * @throws ClassNotFoundException if some very serious bug happen
     */
    @Test
    public void testDefaultEffectSerialization() throws IOException, ClassNotFoundException {
        final Effect e = EffectFactory.buildDefaultEffect();
        FileUtilities.objectToFile(e, FILEPATH, false);
        FileUtilities.fileToObject(FILEPATH);
    }

}
