/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.expressions.implementations;

/**
 */
public enum Operator {
    /**
     * Add list to list.
     */
    ADD,
    /**
     * Delete sublist from list.
     */
    DEL,
    /**
     * Division.
     */
    DIV,
    /**
     * Maximum.
     */
    MAX,
    /**
     * Minimum.
     */
    MIN,
    /**
     * Subtraction.
     */
    MINUS,
    /**
     * Absolute value.
     */
    MOD,
    /**
     * Sum.
     */
    PLUS,
    /**
     * Multiplication.
     */
    TIMES;

    @Override
    public String toString() {
        switch (this) {
        case PLUS:
            return "+";
        case MINUS:
            return "-";
        case TIMES:
            return "*";
        case DIV:
            return "/";
        case MIN:
            return "min";
        case MAX:
            return "max";
        case MOD:
            return "abs";
        case ADD:
            return "add";
        case DEL:
            return "del";
        default:
            return "unknown";
        }
    }
}
