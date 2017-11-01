package it.unibo.alchemist.boundary.gui.effects;

import it.unibo.alchemist.boundary.DrawCommand;
import it.unibo.alchemist.boundary.gui.utility.ResourceLoader;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.model.interfaces.Concentration;
import it.unibo.alchemist.model.interfaces.Environment;
import javafx.scene.paint.Color;

import java.io.*;
import java.util.LinkedList;
import java.util.Queue;

/**
 * It models an abstract implementation of the {@link EffectFX effect} interface, implementing default name and visibility properties.
 * <p>
 * The effect behavior can be implemented via {@link #computeDrawCommands(Environment)} template method.
 */
public abstract class AbstractEffect implements EffectFX {
    /**
     * Default name of the effect.
     */
    protected static final String DEFAULT_NAME = ResourceLoader.getStringRes("effect_default_name");

    /**
     * Default visibility of an effect.
     */
    protected static final boolean DEFAULT_VISIBILITY = true;

    private String name;
    private boolean visibility;
//    private transient CommandQueueBuilder commandQueueBuilder;

    /**
     * No parameters constructor.
     * <p>
     * Default visibility is {@value DEFAULT_VISIBILITY}.
     */
    protected AbstractEffect() {
        this(DEFAULT_NAME, DEFAULT_VISIBILITY);
    }

    /**
     * Constructor that lets set the name of the effect.
     * <p>
     * Default visibility is {@value DEFAULT_VISIBILITY}.
     *
     * @param name the name of the effect
     */
    protected AbstractEffect(final String name) {
        this(name, DEFAULT_VISIBILITY);
    }

    /**
     * Constructor that lets set the visibility of the effect.
     *
     * @param isVisible the visibility of the effect
     */
    protected AbstractEffect(final boolean isVisible) {
        this(DEFAULT_NAME, isVisible);
    }

    /**
     * Default contructor.
     *
     * @param name      the name of the effect
     * @param isVisible the visibility of the effect
     */
    protected AbstractEffect(final String name, final boolean isVisible) {
        this.name = name;
        this.visibility = isVisible;
//        this.commandQueueBuilder = new CommandQueueBuilder();
    }

    @Override
    public <T> Queue<DrawCommand> computeDrawCommands(final Environment<T> environment) {
        getData(environment);
        if (isVisible()) {
            return consumeData();
        } else {
            return new LinkedList<>(); // TODO: default empty queue
        }
    }

    /**
     * The method is called to consume the data extrapolated from {@link Environment} by {@link #getData(Environment)} method.
     *
     * @return the queue of command to be executed on JavaFX thread
     */
    protected abstract Queue<DrawCommand> consumeData();

    /**
     * The method extrapolates data from environment.
     * <p>
     * It is strongly recommended not to keep any reference to {@link Environment}- or {@link Simulation}-specific objects.
     *
     * @param environment the {@link Environment} to extrapolate data from
     * @param <T>         the {@link Concentration} type
     */
    protected abstract <T> void getData(final Environment<T> environment);

    @Override
    public final String getName() {
        return this.name;
    }

    @Override
    public final void setName(final String name) {
        this.name = name;
    }

    @Override
    public final boolean isVisible() {
        return this.visibility;
    }

    @Override
    public final void setVisibility(boolean visibility) {
        this.visibility = visibility;
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
     * {@code DataOutput}.</blockquote>
     *
     * @param stream the output stream
     * @throws IOException if I/O errors occur while writing to the underlying stream
     */
    private void writeObject(final ObjectOutputStream stream) throws IOException {
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
     * primitive data types supported by {@code DataOutput}.</blockquote>
     *
     * @param stream the input stream
     * @throws UTFDataFormatException if read bytes do not represent a valid modified UTF-8 encoding of a string
     * @throws EOFException           if the end of file is reached
     * @throws ClassNotFoundException if cannot find the class
     * @throws IOException            if other I/O error has occurred
     */
    private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
        name = stream.readUTF();
        visibility = stream.readBoolean();
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(final Object obj);
}
