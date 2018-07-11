/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.loader.variables;

import java.util.stream.Stream;

/**
 * This variable is a flag. Being booleans not a valid data type in charts, this
 * variable just outputs 0 and 1. This is equivalent to a {@link LinearVariable}
 * with two samples ranging from 0 to 1.
 */
public class Flag extends PrintableVariable<Boolean> {

    private static final long serialVersionUID = 1L;
    private final boolean defVal;

    /**
     * @param def
     *            the default value
     */
    public Flag(final boolean def) {
        this.defVal = def;
    }

    @Override
    public Boolean getDefault() {
        return defVal;
    }

    @Override
    public Stream<Boolean> stream() {
        return Stream.of(true, false);
    }

}
