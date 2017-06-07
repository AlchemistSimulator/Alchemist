package it.unibo.alchemist.boundary.gui.effects;

import java.awt.Color;
import java.awt.Graphics2D;

import org.apache.commons.math3.util.FastMath;

import it.unibo.alchemist.boundary.gui.view.property.PropertiesFactory;
import it.unibo.alchemist.boundary.gui.view.property.RangedDoubleProperty;
import it.unibo.alchemist.boundary.wormhole.interfaces.IWormhole2D;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import javafx.beans.property.DoubleProperty;

/**
 * Simple effect that draws a {@link Color#BLACK black} dot for each node.
 * <p>
 * It's possible to set the size of the dot.
 */
public class DrawDot implements EffectFX {
    /** Default generated Serial Version UID. */
    private static final long serialVersionUID = -6098041600645663870L;

    /** Default dot size. */
    private static final double DEFAULT_SIZE = 5;

    /** Maximum value for the scale factor. */
    private static final double MAX_SCALE = 100;
    /** Minimum value for the scale factor. */
    private static final double MIN_SCALE = 0;
    /** Range for the scale factor. */
    private static final double SCALE_DIFF = MAX_SCALE - MIN_SCALE;
    /** Initial value of the scale factor. */
    private static final double SCALE_INITIAL = (SCALE_DIFF) / 2 + MIN_SCALE;

    /** Default {@code Color}. */
    private static final Color DEFAULT_COLOR = Color.BLACK;
    // TODO maybe should switch to JavaFX Color class

    private final RangedDoubleProperty size = PropertiesFactory.getPercentageRangedProperty("Size", DEFAULT_SIZE);

    @Override
    public <T> void apply(final Graphics2D graphic, final Environment<T> environment, final IWormhole2D wormhole) {
        environment.forEach((Node<T> node) -> {
            final double ks = SCALE_INITIAL;
            final double sizex = size.get();
            final double startx = wormhole.getViewPoint(environment.getPosition(node)).getX() - sizex / 2;
            final double sizey = FastMath.ceil(sizex * ks);
            final double starty = wormhole.getViewPoint(environment.getPosition(node)).getY() - sizey / 2;

            graphic.fillOval((int) startx, (int) starty, (int) sizex, (int) sizey);
            graphic.setColor(DEFAULT_COLOR);

        });
    }

    /**
     * The size of the dot representing each {@link Node} in the
     * {@link Environment} specified when calling
     * {@link #apply(Graphics2D, Environment, IWormhole2D) apply} in percentage.
     * 
     * @return the size property
     */
    protected DoubleProperty sizeProperty() {
        return this.size;
    }

    /**
     * Gets the value of the property {@code sizeProperty}.
     * 
     * @return the size of the dot
     */
    protected Double getSize() {
        return this.size.get();
    }

    /**
     * Sets the value of the property {@code sizeProperty}.
     * 
     * @param size
     *            the size to set
     * @throws IllegalArgumentException
     *             if the provided value is not a valid percentage
     */
    protected void setSize(final Double size) {
        this.size.set(size);
    }

}
