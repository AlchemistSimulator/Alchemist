package it.unibo.alchemist.boundary.gui.effects;

import it.unibo.alchemist.boundary.gui.utility.ResourceLoader;
import it.unibo.alchemist.boundary.gui.view.properties.PropertyFactory;
import it.unibo.alchemist.boundary.gui.view.properties.RangedDoubleProperty;
import it.unibo.alchemist.boundary.wormhole.interfaces.BidimensionalWormhole;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import javafx.beans.property.DoubleProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.apache.commons.math3.util.FastMath;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


/**
 * Simple effect that draws a {@link Color#BLACK black} dot for each
 * {@link Node}.
 * <p>
 * It's possible to set the size of the dots.
 */
public class DrawDot implements EffectFX {

    /**
     * Magic number used by auto-generated {@link #hashCode()} method.
     */
    private static final int HASHCODE_NUMBER_1 = 1231;
    /**
     * Magic number used by auto-generated {@link #hashCode()} method.
     */
    private static final int HASHCODE_NUMBER_2 = 1237;

    /**
     * Default generated Serial Version UID.
     */
    private static final long serialVersionUID = -6098041600645663870L;

    /**
     * Default effect name.
     */
    private static final String DEFAULT_NAME = ResourceLoader.getStringRes("drawdot_default_name");

    /**
     * Default dot size.
     */
    private static final double DEFAULT_SIZE = 5;

    /**
     * Maximum value for the scale factor.
     */
    private static final double MAX_SCALE = 100;
    /**
     * Minimum value for the scale factor.
     */
    private static final double MIN_SCALE = 0;
    /**
     * Range for the scale factor.
     */
    private static final double SCALE_DIFF = MAX_SCALE - MIN_SCALE;
    /**
     * Default value of the scale factor.
     */
    private static final double DEFAULT_SCALE = (SCALE_DIFF) / 2 + MIN_SCALE;

    /**
     * Default {@code Color}.
     */
    private static final Color DEFAULT_COLOR = Color.BLACK;

    private RangedDoubleProperty size = PropertyFactory.getPercentageRangedProperty(ResourceLoader.getStringRes("drawdot_size"), DEFAULT_SIZE);
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
     * @param name the name of the effect.
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
    public <T> void apply(final GraphicsContext graphic, final Environment<T> environment, final BidimensionalWormhole wormhole) {
        environment.forEach(node -> {
            final double sizeX = size.get();
            final double startX = wormhole.getViewPoint(environment.getPosition(node)).getX() - sizeX / 2;
            final double sizeY = FastMath.ceil(sizeX * DEFAULT_SCALE);
            final double startY = wormhole.getViewPoint(environment.getPosition(node)).getY() - sizeY / 2;

            graphic.setFill(color);
            graphic.fillOval((int) startX, (int) startY, (int) sizeX, (int) sizeY);
//            graphic.fill();
        });
    }

    /**
     * The size of the dots representing each {@link Node} in the
     * {@link Environment} specified when calling
     * {@link #apply(GraphicsContext, Environment, BidimensionalWormhole) apply} in percentage.
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
     * @param size the size to set
     * @throws IllegalArgumentException if the provided value is not a valid percentage
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
     * @param color the color to set
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

    /**
     * Method needed for well working serialization.
     * <p>
     * From {@link Serializable}: <blockquote>The {@code writeObject} method is
     * responsible for writing the state of the object for its particular class
     * so that the corresponding readObject method can restore it. The default
     * mechanism for saving the Object's fields can be invoked by calling
     * {@code out.defaultWriteObject}. The method does not need to concern
     * itself with the state belonging to its superclasses or subclasses. State
     * is saved by writing the 3 individual fields to the
     * {@code ObjectOutputStream} using the {@code writeObject} method or by
     * using the methods for primitive data types supported by
     * {@code DataOutput}. </blockquote>
     *
     * @param stream the output stream
     */
    private void writeObject(final ObjectOutputStream stream) throws IOException {
        stream.writeObject(size);
        stream.writeDouble(color.getRed());
        stream.writeDouble(color.getGreen());
        stream.writeDouble(color.getBlue());
        stream.writeDouble(color.getOpacity());
        stream.writeUTF(name);
        stream.writeBoolean(visibility);
    }

    /**
     * Method needed for well working serialization.
     * <p>
     * From {@link Serializable}: <blockquote>The {@code readObject} method is
     * responsible for reading from the stream and restoring the classes fields.
     * It may call {@code in.defaultReadObject} to invoke the default mechanism
     * for restoring the object's non-static and non-transient fields. The
     * {@code defaultReadObject} method uses information in the stream to assign
     * the fields of the object saved in the stream with the correspondingly
     * named fields in the current object. This handles the case when the class
     * has evolved to add new fields. The method does not need to concern itself
     * with the state belonging to its superclasses or subclasses. State is
     * saved by writing the individual fields to the {@code ObjectOutputStream}
     * using the {@code writeObject} method or by using the methods for
     * primitive data types supported by {@code DataOutput}. </blockquote>
     *
     * @param stream the input stream
     */
    private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
        size = (RangedDoubleProperty) stream.readObject();
        color = new Color(stream.readDouble(), stream.readDouble(), stream.readDouble(), stream.readDouble());
        name = stream.readUTF();
        visibility = stream.readBoolean();
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
