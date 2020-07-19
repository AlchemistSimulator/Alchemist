package it.unibo.alchemist.boundary.gui.effects;

import com.google.common.hash.Hashing;
import it.unibo.alchemist.boundary.gui.CommandQueueBuilder;
import it.unibo.alchemist.boundary.gui.effects.json.EffectGroupAdapter;
import it.unibo.alchemist.boundary.gui.utility.ResourceLoader;
import it.unibo.alchemist.boundary.interfaces.DrawCommand;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Position2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import kotlin.text.Charsets;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class models a group of effects, stored as a stack. It can manage
 * priority of visualization and visibility of each effect inside it.
 *
 * @param <P> The position type
 */
public final class EffectStack<P extends Position2D<? extends P>> implements EffectGroup<P> {
    /** Default generated serial version UID. */
    private static final long serialVersionUID = -3606828966321303483L;
    /** Default IllegalArgumentException message. */
    private static final String CANNOT_FIND_EFFECT = "Cannot find the effect in the stack";
    /** Default effect group name. */
    private static final String DEFAULT_NAME = ResourceLoader.getStringRes("effect_stack_default_name");
    /** Default logger. */
    private static final Logger L = LoggerFactory.getLogger(EffectStack.class);

    private final List<EffectFX<P>> effects;
    private int topIndex;
    private String name;
    private boolean visibility;

    /**
     * Constructor that creates an empty stack of effects with default name.
     */
    public EffectStack() {
        this(DEFAULT_NAME);
    }

    /**
     * Default constructor. It creates an empty stack of effects with a given
     * name.
     * 
     * @param name
     *            the name of the group
     */
    public EffectStack(final String name) {
        this.effects = new ArrayList<>();
        this.topIndex = 0;
        this.name = name;
        this.visibility = true;
    }

