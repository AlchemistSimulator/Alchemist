/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import it.unibo.alchemist.exceptions.UncomparableDistancesException;

import org.junit.Test;


/**
 */
public class TestUncomparableDistances {

    /**
     * 
     */
    @Test
    public void test() {
        final UncomparableDistancesException e = new UncomparableDistancesException(null, null);
        assertNotNull(e);
        assertNull(e.getP1());
        assertNull(e.getP2());
    }

}
