package it.unibo.alchemist.boundary.gui.utility;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.InputStream;

import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory;
import javafx.scene.image.Image;

/**
 * Utility class that install that wraps {@link SvgImageLoaderFactory}, letting
 * install a SVG loader and return a SVG image.
 */
public final class SVGImageUtils {

    /**
     * Private, empty, constructor, as this is an utility class.
     */
    private SVGImageUtils() {
        // Empty constructor
    }

    /**
     * Returns the Image of a SVG image.
     * <p>
     * Should have called {@link #installSvgLoader()} first.
     * 
     * @param path
     *            The SVG image position
     * @param width
     *            The percent width of image
     * @param height
     *            The percent height of image
     * @return The image
     */
    public static Image getSvgImage(final String path, final double width, final double height) {
        SvgImageLoaderFactory.install();

        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final InputStream imageStream = ResourceLoader.load(path);
        return new Image(imageStream, screenSize.getWidth() * width / 100, screenSize.getHeight() * height / 100, true, true);
    }

    /**
     * Returns the Image of a SVG image.
     * <p>
     * Should have called {@link #installSvgLoader()} first.
     * 
     * @param path
     *            The SVG image position
     * @return The image
     */
    public static Image getSvgImage(final String path) {
        SvgImageLoaderFactory.install();

        final InputStream imageStream = ResourceLoader.load(path);
        return new Image(imageStream);
    }

}
