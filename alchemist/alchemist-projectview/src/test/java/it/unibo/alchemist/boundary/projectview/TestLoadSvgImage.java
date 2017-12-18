package it.unibo.alchemist.boundary.projectview;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.kaikikm.threadresloader.ResourceLoader;

import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory;
import javafx.scene.image.Image;

/**
 * The the use of SVG images.
 *
 */
public class TestLoadSvgImage {

    /**
     * 
     */
    public static void installSvgLoader() {
        SvgImageLoaderFactory.install();
    }

    /**
     * 
     */
    @Test
    public void testImage() {
        final InputStream imageData = ResourceLoader.getResourceAsStream("icon/testicon.svg");
        Assert.assertNotNull(imageData);
        final Image image = new Image(imageData);
        Assert.assertNotNull(image);
    }

}
