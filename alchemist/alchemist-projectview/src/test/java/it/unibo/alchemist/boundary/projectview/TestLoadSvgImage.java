package it.unibo.alchemist.boundary.projectview;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import it.unibo.alchemist.boundary.gui.utility.SVGImageUtils;
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
        final InputStream imageData = TestLoadSvgImage.class.getClassLoader().getResourceAsStream("icon/testicon.svg");
        Assert.assertNotNull(imageData);
        final Image image = new Image(imageData);
        Assert.assertNotNull(image);
    }

    /**
     * 
     */
    @Test
    public void testUtilityClass() {
        final Image image1 = SVGImageUtils.getSvgImage("icon/testicon.svg");
        Assert.assertNotNull(image1);

        final Image image2 = SVGImageUtils.getSvgImage("icon/testicon.svg", 1.0, 1.0);
        Assert.assertNotNull(image2);
    }

}
