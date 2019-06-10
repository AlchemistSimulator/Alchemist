/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import it.unibo.alchemist.model.implementations.molecules.LsaMolecule;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;

import org.junit.jupiter.api.Test;

/**
 */
public class TestLsaMolecule {

    private static final String ABSURD_DESCRIPTION = "Q|\\!\"£$%&/()=?^[]@ł€¶ŧ←↓→øþæßðđŋħł@«»¢“”ñµŁ¢®Ŧ¥↑ıØÞ{}&ŊªÐ§Æ»<>©‘’Ñº×";

    /**
     * 
     */
    @Test
    public void test() {
        final ILsaMolecule empty = new LsaMolecule();
        assertNotNull(empty);
        final ILsaMolecule parsedGround = new LsaMolecule("a,b,c,[a;b;c;],d");
        assertNotNull(parsedGround);
        final ILsaMolecule parsedVars = new LsaMolecule("A,B,C,[A;b;c;],D");
        assertNotNull(parsedVars);
        final ILsaMolecule withDescription = new LsaMolecule("a,b,c,[a;b;c;]", ABSURD_DESCRIPTION);
        assertNotNull(withDescription);
        assertTrue(withDescription.getArg(withDescription.argsNumber() - 1).toString().equals(ABSURD_DESCRIPTION));

        /*
         * Matching tests
         */
        assertFalse(empty.matches(parsedGround));
        assertFalse(empty.matches(parsedVars));
        assertFalse(empty.matches(withDescription));

        assertFalse(parsedGround.matches(empty));
        assertTrue(parsedGround.matches(parsedVars));
        assertFalse(parsedGround.matches(withDescription));

        assertFalse(parsedVars.matches(empty));
        assertTrue(parsedVars.matches(parsedGround));
        assertTrue(parsedVars.matches(withDescription));

        assertFalse(withDescription.matches(empty));
        assertFalse(withDescription.matches(parsedGround));
        assertTrue(withDescription.matches(parsedVars));

        /*
         * Generality test
         */
        assertFalse(empty.moreGenericOf(parsedGround));
        assertFalse(empty.moreGenericOf(parsedVars));
        assertFalse(empty.moreGenericOf(withDescription));

        assertFalse(parsedGround.moreGenericOf(empty));
        assertFalse(parsedGround.moreGenericOf(parsedVars));
        assertFalse(parsedGround.moreGenericOf(withDescription));

        assertFalse(parsedVars.moreGenericOf(empty));
        assertTrue(parsedVars.moreGenericOf(parsedGround));
        assertTrue(parsedVars.moreGenericOf(withDescription));

        assertFalse(withDescription.moreGenericOf(empty));
        assertFalse(withDescription.moreGenericOf(parsedGround));
        assertFalse(withDescription.moreGenericOf(parsedVars));
    }

}
