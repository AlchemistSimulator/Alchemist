/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.test;

import static org.junit.Assert.assertEquals;
import static it.unibo.alchemist.model.interfaces.Context.LOCAL;
import static it.unibo.alchemist.model.interfaces.Context.NEIGHBORHOOD;
import static it.unibo.alchemist.model.interfaces.Context.GLOBAL;
import static it.unibo.alchemist.model.interfaces.Context.getWider;

import org.junit.Test;


/**
 */
public class TestContext {

    /**
     * 
     */
    @Test
    public final void test() {
        assertEquals(LOCAL, getWider(LOCAL, LOCAL));
        assertEquals(NEIGHBORHOOD, getWider(LOCAL, NEIGHBORHOOD));
        assertEquals(GLOBAL, getWider(LOCAL, GLOBAL));
        assertEquals(NEIGHBORHOOD, getWider(NEIGHBORHOOD, LOCAL));
        assertEquals(NEIGHBORHOOD, getWider(NEIGHBORHOOD, NEIGHBORHOOD));
        assertEquals(GLOBAL, getWider(NEIGHBORHOOD, GLOBAL));
        assertEquals(GLOBAL, getWider(GLOBAL, LOCAL));
        assertEquals(GLOBAL, getWider(GLOBAL, NEIGHBORHOOD));
        assertEquals(GLOBAL, getWider(GLOBAL, GLOBAL));
    }

}
