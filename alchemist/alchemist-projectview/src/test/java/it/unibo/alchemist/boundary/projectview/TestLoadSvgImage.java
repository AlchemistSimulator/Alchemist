package it.unibo.alchemist.boundary.projectview;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import it.unibo.alchemist.boundary.gui.utility.ResourceLoader;
import javafx.scene.image.Image;

/**
 * The the use of SVG images.
 *
 */
public class TestLoadSvgImage {

    /**
     * 
     */
    @Test
    public void testImage() {
        final InputStream imageData = ResourceLoader.load("/icon/testicon.svg");
        Assert.assertNotNull(imageData);
        final Image image = new Image(imageData);
        Assert.assertNotNull(image);
    }

}
