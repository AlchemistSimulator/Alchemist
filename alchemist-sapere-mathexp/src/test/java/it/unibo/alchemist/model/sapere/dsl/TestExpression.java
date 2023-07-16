/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.sapere.dsl;

// CHECKSTYLE:OFF

import it.unibo.alchemist.model.sapere.dsl.impl.Expression;
import it.unibo.alchemist.model.sapere.dsl.impl.Type;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 */
class TestExpression {

    private static final String[] VAR = {
            "A", "ASD", "Asd"
    };
    private static final String[] CONST = {
            "a", "asd", "dsd123"
    };
    private static final String[] OPERATOR = {
            "1+2", "((1+9)*(3-4))/5", "L add L2", "L del L2"
    };
    private static final String[] LIST = {
            "[a;b;c;]", "[1;2;3;]", "[A;b;1;]"
    };
    private static final String[] COMPARATOR = {
            "def: A < 10", "def: A <= 10", "def: A = 10", "def: A >= 10", "def: A != 10"
    };

    /**
     * 
     */
    @Test
    void testBuildVars() {
        for (final String s : VAR) {
            final Expression e = new Expression(s);
            assertNotNull(e);
            assertTrue(e.getRootNodeType().equals(Type.VAR));
        }
    }

    /**
     * 
     */
    @Test
    void testBuildConst() {
        for (final String s : CONST) {
            final Expression e = new Expression(s);
            assertNotNull(e);
            assertTrue(e.getRootNodeType().equals(Type.CONST));
        }
    }

    /**
     * 
     */
    @Test
    void testBuildOperator() {
        for (final String s : OPERATOR) {
            final Expression e = new Expression(s);
            assertNotNull(e);
            assertTrue(e.getRootNodeType().equals(Type.OPERATOR));
        }
    }

    /**
     * 
     */
    @Test
    void testBuildList() {
        for (final String s : LIST) {
            final Expression e = new Expression(s);
            assertNotNull(e);
            assertTrue(e.getRootNodeType().equals(Type.LIST));
        }
    }

    /**
     * 
     */
    @Test
    void testBuildComparator() {
        for (final String s : COMPARATOR) {
            final Expression e = new Expression(s);
            assertNotNull(e);
            assertTrue(e.getRootNodeType().equals(Type.COMPARATOR));
        }
    }

    /**
     * 
     */
    @Test
    void testListComparator() {
        final Expression le = new Expression("def: A has [a;]");
        assertFalse(le.matches(new Expression("A"), null));
        assertFalse(le.matches(new Expression("B"), null));
        assertTrue(le.matches(new Expression("[a;]"), null));
        assertTrue(le.matches(new Expression("[a;b;]"), null));
        assertTrue(new Expression("[a;]").matches(le, null));
        assertTrue(new Expression("[a;b;]").matches(le, null));
        assertFalse(le.matches(new Expression("[b;]"), null));
        assertFalse(new Expression("[b;]").matches(le, null));
        assertFalse(new Expression("a").matches(le, null));
        assertFalse(new Expression("b").matches(le, null));
        assertFalse(new Expression("A").matches(le, null));
        assertFalse(new Expression("B").matches(le, null));
    }

    /**
     * 
     */
    @Test
    void testList() {
        final Expression empty = new Expression("[]");
        final Expression le = new Expression("[a;b;c;]");
        assertTrue(le.matches(new Expression("A"), null));
        assertTrue(le.matches(new Expression("B"), null));
        assertTrue(empty.matches(new Expression("def: Empty isempty"), null));
        assertTrue(le.matches(new Expression("def: NotEmpty notempty"), null));
        assertTrue(new Expression("[a;b;c;]").matches(le, null));
        assertTrue(new Expression("[b;c;a;]").matches(le, null));
        assertTrue(new Expression("[A;B;C;]").matches(le, null));
        assertTrue(new Expression("[A;B;c;]").matches(le, null));
        assertTrue(new Expression("[A;b;c;]").matches(le, null));
        assertTrue(new Expression("[a;B;c;]").matches(le, null));
        assertTrue(new Expression("[a;b;c;c;a;b;]").matches(le, null));
        assertFalse(le.matches(new Expression("[a;B;]"), null));
        assertFalse(le.matches(new Expression("def: NotEmpty isempty"), null));
        assertFalse(empty.matches(new Expression("def: Empty notempty"), null));
        assertFalse(new Expression("[A;B;C;D;]").matches(le, null));
        assertFalse(le.matches(new Expression("[a;b;d;]"), null));
    }

}
