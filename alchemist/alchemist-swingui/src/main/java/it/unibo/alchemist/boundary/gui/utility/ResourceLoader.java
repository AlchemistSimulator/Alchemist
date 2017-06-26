package it.unibo.alchemist.boundary.gui.utility;

import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * This utility class provides a working file loading everywhere in the project,
 * and both in source running and jar running.
 */
public final class ResourceLoader {
    /** Resource bundle. */
    private static final String RESOURCE_BUNDLE = "it.unibo.alchemist.l10n.FXUIStrings";

    /** Empty, private, constructor, as this is an utility class. */
    private ResourceLoader() {
        // Empty private constructor
    }

    /**
     * Static method that wraps class.getResourceAsStream() method to return an
     * input stream form a specified path.
     * <p>
     * It also tries to fix wrongly specified paths.
     * 
     * @param path
     *            the path to the file
     * @return An {@link InputStream} object or null if no resource with this
     *         name is found
     */
    public static InputStream load(final String path) {
        String newPath = path;

        if (path.toCharArray()[0] != '/') {
            newPath = "/" + newPath;
        }

        return ResourceLoader.class.getResourceAsStream(newPath);
    }

    /**
     * Static method that wraps class.getResource() method to return an URL from
     * a specified path.
     * <p>
     * It also tries to fix wrongly specified paths.
     * 
     * @param path
     *            the path to the file
     * @return An {@link URL} object or null if no resource with this name is
     *         found
     */
    public static URL loadURL(final String path) {
        String newPath = path;

        if (path.toCharArray()[0] != '/') {
            newPath = "/" + newPath;
        }

        return ResourceLoader.class.getResource(newPath);
    }

    /**
     * Looks up on the property files and returns the correct String.
     * 
     * @param key
     *            the key
     * @return the String
     */
    public static String getStringRes(final String key) {
        return ResourceBundle.getBundle(RESOURCE_BUNDLE).getString(key);
    }
}