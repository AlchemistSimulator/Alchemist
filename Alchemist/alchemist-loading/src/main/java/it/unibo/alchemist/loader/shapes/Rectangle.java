package it.unibo.alchemist.loader.shapes;

import java.awt.geom.Rectangle2D;

import it.unibo.alchemist.model.interfaces.Position;

/**
 * A Rectangle.
 */
public class Rectangle implements Shape {

    private final Rectangle2D rect;

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
        rect = new Rectangle2D.Double(x, y, w, h);
    }

    @Override
    public boolean contains(final Position p) {
        assert p.getDimensions() == 2;
        return rect.contains(p.getCoordinate(0), p.getCoordinate(1));
    }

    @Override
    public String toString() {
        return rect.toString();
    }

}
