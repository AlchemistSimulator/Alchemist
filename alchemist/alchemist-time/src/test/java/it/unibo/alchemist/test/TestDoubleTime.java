/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Time;

import org.junit.Test;

/**
 */
public class TestDoubleTime {

    /**
     * 
     */
    @Test
    public void testDoubleTime() {
        final Time t = new DoubleTime(1);
        assertNotNull(t);
        assertEquals(new DoubleTime(1), t);
        assertNotEquals(new DoubleTime(), t);
        assertEquals(new DoubleTime(1).hashCode(), t.hashCode());
    }

}
