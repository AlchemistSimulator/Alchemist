package it.unibo.alchemist.boundary.wormhole.implementation;

import it.unibo.alchemist.boundary.wormhole.implementation.adapter.NodeViewType;
import it.unibo.alchemist.boundary.wormhole.interfaces.Wormhole2D;
import it.unibo.alchemist.model.interfaces.Environment;
import javafx.scene.Node;
import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.geom.Dimension2D;

import static it.unibo.alchemist.boundary.wormhole.implementation.PointAdapter.from;

/**
 * Partial implementation for the interface {@link Wormhole2D} for a {@link Node JavaFX view}.
 * <br/>
 * I am considering the particular case of the view as an entity into the
 * sceern-space: the y-axis grows on the bottom side of the screen.
 */
public class WormholeFX extends AbstractWormhole {

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
                new NodeViewType(view),
                from(view.getBoundsInLocal().getWidth() / 2, view.getBoundsInLocal().getHeight() / 2)
        );
    }

    /**
     * {@inheritDoc}
     * <p>
     * {@link Dimension2D#setSize(double, double) setSize()} method in returned object throws {@link UnsupportedOperationException}
     */
    @Override
    public Tuple2<Double, Double> getViewSize() {
        return new Tuple2<>(getView().getWidth(), getView().getHeight());
    }

    @Override
    public void optimalZoom() {
        if (getEnvRatio() <= getViewRatio()) {
            setZoom(Math.max(1, getView().getHeight()) / getEnvironment().getSize()[1]);
        } else {
            setZoom(Math.max(1, getView().getWidth()) / getEnvironment().getSize()[0]);
        }
    }

    @Override
    protected Logger getLogger() {
        return WormholeFX.L;
    }

    @Override
    protected double getViewRatio() {
        return Math.max(1, getView().getWidth()) / Math.max(1, getView().getHeight());
    }
}
