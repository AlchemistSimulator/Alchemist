package it.unibo.alchemist.boundary.gui.effects;

import java.awt.Color;
import java.awt.Graphics2D;

import org.apache.commons.math3.util.FastMath;

import it.unibo.alchemist.boundary.gui.view.properties.PropertyFactory;
import it.unibo.alchemist.boundary.gui.view.properties.RangedDoubleProperty;
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
    /** Magic number used by auto-generated {@link #hashCode()} method. */
    private static final int HASHCODE_NUMBER_1 = 1231;
    /** Magic number used by auto-generated {@link #hashCode()} method. */
    private static final int HASHCODE_NUMBER_2 = 1237;

    /** Default generated Serial Version UID. */
    private static final long serialVersionUID = -6098041600645663870L;

    /** Default effect name. */
    private static final String DEFAULT_NAME = "Unnamed DrawDot";

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

    private final RangedDoubleProperty size = PropertyFactory.getPercentageRangedProperty("Size", DEFAULT_SIZE);
    private Color color = DEFAULT_COLOR;

    private String name;

    private boolean visibility;

    /**
     * Empty constructor.
     * <p>
     * Name is set to default name.
     * <p>
     * Default visibility is true.
     */
    public DrawDot() {
        this(DEFAULT_NAME);
    }

    /**
     * Default constructor.
     * <p>
     * Default visibility is true.
     * 
     * @param name
     *            the name of the effect.
     */
    public DrawDot(final String name) {
        this.name = name;
        this.visibility = true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * For each {@link Node} in the specified {@link Environment}, it will draw
     * a {@link Color#BLACK black} dot.
     */
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
    public DoubleProperty sizeProperty() {
        return this.size;
    }

    /**
     * Gets the value of the property {@code sizeProperty}.
     * 
     * @return the size of the dots
     */
    public Double getSize() {
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
    public void setSize(final Double size) {
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

    @Override
    public boolean isVisibile() {
        return this.visibility;
    }

    @Override
    public void setVisibility(final boolean vilibility) {
        this.visibility = vilibility;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((color == null) ? 0 : color.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((size == null) ? 0 : size.hashCode());
        result = prime * result + (visibility ? HASHCODE_NUMBER_1 : HASHCODE_NUMBER_2);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DrawDot other = (DrawDot) obj;
        if (isVisibile() != other.isVisibile()) {
            return false;
        }
        if (getColor() == null) {
            if (other.getColor() != null) {
                return false;
            }
        } else if (!getColor().equals(other.getColor())) {
            return false;
        }
        if (getName() == null) {
            if (other.getName() != null) {
                return false;
            }
        } else if (!getName().equals(other.getName())) {
            return false;
        }
        if (getSize() == null) {
            if (other.getSize() != null) {
                return false;
            }
        } else if (!getSize().equals(other.getSize())) {
            return false;
        }
        return true;
    }
}
