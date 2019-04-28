package it.unibo.alchemist.boundary.gui.effects;

import it.unibo.alchemist.boundary.gui.CommandQueueBuilder;
import it.unibo.alchemist.boundary.gui.effects.json.ColorSerializationAdapter;
import it.unibo.alchemist.boundary.gui.utility.ResourceLoader;
import it.unibo.alchemist.boundary.gui.view.properties.PropertyFactory;
import it.unibo.alchemist.boundary.gui.view.properties.RangedDoubleProperty;
import it.unibo.alchemist.boundary.interfaces.DrawCommand;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position2D;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import javafx.beans.property.DoubleProperty;
import javafx.scene.paint.Color;
import org.danilopianini.util.Hashes;

/**
 * Simple effect that draws a {@link Color#BLACK black} line for each
 * {@link Node} in a {@link Neighborhood}.
 * <p>
 * It's possible to set the size of the dots.
 */
public class DrawLinks<P extends Position2D<? extends P>> extends AbstractEffect<P> {
    /**
     * Default serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default effect name.
     */
    private static final String DEFAULT_NAME = ResourceLoader.getStringRes("drawlinks_default_name");

    /**
     * Default dot size.
     */
    private static final double DEFAULT_SIZE = 0.15;

    /**
     * Default {@code Color}.
     */
    private static final Color DEFAULT_COLOR = Color.BLACK;
    private final transient ConcurrentHashMap<P, ConcurrentLinkedQueue<P>> positions;
    private RangedDoubleProperty size = PropertyFactory.getPercentageRangedProperty(ResourceLoader.getStringRes("drawdot_size"), DEFAULT_SIZE);
    private Color color = DEFAULT_COLOR;

    /**
     * Empty constructor.
     * <p>
     * Name is set to default name.
     * <p>
     * Default visibility is true.
     */
    public DrawLinks() {
        this(DEFAULT_NAME);
    }

    /**
     * Default constructor.
     * <p>
     * Default visibility is true.
     *
     * @param name the name of the effect.
     */
    public DrawLinks(final String name) {
        super(name, DEFAULT_VISIBILITY);
        positions = new ConcurrentHashMap<>();
    }

    @Override
    protected Queue<DrawCommand<P>> consumeData() {
        final CommandQueueBuilder<P> builder = new CommandQueueBuilder<P>();
        positions.forEach((position, neighbors) -> {
            final double size = getSize();
            builder.addCommand((graphic, wormhole) -> {
                final Point viewPoint = wormhole.getViewPoint(position);
                final double startX = viewPoint.getX() - size / 2;
                final double startY = viewPoint.getY() - size / 2;
                neighbors.forEach(p -> {
                    final double endX = wormhole.getViewPoint(p).getX();
                    final double endY = wormhole.getViewPoint(p).getY();
                    graphic.setStroke(getColor());
                    graphic.setLineWidth(size);
                    graphic.strokeLine(startX, startY, endX, endY);
                });
            });
        });
        return builder.buildCommandQueue();
    }

    @Override
    protected <T> void getData(final Environment<T, P> environment) {
        positions.clear();
        environment.forEach(node -> {
            final ConcurrentLinkedQueue<P> neighbors = new ConcurrentLinkedQueue<>();
            environment.getNeighborhood(node)
                    .getNeighbors()
                    .stream()
                    .map(environment::getPosition)
                    .forEach(neighbors::add);
            positions.putIfAbsent(environment.getPosition(node), neighbors);
        });
    }

    /**
     * The size of the dots representing each {@link Node} in the
     * {@link Environment} specified when drawing.
     *
     * @return the size property
     * @see #setSize(Double)
     * @see #getSize()
     */

    public DoubleProperty sizeProperty() {
        return this.size;
    }

    /**
     * Gets the value of the property {@code sizeProperty}.
     *
     * @return the size of the dots
     * @see #sizeProperty()
     */
    public Double getSize() {
        return this.size.get();
    }

    /**
     * Sets the value of the property {@code sizeProperty}.
     *
     * @param size the size to set
     * @throws IllegalArgumentException if the provided value is not a valid percentage
     * @see #sizeProperty()
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
    public int hashCode() {
        return Hashes.hash32(getColor(), getName(), getSize(), isVisible());
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        final boolean check = checkBasicProperties(this, obj);
        if (check) {
            final DrawLinks other = (DrawLinks) obj;
            final Color otherColor = other.getColor();
            final Color thisColor = getColor();
            return checkEqualsProperties(sizeProperty(), other.sizeProperty())
                    && thisColor == null ? otherColor == null : thisColor.equals(otherColor);
        } else {
            return false;
        }
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
        ColorSerializationAdapter.writeColor(stream, getColor());
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
        color = ColorSerializationAdapter.readColor(stream);
    }
}