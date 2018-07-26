package it.unibo.alchemist.boundary.gui.effects;

import it.unibo.alchemist.boundary.gui.CommandQueueBuilder;
import it.unibo.alchemist.boundary.gui.utility.ResourceLoader;
import it.unibo.alchemist.boundary.interfaces.DrawCommand;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.model.interfaces.Concentration;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Position2D;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UTFDataFormatException;
import java.util.Queue;
import javafx.beans.property.Property;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

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
    /**
     * Default serial version UID.
     */
    private static final long serialVersionUID = 1L;
    private String name;
    private boolean visibility;

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
    }

    /**
     * The method is useful to implement comparisons of properties in {@link #equals(Object)} method.
     *
     * @param prop1 the comparing object
     * @param prop2 the other object
     * @param <T>   the object type wrapped by the JavaFX {@link Property}
     * @param <P>   the JavaFX {@link Property}
     * @return true if the objects are both null or equal, false otherwise
     */
    @Contract("null, !null -> false")
    protected static <T, P extends Property<T>> boolean checkEqualsProperties(final @Nullable P prop1, final @Nullable P prop2) {
        if (prop1 == null) {
            return prop2 == null;
        } else {
            return prop2 != null && prop1.getValue().equals(prop2.getValue());
        }
    }

    /**
     * The method compares two {@link AbstractEffect Effects} to check basic properties of visibility, name and class.
     *
     * @param anEffect    the first effect to check
     * @param otherEffect the other effect to check
     * @param <T>         the type of Effect
     * @return true if the Effects have the same name and visibility and are assignable from same class or are both null, false otherwise
     */
    @Contract("null, null -> true; null, !null -> false; !null, null -> false")
    @SuppressWarnings("unchecked")
    protected static <T extends AbstractEffect> boolean checkBasicProperties(final @Nullable T anEffect, final @Nullable Object otherEffect) {
        if (anEffect == null) {
            return otherEffect == null;
        } else if (otherEffect != null) {
            if (anEffect == otherEffect) { // NOPMD - the comparison wants to check if the variables point to the same object
                return true;
            }
            if (!anEffect.getClass().isInstance(otherEffect)) {
                return false;
            }
            final T other = (T) otherEffect;
            if (anEffect.isVisible() != other.isVisible()) {
                return false;
            }
            if (anEffect.getName() == null) {
                return other.getName() == null;
            } else {
                return anEffect.getName().equals(other.getName());
            }
        } else {
            return false;
        }
    }

    @Override
    public <T, P extends Position2D<? extends P>> Queue<DrawCommand> computeDrawCommands(final Environment<T, P> environment) {
        getData(environment);
        final CommandQueueBuilder builder = new CommandQueueBuilder();
        consumeData().stream()
                .map(cmd -> cmd.wrap(this::isVisible))
                .forEach(builder::addCommand);
        return builder.buildCommandQueue();
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
    protected abstract <T, P extends Position2D<? extends P>> void getData(Environment<T, P> environment);

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
    public final void setVisibility(final boolean visibility) {
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
    public abstract boolean equals(Object obj);
}
