/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.effects;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.boundary.fxui.util.RangedDoubleProperty;
import it.unibo.alchemist.boundary.fxui.util.ResourceLoader;
import it.unibo.alchemist.boundary.fxui.properties.PropertyFactory;
import it.unibo.alchemist.boundary.fxui.properties.RangedIntegerProperty;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Position2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.paint.Color;

/**
 * Simple effect that draws a colored dot for each {@link Node}.
 * <p>
 * It's possible to set the size and the color of the dots.
 *
 * @param <P> position type
 */
@SuppressFBWarnings("EI_EXPOSE_REP")
public class DrawColoredDot<P extends Position2D<? extends P>> extends DrawDot<P> {

    /**
     * Default generated Serial Version UID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Default effect name.
     */
    private static final String DEFAULT_NAME = ResourceLoader.getStringRes("drawcoloreddot_default_name");
    private RangedIntegerProperty red;
    private RangedIntegerProperty green;
    private RangedIntegerProperty blue;
    private RangedDoubleProperty alpha;

    /**
     * Empty constructor.
     * <p>
     * Name is set to default name.
     * <p>
     * Default visibility is true.
     */
    public DrawColoredDot() {
        this(DEFAULT_NAME);
    }

    /**
     * Default constructor.
     * <p>
     * Default visibility is true.
     *
     * @param name the name of the effect.
     */
    public DrawColoredDot(final String name) {
        super(name);
        final java.awt.Color awtColor = convertColor(super.getColor());
        // Set properties to default color of DrawDot
        red = PropertyFactory.getAWTColorChannelProperty(
                ResourceLoader.getStringRes("drawcoloreddot_red"),
                awtColor.getRed()
        );
        green = PropertyFactory.getAWTColorChannelProperty(
                ResourceLoader.getStringRes("drawcoloreddot_green"),
                awtColor.getGreen()
        );
        blue = PropertyFactory.getAWTColorChannelProperty(
                ResourceLoader.getStringRes("drawcoloreddot_blue"),
                awtColor.getBlue()
        );
        alpha = PropertyFactory.getFXColorChannelProperty(
                ResourceLoader.getStringRes("drawcoloreddot_alpha"),
                super.getColor().getOpacity()
        );
        // Update the color at each change
        red.addListener(this.updateColor());
        green.addListener(this.updateColor());
        blue.addListener(this.updateColor());
        alpha.addListener(this.updateColor());
    }

    /**
     * Convert a {@link Color JavaFX color} to an {@link java.awt.Color AWT color}.
     *
     * @param fxColor the JavaFX color
     * @return the AWT color
     */
    protected static java.awt.Color convertColor(final Color fxColor) {
        return new java.awt.Color(
                (float) fxColor.getRed(),
                (float) fxColor.getGreen(),
                (float) fxColor.getBlue(),
                (float) fxColor.getOpacity());
    }

