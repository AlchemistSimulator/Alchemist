/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.grid.simulation;

import java.io.FileNotFoundException;

/**
 * Result of {@link RemoteSimulation}.
 *
 */
public interface RemoteResult {
    /**
     * Save simulation's result in a local file.
     * @param targetFile Local file
     * @throws FileNotFoundException In case file is not found
     *
     */
    void saveLocally(String targetFile) throws FileNotFoundException;
}
