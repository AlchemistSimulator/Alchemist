package it.unibo.alchemist.loader.displacements;

import java.util.stream.Stream;

import org.apache.commons.math3.random.RandomGenerator;

import it.unibo.alchemist.loader.variables.LinearVariable;
import it.unibo.alchemist.loader.variables.Variable;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Position;

/**
 * A (possibly randomized) grid of nodes.
 */
public class Grid implements Displacement {

    private final Environment<?> pm;
    private final RandomGenerator rand;
    private final Variable xVar, yVar;
    private final double xrand, yrand;

    /**
     * @param pm
     *            the {@link Environment}
     * @param rand
     *            the {@link RandomGenerator}
     * @param xstart
     *            the start x position
     * @param ystart
     *            the start y position
     * @param xend
     *            the end x position
     * @param yend
     *            the end y position
     * @param xstep
     *            how distant on the x axis (on average) nodes should be
     * @param ystep
     *            how distant on the y axis (on average) nodes should be
     * @param xrand
     *            how randomized should be positions along the x axis
     * @param yrand
     *            how randomized should be positions along the y axis
     */
    public Grid(final Environment<?> pm,
            final RandomGenerator rand,
            final double xstart,
            final double ystart,
            final double xend,
            final double yend,
            final double xstep,
            final double ystep,
            final double xrand,
            final double yrand) {
        this.pm = pm;
        this.rand = rand;
        xVar = new LinearVariable(xstart, xstart, xend, xstep);
        yVar = new LinearVariable(ystart, ystart, yend, ystep);
        this.xrand = xrand;
        this.yrand = yrand;
    }

    @Override
    public Stream<Position> stream() {
        return yVar.stream()
                .mapToObj(Double::new)
                .flatMap(y -> xVar.stream()
                    .mapToObj(x -> {
                        final double dx = xrand * (rand.nextDouble() - 0.5);
                        final double dy = yrand * (rand.nextDouble() - 0.5);
                        return pm.makePosition(x + dx, y + dy);
                    }));
    }

}
