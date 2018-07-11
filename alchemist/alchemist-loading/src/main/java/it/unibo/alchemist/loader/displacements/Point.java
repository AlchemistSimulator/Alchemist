package it.unibo.alchemist.loader.displacements;

import java.util.stream.Stream;

import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Position;

/**
 * A single node in a single point.
 */
public class Point<P extends Position<? extends P>> implements Displacement<P> {

    private final double x, y;
    private final Environment<?, P> pm;

    /**
     * @param pm
     *            The {@link Environment}
     * @param x
     *            x coordinate
     * @param y
     *            y coordinate
     */
    public Point(final Environment<?, P> pm, final double x, final double y) {
        this.x = x;
        this.y = y;
        this.pm = pm;
    }

    @Override
    public Stream<P> stream() {
        return Stream.of(pm.makePosition(x, y));
    }

}
