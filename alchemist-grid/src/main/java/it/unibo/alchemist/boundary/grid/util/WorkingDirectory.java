/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.grid.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.nio.file.Files;
import java.util.Objects;

import org.apache.commons.io.FileUtils;

/**
 * Class that manage a temp local working directory.
 *
 */
public final class WorkingDirectory implements AutoCloseable {

    private final File directory;

    /**
     * Create new local temp working directory.
     */
    public WorkingDirectory() {
        File tryDirectory;
        try {
            tryDirectory = Files.createTempDirectory("alchemist").toFile();
        } catch (IOException e) {
            throw new IllegalStateException("An error occure while attempting to create a temporary directory", e);
        }
        this.directory = Objects.requireNonNull(tryDirectory);
    }

    /**
     * 
     * @return Temp directory URL
     * @throws MalformedURLException if the directory {@link java.net.URI} form cannot be converted to {@link URL} form
     */
    public URL getDirectoryUrl() throws MalformedURLException {
        return this.directory.toURI().toURL();
    }

    /**
     * Get folder's file content.
     * @param filename File's name
     * @return File's content
     * @throws IOException in case of an I/O error
     */
    public String getFileContent(final String filename) throws IOException {
        final File f = new File(this.getFileAbsolutePath(filename));
        return FileUtils.readFileToString(f, StandardCharsets.UTF_8);
    }

    /**
     * Write multiple files inside the directory.
     * @param files A map with relative paths + files names as keys and file contents as values.
     * @throws IOException in case of an I/O error
     */
    public void writeFiles(final Map<String, byte[]> files) throws IOException {
        for (final Entry<String, byte[]> e : files.entrySet()) {
            final File f = new File(this.directory.getAbsolutePath() + File.separator + e.getKey());
            if (f.getParentFile().exists() || f.getParentFile().mkdirs()) {
                FileUtils.writeByteArrayToFile(f, e.getValue());
            } else {
                throw new IllegalStateException("Could not create directory structure for " + f);
            }
        }
    }

    /**
     * 
     * @param filename File name
     * @return Absolute path for given filename in this directory, even if it doesn't exists
     */
    public String getFileAbsolutePath(final String filename) {
        return this.directory.getAbsolutePath() + File.separator + filename;
    }

    @Override
    public void close() throws IOException {
        FileUtils.deleteDirectory(this.directory);
    }

}
