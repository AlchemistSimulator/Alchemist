/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.protelis.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Serializer {

    /**
     * @param toSend the object to be serialized and sent
     */
    void serializeAndSend(final Object toSend, final OutputStream outputStream) throws IOException;

    /**
     * @param inputStream the input stream from which the object is received
     * @return the deserialized object received from the given input stream
     */
    Object deserialize(final InputStream inputStream) throws IOException, ClassNotFoundException;
}
