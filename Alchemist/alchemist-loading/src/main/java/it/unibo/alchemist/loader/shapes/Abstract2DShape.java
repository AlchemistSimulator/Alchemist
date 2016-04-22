package it.unibo.alchemist.loader.shapes;

import it.unibo.alchemist.model.interfaces.Position;

/**
 * A bidimensional Alchemist {@link Shape} that relies on AWT {@link java.awt.Shape}.
 */
public abstract class Abstract2DShape implements Shape {

    private final java.awt.Shape shape;

    /**
     * @param shape any Java AWT {@link java.awt.Shape}
     */
    protected Abstract2DShape(final java.awt.Shape shape) {
        this.shape = shape;
    }

    @Override
    public boolean contains(final Position position) {
        if (position.getDimensions() != 2) {
            throw new IllegalArgumentException("Only bidimensional positions are accepted by this " + Abstract2DShape.class.getName());
        }
        return shape.contains(position.getCoordinate(0), position.getCoordinate(1));
    }

    @Override
    public String toString() {
        return shape.toString();
    }

}
