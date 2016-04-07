package it.unibo.alchemist.loader.displacements;

import org.apache.commons.math3.random.RandomGenerator;

import it.unibo.alchemist.loader.PositionMaker;
import it.unibo.alchemist.model.interfaces.Position;

/**
 *
 */
public class Rectangle extends AbstractRandomDisplacement {

    private final double x, y, width, height;

    /**
     * @param pm
     *            the {@link PositionMaker}
     * @param rand
     *            the {@link RandomGenerator}
     * @param nodes
     *            the number of nodes
     * @param x
     *            x start point
     * @param y
     *            y start point
     * @param sizex
     *            x size
     * @param sizey
     *            y size
     */
    public Rectangle(final PositionMaker pm, final RandomGenerator rand,
            final int nodes,
            final double x, final double y,
            final double sizex, final double sizey) {
        super(pm, rand, nodes);
        this.x = x;
        this.y = y;
        this.width = sizex;
        this.height = sizey;
    }

    @Override
    protected Position indexToPosition(final int i) {
        return makePosition(randomDouble(x, x + width), randomDouble(y, y + height));
    }

}
