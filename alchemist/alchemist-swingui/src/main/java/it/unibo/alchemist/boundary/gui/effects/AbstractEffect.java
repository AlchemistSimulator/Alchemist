package it.unibo.alchemist.boundary.gui.effects;

import it.unibo.alchemist.boundary.CommandQueueBuilder;
import it.unibo.alchemist.boundary.gui.utility.ResourceLoader;
import it.unibo.alchemist.boundary.wormhole.interfaces.BidimensionalWormhole;
import it.unibo.alchemist.model.interfaces.Environment;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Queue;
import javafx.scene.canvas.GraphicsContext;

public abstract class AbstractEffect implements EffectFX {
    protected static final String DEFAULT_NAME = ResourceLoader.getStringRes("effect_dafault_name");
    protected static final boolean DEFAULT_VISIBILITY = true;

    private String name;
    private boolean visibility;

    /**
     * No parameters constructor.
     */
    protected AbstractEffect() {
        this(DEFAULT_NAME, DEFAULT_VISIBILITY);
    }

    /**
     * Constructor that lets set the name of the effect.
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
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}, or an empty runnable if the effect is not visible
     */
    @Override
    public <T> Runnable apply(final GraphicsContext graphic, final Environment<T> environment, final BidimensionalWormhole wormhole) {
        if (!isVisibile()) {
            return () -> {
            };
        } else {
            final Queue<Runnable> commandQueue = getCommandQueue(graphic, environment, wormhole);
            return () -> commandQueue.forEach(Runnable::run);
        }
    }

    /**
     * It parses data on current thread and builds a queue of commands to be executed on JavaFX thread by {@link #apply(GraphicsContext, Environment, BidimensionalWormhole) apply()} method.
     *
     * @param graphic     the {@code Graphics2D} to use
     * @param environment the {@code Environment} containing the nodes to draw
     * @param wormhole    the {@code BidimensionalWormhole} object to calculate positions
     * @param <T>         the {@link Environment} type
     * @return the queue of commands to be executed on JavaFX thread
     * @see #apply(GraphicsContext, Environment, BidimensionalWormhole)
     * @see CommandQueueBuilder
     */
    protected abstract <T> Queue<Runnable> getCommandQueue(final GraphicsContext graphic, final Environment<T> environment, final BidimensionalWormhole wormhole);

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
    public void setVisibility(boolean vilibility) {
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
     * {@code DataOutput}. </blockquote>
     *
     * @param stream the output stream
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
     * primitive data types supported by {@code DataOutput}. </blockquote>
     *
     * @param stream the input stream
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
