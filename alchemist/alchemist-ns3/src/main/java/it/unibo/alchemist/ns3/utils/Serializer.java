/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.ns3.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A serializer is a object used to serialize and deserialize objects to and from a stream.
 */
public interface Serializer {

    /**
     * @param toSend the object to be serialized and sent
     * @param outputStream The {@link OutputStream} used to send the data
     *
     * @throws IOException if the underlying stream fails
     */
    void serializeAndSend(Object toSend, OutputStream outputStream) throws IOException;

    /**
     * @param inputStream the input stream from which the object is received
     * @return the deserialized object received from the given input stream
     *
     * @throws IOException if the underlying stream fails
     * @throws ClassNotFoundException if no definition for the class with the specified name could be found
     */
    Object deserialize(InputStream inputStream) throws IOException, ClassNotFoundException;
}
