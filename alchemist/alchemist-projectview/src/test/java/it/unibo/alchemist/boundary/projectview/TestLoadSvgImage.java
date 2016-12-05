package it.unibo.alchemist.boundary.projectview;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

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
        final InputStream imageData = TestLoadSvgImage.class.getClassLoader().getResourceAsStream("icon/delete.svg");
        Assert.assertNotNull(imageData);
        final Image image = new Image(imageData);
        Assert.assertNotNull(image);
    }

}
