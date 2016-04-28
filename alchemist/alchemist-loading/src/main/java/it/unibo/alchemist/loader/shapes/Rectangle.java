package it.unibo.alchemist.loader.shapes;

import java.awt.geom.Rectangle2D;

/**
 * A Rectangle.
 */
public class Rectangle extends Abstract2DShape {

    /**
     * @param x
     *            start x point
     * @param y
     *            start y point
     * @param w
     *            width
     * @param h
     *            height
     */
    public Rectangle(final double x, final double y, final double w, final double h) {
        super(new Rectangle2D.Double(x, y, w, h));
    }

}
