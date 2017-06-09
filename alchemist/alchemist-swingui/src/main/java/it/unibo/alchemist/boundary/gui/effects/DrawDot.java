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
 * Simple effect that draws a {@link Color#BLACK black} dot for each
 * {@link Node}.
 * <p>
 * It's possible to set the size of the dots.
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
    /** Default value of the scale factor. */
    private static final double DEFAULT_SCALE = (SCALE_DIFF) / 2 + MIN_SCALE;

    /** Default {@code Color}. */
    private static final Color DEFAULT_COLOR = Color.BLACK;
    // TODO maybe should switch to JavaFX Color class

    private final RangedDoubleProperty size = PropertiesFactory.getPercentageRangedProperty("Size", DEFAULT_SIZE);
    private Color color = DEFAULT_COLOR;

    private String name;

    @Override
    public <T> void apply(final Graphics2D graphic, final Environment<T> environment, final IWormhole2D wormhole) {
        environment.forEach((Node<T> node) -> {
            final double ks = DEFAULT_SCALE;
            final double sizex = size.get();
            final double startx = wormhole.getViewPoint(environment.getPosition(node)).getX() - sizex / 2;
            final double sizey = FastMath.ceil(sizex * ks);
            final double starty = wormhole.getViewPoint(environment.getPosition(node)).getY() - sizey / 2;

            graphic.fillOval((int) startx, (int) starty, (int) sizex, (int) sizey);
            graphic.setColor(color);

        });
    }

    /**
     * The size of the dots representing each {@link Node} in the
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
     * @return the size of the dots
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

    /**
     * Gets the color of the dots.
     * <p>
     * Default color should be {@link Color#BLACK black}.
     * 
     * @return the color of the dots
     */
    protected Color getColor() {
        return this.color;
    }

    /**
     * Sets the color of the dots.
     * 
     * @param color
     *            the color to set
     */
    protected void setColor(final Color color) {
        this.color = color;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

}