    @Override
    public final Color getColor() { // NOPMD - Only widening method visibility
        return super.getColor();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Updates also all color-related properties.
     */
    @Override
    public final void setColor(final Color color) {
        // Also widens method visibility from parent
        this.setAlpha(color.getOpacity());
        final java.awt.Color awtColor = convertColor(color);
        this.setBlue(awtColor.getBlue());
        this.setGreen(awtColor.getGreen());
        this.setRed(awtColor.getRed());
        super.setColor(color);
    }

    /**
     * Returns a {@link ChangeListener} that updates the color of the dots.
     *
     * @return the {@code ChangeListener}
     */
    private ChangeListener<Number> updateColor() {
        return (observable, oldValue, newValue) -> super.setColor(
                Color.rgb(
                        red.getValue(),
                        green.getValue(),
                        blue.getValue(),
                        alpha.getValue()));
    }

    /**
     * The alpha channel of the color of the dots representing each {@link Node}
     * in the {@link Environment} specified when drawing.
     *
     * @return the alpha channel property
     */
    public DoubleProperty alphaProperty() {
        return this.alpha;
    }

    /**
     * Gets the value of {@code alphaProperty}.
     *
     * @return the alpha channel of the color of the dots
     */
    public double getAlpha() {
        return this.alpha.get();
    }

    /**
     * Sets the value of {@code alphaProperty}.
     *
     * @param alpha the alpha channel to set, in range 0.0-1.0
     */
    public void setAlpha(final double alpha) {
        this.alpha.set(alpha);
    }

    /**
     * The blue channel of the color of the dots representing each {@link Node}
     * in the {@link Environment} specified when drawing.
     *
     * @return the blue channel property
     */
    public IntegerProperty blueProperty() {
        return this.blue;
    }

    /**
     * Gets the value of {@code blueProperty}.
     *
     * @return the blue channel of the color of the dots, in range 0-255
     */
    public int getBlue() {
        return this.blue.get();
    }

    /**
     * Sets the value of {@code blueProperty}.
     *
     * @param blue the blue channel to set, in range 0-255
     */
    public void setBlue(final int blue) {
        this.blue.set(blue);
    }

    /**
     * The green channel of the color of the dots representing each {@link Node}
     * in the {@link Environment} specified when drawing.
     *
     * @return the green channel property
     */
    public IntegerProperty greenProperty() {
        return this.green;
    }

    /**
     * Gets the value of {@code greenProperty}.
     *
     * @return the green channel of the color of the dots, in range 0-255
     */
    public int getGreen() {
        return this.green.get();
    }

    /**
     * Sets the value of {@code greenProperty}.
     *
     * @param green the green channel to set, in range 0-255
     */
    public void setGreen(final int green) {
        this.green.set(green);
    }

    /**
     * The red channel of the color of the dots representing each {@link Node}
     * in the {@link Environment} specified when drawing.
     *
     * @return the red channel property
     */
    public IntegerProperty redProperty() {
        return this.red;
    }

    /**
     * Gets the value of {@code redProperty}.
     *
     * @return the red channel of the color of the dots, in range 0-255
     */
    public int getRed() {
        return this.red.get();
    }

    /**
     * Sets the value of {@code redProperty}.
     *
     * @param red the red channel to set, in range 0-255
     */
    public void setRed(final int red) {
        this.red.set(red);
    }

    /**
     * Method needed for well working serialization.
     * <p>
     * From {@link java.io.Serializable}: <blockquote>The {@code writeObject} method is
     * responsible for writing the state of the object for its particular class
     * so that the corresponding readObject method can restore it. The default
     * mechanism for saving the Object's fields can be invoked by calling
     * {@code out.defaultWriteObject}. The method does not need to concern
     * itself with the state belonging to its superclasses or subclasses. State
     * is saved by writing the 3 individual fields to the
     * {@code ObjectOutputStream} using the {@code writeObject} method or by
     * using the methods for primitive data types supported by
     * {@code DataOutput}.</blockquote>
     *
     * @param stream the output stream
     * @throws java.io.InvalidClassException    if something is wrong with a class used by serialization.
     * @throws java.io.NotSerializableException if some object to be serialized does not implement
     *      the java.io.Serializable interface.
     * @throws IOException              if any exception thrown by the underlying OutputStream.
     */
    private void writeObject(final ObjectOutputStream stream) throws IOException {
        stream.writeObject(red);
        stream.writeObject(green);
        stream.writeObject(blue);
        stream.writeObject(alpha);
    }

    /**
     * Method needed for well working serialization.
     * <p>
     * From {@link java.io.Serializable}: <blockquote>The {@code readObject} method is
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
     * primitive data types supported by {@code DataOutput}.</blockquote>
     *
     * @param stream the input stream
     * @throws ClassNotFoundException   if class of a serialized object cannot be found.
     * @throws java.io.InvalidClassException    if something is wrong with a class used by serialization.
     * @throws java.io.StreamCorruptedException if control information in the stream is inconsistent.
     * @throws java.io.OptionalDataException    if primitive data was found in the stream instead of objects.
     * @throws IOException              if any of the usual Input/Output related exceptions.
     */
    private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
        red = (RangedIntegerProperty) stream.readObject();
        green = (RangedIntegerProperty) stream.readObject();
        blue = (RangedIntegerProperty) stream.readObject();
        alpha = (RangedDoubleProperty) stream.readObject();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(
            alphaProperty(),
            blueProperty(),
            greenProperty(),
            getName(),
            redProperty(),
            getSize(),
            isVisible()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        final var other = (DrawColoredDot<?>) obj;
        return super.equals(obj)
                && checkEqualsProperties(blueProperty(), other.blueProperty())
                && checkEqualsProperties(redProperty(), other.redProperty())
                && checkEqualsProperties(greenProperty(), other.greenProperty())
                && checkEqualsProperties(alphaProperty(), other.alphaProperty());
    }
}