    @Override
    public <T> Queue<DrawCommand<P>> computeDrawCommands(final Environment<T, P> environment) {
        final CommandQueueBuilder<P> builder = new CommandQueueBuilder<>();

        if (isVisible()) {
            this.stream()
                    .map(effectFX -> effectFX.computeDrawCommands(environment))
                    .flatMap(Collection::stream)
                    .map(command -> command.wrap(this::isVisible))
                    .forEach(builder::addCommand);
        }
        return builder.buildCommandQueue();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Puts the effects in the group, giving it the maximum priority.
     * <p>
     * Acts nearly the same than using {@link #add(EffectFX)} or
     * {@link #offer(EffectFX)}.
     * 
     * @param effect
     *            the effect
     * @return the effect pushed
     * @throws UnsupportedOperationException
     *             if the add operation is not supported by this list
     * @throws ClassCastException
     *             if the class of the specified element prevents it from being
     *             added to this list
     * @throws NullPointerException
     *             if the specified element is null and this list does not
     *             permit null elements
     * @throws IllegalArgumentException
     *             if some property of this element prevents it from being added
     *             to this list
     */
    public EffectFX<P> push(final EffectFX<P> effect) {
        this.effects.add(effect);
        this.topIndex++;
        return effect;
    }

    /**
     * Removes the effect with maximum priority and returns it.
     * <p>
     * Acts nearly the same than using {@link #remove()} or {@link #poll()}.
     * 
     * @return the effect with maximum priority
     */
    public EffectFX<P> pop() {
        final EffectFX<P> e = this.effects.get(topIndex);
        this.effects.remove(topIndex);
        this.topIndex--;
        return e;
    }

    @Override
    public int search(final EffectFX<P> effect) {
        for (int i = topIndex; i >= 0; i--) {
            if (this.effects.get(i).equals(effect)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean getVisibilityOf(final EffectFX<P> effect) {
        final int effectIndex = this.search(effect);
        if (effectIndex == -1) {
            throw new IllegalArgumentException(CANNOT_FIND_EFFECT);
        }
        return this.effects.get(effectIndex).isVisible();
    }

    @Override
    public void setVisibilityOf(final EffectFX<P> effect, final boolean visibility) {
        final int effectIndex = this.search(effect);
        if (effectIndex == -1) {
            throw new IllegalArgumentException(CANNOT_FIND_EFFECT);
        }
        this.effects.get(effectIndex).setVisibility(visibility);
    }

    @Override
    public void changePriority(final EffectFX<P> effect, final int offset) {
        final int currentPos = this.search(effect);
        final int newPos = currentPos + offset;
        final EffectFX<P> temp = this.effects.get(newPos);

        this.effects.set(newPos, effect);

        this.effects.set(currentPos, temp);
    }

    @Override
    public boolean isVisible() {
        return this.visibility;
    }

    @Override
    public void setVisibility(final boolean visibility) {
        this.visibility = visibility;
    }

    @Override
    public Iterator<EffectFX<P>> iterator() {
        return effects.iterator();
    }

    @Override
    public int size() {
        return this.effects.size();
    }

    @Override
    public boolean contains(final Object o) {
        return this.effects.contains(o);
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return this.effects.toArray();
    }

    @NotNull
    @Override
    public <T> T[] toArray(final T[] a) {
        return this.effects.toArray(a);
    }

    @Override
    public boolean add(final EffectFX<P> e) {
        if (e == null || this.contains(e)) {
            return false;
        } else {
            try {
                return this.push(e) != null;
            } catch (UnsupportedOperationException | ClassCastException | IllegalArgumentException ex) {
                L.debug(ex.toString());
                return false;
            }
        }
    }

    @Override
    public boolean remove(final Object o) {
        if (o instanceof EffectFX<?>) {
            @SuppressWarnings("unchecked")
            final EffectFX<P> effect = (EffectFX<P>) o;
            final int index = this.search(effect);
            if (index == -1) {
                return false;
            } else {
                try {
                    this.topIndex--;
                    return this.effects.remove(effect);
                } catch (UnsupportedOperationException | IndexOutOfBoundsException ex) {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    @Override
    public EffectFX<P> remove() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("The stack is empty");
        } else {
            return this.pop();
        }
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return this.effects.containsAll(c);
    }

    @Override
    public boolean addAll(final Collection<? extends EffectFX<P>> c) {
        try {
            c.forEach(e -> {
                if (this.push(e) == null) {
                    throw new IllegalArgumentException();
                }
            });
            return true;
        } catch (UnsupportedOperationException | ClassCastException | IllegalArgumentException ex) {
            return false;
        }
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        boolean b = false;
        for (final Object e : c) {
            if (this.remove(e)) {
                b = true;
            }
        }
        return b;
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        boolean b = false;

        for (final EffectFX effect : this.effects) {
            if (!c.contains(effect)) {
                this.remove(effect);
                b = true;
            }
        }

        return b;
    }

    @Override
    public void clear() {
        this.effects.forEach(this::remove);
    }

    @Override
    public boolean offer(final EffectFX<P> e) {
        try {
            return this.add(e);
        } catch (UnsupportedOperationException | ClassCastException ex) {
            return false;
        }
    }

    @Override
    public EffectFX<P> poll() {
        return this.pop();
    }

    /**
     * Returns the effect with maximum priority, without removing it.
     * <p>
     * See {@link Queue#peek()}.
     * 
     * @return the effect with maximum priority
     */
    @Override
    public EffectFX<P> peek() {
        return this.effects.get(topIndex);
    }

    @Override
    public EffectFX<P> element() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("The stack is empty");
        } else {
            return this.peek();
        }
    }

    @Override
    public boolean isEmpty() {
        return effects.isEmpty();
    }

    @Override
    public int hashCode() {
        return Hashing.murmur3_32().newHasher()
                .putString(effects.toString(), Charsets.UTF_8)
                .putString(name, Charsets.UTF_8)
                .putInt(topIndex)
                .putBoolean(visibility)
                .hash().asInt();
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
        final EffectStack other = (EffectStack) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (topIndex != other.topIndex) {
            return false;
        }
        if (visibility != other.visibility) {
            return false;
        }
        if (effects == null) {
            if (other.effects != null) {
                return false;
            }
        } else if (!effects.equals(other.effects)) {
            return false;
        }
        return true;
    }

    /**
     * Returns a {@link com.google.gson.JsonSerializer} and {@link com.google.gson.JsonDeserializer} combo class
     * to be used as a {@code TypeAdapter} for this
     * {@code EffectStack}.
     * 
     * @return the {@code TypeAdapter} for this class
     */
    @NotNull
    public static EffectGroupAdapter getTypeAdapter() {
        return new EffectGroupAdapter();
    }
}
