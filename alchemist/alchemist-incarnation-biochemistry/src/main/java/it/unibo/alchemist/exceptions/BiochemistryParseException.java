/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
/*
 * Copyright (C) 2010-2016, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.exceptions;

/**
 * Represents an exception thrown when parse errors are encountered. 
 */
public class BiochemistryParseException extends RuntimeException {

    private static final long serialVersionUID = -5287091656680353238L;

    /**
     * Construct the exception with the given message.
     * @param message the error message.
     */
    public BiochemistryParseException(final String message) {
        super(message);
    }
}
