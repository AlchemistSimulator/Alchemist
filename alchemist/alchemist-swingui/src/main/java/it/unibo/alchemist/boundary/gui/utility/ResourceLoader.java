package it.unibo.alchemist.boundary.gui.utility;

import java.io.InputStream;
import java.net.URL;

/**
 * This utility class provides a working file loading everywhere in the project,
 * and both in source running and jar running.
 */
public final class ResourceLoader {
    private ResourceLoader() {
        // Empty private constructor
    }

    /**
     * Static method that wraps class.getResourceAsStream() method to return an
     * input stream form a specified path.
     * 
     * @param path
     *            the path to the file
     * @return An {@link InputStream} object or null if no resource with this
     *         name is found
     */
    public static InputStream load(final String path) {
        return ResourceLoader.class.getResourceAsStream(path);
    }

    /**
     * Static method that wraps class.getResource() method to return an URL from
     * a specified path.
     * 
     * @param path
     *            the path to the file
     * @return An {@link URL} object or null if no resource with this name is
     *         found
     */
    public static URL loadURL(final String path) {
        return ResourceLoader.class.getResource(path);
    }
}