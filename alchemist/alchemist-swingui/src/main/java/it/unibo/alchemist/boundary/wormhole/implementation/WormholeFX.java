package it.unibo.alchemist.boundary.wormhole.implementation;

import it.unibo.alchemist.boundary.wormhole.interfaces.IWormhole2D;
import it.unibo.alchemist.model.interfaces.Environment;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.geom.Dimension2D;

import static it.unibo.alchemist.boundary.wormhole.implementation.PointAdapter.from;

/**
 * Partial implementation for the interface {@link IWormhole2D} for a {@link Node JavaFX view}.
 * <br/>
 * I am considering the particular case of the view as an entity into the
 * sceern-space: the y-axis grows on the bottom side of the screen.
 */
public class WormholeFX extends AbstractWormhole<Node> {
    /**
     * Default logger.
     */
    private static final Logger L = LoggerFactory.getLogger(WormholeFX.class);

    /**
     * Default constructor.
     * <br/>
     * Initializes a new instance directly setting the size of both view and
     * environment, and the offset too.
     *
     * @param environment the {@code Environment}
     * @param view        the controlled {@code Node}
     */
    public WormholeFX(final Environment<?> environment, final Node view) {
        super(environment,
                view,
                from(view.getBoundsInLocal().getWidth() / 2, view.getBoundsInLocal().getHeight() / 2)
        );
    }

    /**
     * {@inheritDoc}
     * <p>
     * {@link Dimension2D#setSize(double, double) setSize()} method in returned object throws {@link UnsupportedOperationException}
     *
     * @see #getViewBounds()
     */
    @Override
    public Dimension2D getViewSize() {
        return new Dimension2D() {
            @Override
            public double getWidth() {
                return getViewBounds().getWidth();
            }

            @Override
            public double getHeight() {
                return getViewBounds().getHeight();
            }

            @Override
            public void setSize(double v, double v1) throws UnsupportedOperationException {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Gets the size of the view as bounds.
     *
     * @return the bounds of the view
     * @see #getViewSize()
     * @see Node#getBoundsInLocal()
     */
    public Bounds getViewBounds() {
        return getView().getBoundsInLocal();
    }

    @Override
    public void optimalZoom() {
        if (getEnvRatio() <= getViewRatio()) {
            setZoom(Math.max(1, getViewBounds().getHeight()) / getEnvironment().getSize()[1]);
        } else {
            setZoom(Math.max(1, getViewBounds().getWidth()) / getEnvironment().getSize()[0]);
        }
    }

    @Override
    protected Logger getLogger() {
        return WormholeFX.L;
    }

    /**
     * {@inheritDoc}
     *
     * @see #getViewBounds()
     */
    @Override
    protected double getViewRatio() {
        return Math.max(1, getViewBounds().getWidth()) / Math.max(1, getViewBounds().getHeight());
    }
}
