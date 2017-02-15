/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import static org.junit.Assert.assertNotNull;
import it.unibo.alchemist.core.interfaces.Status;

import org.junit.Test;


/**
 */
public class TestStatus {

    /**
     * 
     */
    @Test
    public void test() {
        assertNotNull(Status.PAUSED);
        assertNotNull(Status.RUNNING);
        assertNotNull(Status.TERMINATED);
    }

}
