/**
 * 
 */
package it.unibo.alchemist.loader.displacements;

import static org.apache.commons.math3.util.FastMath.PI;
import static org.apache.commons.math3.util.FastMath.cos;
import static org.apache.commons.math3.util.FastMath.sin;
import static org.apache.commons.math3.util.FastMath.sqrt;

import org.apache.commons.math3.random.RandomGenerator;

import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Position;

/**
 *
 */
public class Circle extends AbstractRandomDisplacement {

    private final double centerx, centery, radius;

    /**
     * @param pm
     *            the {@link Environment}
     * @param rand
     *            the {@link RandomGenerator}
     * @param nodes
     *            the number of nodes
     * @param centerx
     *            the center x of the circle
     * @param centery
     *            the center y of the circle
     * @param radius
     *            the radius of the circle
     */
    public Circle(final Environment<?> pm,
            final RandomGenerator rand,
            final int nodes,
            final double centerx, final double centery, final double radius) {
        super(pm, rand, nodes);
        this.centerx = centerx;
        this.centery = centery;
        this.radius = radius;
    }

    @Override
    protected Position indexToPosition(final int i) {
        final double angle = randomDouble(0, 2 * PI);
        final double rad = radius * sqrt(randomDouble());
        return makePosition(centerx + rad * cos(angle), centery + rad * sin(angle));
    }

}
