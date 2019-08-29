package it.unibo.alchemist.boundary.monitors;

import it.unibo.alchemist.boundary.wormhole.interfaces.IWormhole2D;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Position2D;
import it.unibo.alchemist.model.interfaces.environments.BoundariesVisitor;
import it.unibo.alchemist.model.interfaces.environments.RectangularBoundaries;
import org.jetbrains.annotations.NotNull;

import java.awt.Graphics2D;
import java.awt.Point;

/**
 * Draws {@link it.unibo.alchemist.model.interfaces.environments.Boundaries}.
 * @param <P> Position type
 */
public final class BoundariesDrawer<P extends Position2D<P>> implements BoundariesVisitor {
    private final Graphics2D g;
    private final IWormhole2D<P> w;
    private final Environment<?, P> env;

    /**
     * Draws {@link it.unibo.alchemist.model.interfaces.environments.Boundaries}.
     *
     * @param graphics    Graphics2D used to draw the boundaries
     * @param wormhole    Needed to get view points
     * @param environment The environment compatible with the wormhole
     */
    public BoundariesDrawer(@NotNull final Graphics2D graphics, @NotNull final IWormhole2D<P> wormhole, @NotNull final Environment<?, P> environment) {
        g = graphics;
        w = wormhole;
        env = environment;
    }

    @Override
    public void visit(@NotNull final RectangularBoundaries rect) {
        final Point topLeft = w.getViewPoint(env.makePosition(-rect.getWidth() / 2, rect.getHeight() / 2));
        final Point bottomRight = w.getViewPoint(env.makePosition(rect.getWidth() / 2, -rect.getHeight() / 2));
        final Point wh = new Point(bottomRight.x - topLeft.x, bottomRight.y - topLeft.y);
        g.drawRect(topLeft.x, topLeft.y, wh.x, wh.y);
    }
}
