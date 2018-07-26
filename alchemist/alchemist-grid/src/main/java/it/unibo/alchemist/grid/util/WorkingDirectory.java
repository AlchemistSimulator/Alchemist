/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.grid.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

import com.google.common.io.Files;

/**
 * Class that manage a temp local working directory.
 *
 */
public class WorkingDirectory implements AutoCloseable {

    private final File directory;

    /**
     * Create new local temp working directory.
     */
    public WorkingDirectory() {
        this.directory = Files.createTempDir();
    }

    /**
     * 
     * @return Temp directory URL
     * @throws MalformedURLException 
     */
    public URL getDirectoryUrl() throws MalformedURLException {
        return this.directory.toURI().toURL();
    }

    /**
     * Get folder's file content.
     * @param filename File's name
     * @return File's content
     * @throws IOException 
     */
    public String getFileContent(final String filename) throws IOException {
        final File f = new File(this.getFileAbsolutePath(filename));
        return FileUtils.readFileToString(f, StandardCharsets.UTF_8);
    }

    /**
     * Write multiple files inside the directory.
     * @param files A map with relative paths + files names as kay and files content as value.
     * @throws IOException 
     */
    public void writeFiles(final Map<String, byte[]> files) throws IOException {
        for (final Entry<String, byte[]> e : files.entrySet()) {
            final File f = new File(this.directory.getAbsolutePath() + File.separator + e.getKey());
            f.getParentFile().mkdirs();
            FileUtils.writeByteArrayToFile(f, e.getValue());
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
