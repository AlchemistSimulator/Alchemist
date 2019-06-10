/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.gpsload.api;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.openstreetmap.osmosis.osmbinary.file.FileFormatException;
import it.unibo.alchemist.model.interfaces.GPSTrace;

/**
 * Strategy to read GPSTrace from file.
 */
public interface GPSFileLoader {

    /**
     * 
     * @param url file with the trace request
     * @return GPSTrace readed
     * @throws FileFormatException file format not valid
     * @throws IOException 
     */
    List<GPSTrace> readTrace(URL url) throws FileFormatException, IOException;

    /**
     * 
     * @return all extension supported by this loader
     */
    Collection<String> supportedExtensions();

}
