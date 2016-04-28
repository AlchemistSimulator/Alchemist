package it.unibo.alchemist.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import it.unibo.alchemist.loader.shapes.Rectangle;
import it.unibo.alchemist.loader.shapes.Shape;
import it.unibo.alchemist.model.implementations.positions.LatLongPosition;

/**
 *
 */
public class TestRectangle {

    /**
     * 
     */
    @Test
    public void test() {
        final Shape s = new Rectangle(12, 44, 1, 1);
        // CHECKSTYLE:OFF
        assertTrue(s.contains(new LatLongPosition(44.132300,12.233000)));
        // CHECKSTYLE:ON
    }

}
