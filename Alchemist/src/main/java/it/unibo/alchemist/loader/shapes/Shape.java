package it.unibo.alchemist.loader.shapes;

import it.unibo.alchemist.model.interfaces.Position;

/**
 * A Shape, representing an partition of the space where a {@link Position} may
 * lie in.
 */
@FunctionalInterface
public interface Shape {

    /**
     * @param position
     *            the position
     * @return true if the position is inside the {@link Shape}.
     */
    boolean contains(Position position);

}
