/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.expressions.implementations;

/**
 */
public enum ListComparator {

    /**
     * The list is empty.
     */
    EMPTY,
    /**
     * The list is entirely included in the other list.
     */
    HAS,
    /**
     * The list is not entirely included in the other list.
     */
    HAS_NOT,
    /**
     * The list contains at least an element.
     */
    NOT_EMPTY

}
