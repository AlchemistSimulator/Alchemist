package it.unibo.alchemist.loader.shapes;

import java.awt.geom.Ellipse2D;

/**
 * A circle.
 */
public class Circle extends Abstract2DShape {

    /**
     * @param centerx x coordinate of the center of the circle
     * @param centery y coordinate of the center of the circle
     * @param radius the circle radius
     */
    public Circle(final double centerx, final double centery, final double radius) {
        super(new Ellipse2D.Double(centerx - radius, centery - radius, radius * 2, radius * 2));
        if (radius < 0) {
            throw new IllegalArgumentException("Circle radius must be positive (got: " + radius + ")");
        }
    }

}
