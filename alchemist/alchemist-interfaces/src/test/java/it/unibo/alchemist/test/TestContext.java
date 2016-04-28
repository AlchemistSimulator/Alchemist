/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import it.unibo.alchemist.model.interfaces.Context;

import org.junit.Test;


/**
 */
public class TestContext {

    /**
     * 
     */
    @Test
    public final void test() {
        assertTrue(Context.LOCAL.isMoreStrict(Context.NEIGHBORHOOD));
        assertTrue(Context.LOCAL.isMoreStrict(Context.GLOBAL));
        assertTrue(Context.NEIGHBORHOOD.isMoreStrict(Context.GLOBAL));
        assertFalse(Context.NEIGHBORHOOD.isMoreStrict(Context.LOCAL));
        assertFalse(Context.GLOBAL.isMoreStrict(Context.LOCAL));
        assertFalse(Context.GLOBAL.isMoreStrict(Context.NEIGHBORHOOD));
        /*
         * If the context is the same, this method can return whatever. The
         * following test are just to ensure full line coverage.
         */
        assertTrue(Context.GLOBAL.isMoreStrict(Context.GLOBAL)
                || !Context.GLOBAL.isMoreStrict(Context.GLOBAL));
        assertTrue(Context.NEIGHBORHOOD.isMoreStrict(Context.NEIGHBORHOOD)
                || !Context.NEIGHBORHOOD.isMoreStrict(Context.NEIGHBORHOOD));
        assertTrue(Context.LOCAL.isMoreStrict(Context.LOCAL)
                || !Context.LOCAL.isMoreStrict(Context.LOCAL));
    }

}
