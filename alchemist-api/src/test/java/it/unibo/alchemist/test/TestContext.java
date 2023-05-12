/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import org.junit.jupiter.api.Test;

import static it.unibo.alchemist.model.Context.GLOBAL;
import static it.unibo.alchemist.model.Context.LOCAL;
import static it.unibo.alchemist.model.Context.NEIGHBORHOOD;
import static it.unibo.alchemist.model.Context.getWider;
import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 */
class TestContext {

    /**
     * 
     */
    @Test
    final void test() {
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
