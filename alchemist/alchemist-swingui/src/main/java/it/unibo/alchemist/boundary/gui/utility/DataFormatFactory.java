package it.unibo.alchemist.boundary.gui.utility;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import javafx.scene.input.DataFormat;

/**
 * Simple factory that returns the {@link DataFormat} for the specified class.
 * <p>
 * The DataFormat is cached to return only one per class and avoid
 * {@code IllegalArgumentException: DataFormat 'xxx' already exists}.
 * <p>
 * 
 * @see <a href=
 *      "https://bugs.openjdk.java.net/browse/JDK-8118672?focusedCommentId=13730400&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-13730400">Issue
 *      JDK-8118672</a>
 * 
 */
public final class DataFormatFactory {
    /**
     * Static {@link LoadingCache} for a single {@link DataFormat} per class
     * loaded.
     */
    private static final LoadingCache<Class<?>, DataFormat> DATA_FORMATS = CacheBuilder.newBuilder()
            .build(new CacheLoader<Class<?>, DataFormat>() {
                @Override
                public DataFormat load(final Class<?> key) {
                    return new DataFormat(key.getName());
                }
            });

    /**
     * Static {@link DataFormat} loader for the specified class.
     * 
     * @param object
     *            the object you want the {@code DataFormat} for
     * @return the {@code DataFormat}
     */
    public static DataFormat getDataFormat(final Object object) {
        return DATA_FORMATS.getUnchecked(object.getClass());
    }

    /**
     * Static {@link DataFormat} loader for the specified class.
     * 
     * @param clazz
     *            the class you want the {@code DataFormat} for
     * @return the {@code DataFormat}
     */
    public static DataFormat getDataFormat(final Class<?> clazz) {
        return DATA_FORMATS.getUnchecked(clazz);
    }

    /**
     * Default, empty, constructor, as this is an utility class.
     */
    private DataFormatFactory() {
        // Empty constructor
    }

}
