package it.unibo.alchemist.boundary.wormhole.implementation;

import static it.unibo.alchemist.boundary.wormhole.implementation.PointAdapter.from;

import java.awt.Component;

import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.alchemist.boundary.wormhole.implementation.adapter.ComponentViewType;
import it.unibo.alchemist.boundary.wormhole.implementation.adapter.NodeViewType;
import it.unibo.alchemist.boundary.wormhole.interfaces.BidimensionalWormhole;
import it.unibo.alchemist.model.interfaces.Environment;
import javafx.scene.Node;

/**
 * Partial implementation for the interface {@link BidimensionalWormhole} for a {@link Node JavaFX view}.
 * <br/>
 * This considers the particular case of the view as an entity into the
 * sceern-space: the y-axis grows on the bottom side of the screen.
 */
public class Wormhole2D extends AbstractWormhole2D {

    /**
     * Default logger.
     */
    private static final Logger L = LoggerFactory.getLogger(Wormhole2D.class);

    /**
     * Bidimensional wormhole constructor for an AWT/Swing {@link Component} class.
     * <br/>
     * Initializes a new instance directly setting the size of both view and
     * environment, and the offset too.
     *
     * @param environment  the {@link Environment}
     * @param comp the controlled {@code Component}
     */
    public Wormhole2D(final Environment<?> environment, final Component comp) {
        super(
                environment,
                new ComponentViewType(comp),
                from(comp.getWidth() / 2, comp.getHeight() / 2)
        );
    }

    /**
     * Bidimensional wormhole constructor for an AWT/Swing {@link Node} class.
     * <br/>
     * Initializes a new instance directly setting the size of both view and
     * environment, and the offset too.
     *
     * @param environment the {@code Environment}
     * @param view        the controlled {@code Node}
     */
    public Wormhole2D(final Environment<?> environment, final Node view) {
        super(
                environment,
                new NodeViewType(view),
                from(view.getBoundsInLocal().getWidth() / 2, view.getBoundsInLocal().getHeight() / 2)
        );
    }

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
        return Wormhole2D.L;
    }

    @Override
    protected double getViewRatio() {
        return Math.max(1, getView().getWidth()) / Math.max(1, getView().getHeight());
    }
}
