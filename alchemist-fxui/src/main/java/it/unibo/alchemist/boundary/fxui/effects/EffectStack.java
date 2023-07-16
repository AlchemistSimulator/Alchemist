/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.effects;

import it.unibo.alchemist.boundary.fxui.EffectFX;
import it.unibo.alchemist.boundary.fxui.EffectGroup;
import it.unibo.alchemist.boundary.fxui.impl.CommandQueueBuilder;
import it.unibo.alchemist.boundary.fxui.effects.serialization.EffectGroupAdapter;
import it.unibo.alchemist.boundary.fxui.util.ResourceLoader;
import it.unibo.alchemist.boundary.fxui.DrawCommand;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Position2D;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Queue;

/**
 * The class models a group of effects, stored as a stack. It can manage
 * priority of visualization and visibility of each effect inside it.
 *
 * @param <P> The position type
 */
public final class EffectStack<P extends Position2D<? extends P>> implements EffectGroup<P> {

    private static final long serialVersionUID = 1L;
    /** Default IllegalArgumentException message. */
    private static final String CANNOT_FIND_EFFECT = "Cannot find the effect in the stack";
    /** Default effect group name. */
    private static final String DEFAULT_NAME = ResourceLoader.getStringRes("effect_stack_default_name");

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
     * Acts nearly the same as using {@link #add(EffectFX)} or
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
     * Acts nearly the same as using {@link #remove()} or {@link #poll()}.
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
    @Nonnull
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

    @Nonnull
    @Override
    public Object[] toArray() {
        return this.effects.toArray();
    }

    @Nonnull
    @Override
    public <T> T[] toArray(@Nonnull final T[] a) {
        return this.effects.toArray(a);
    }

    @Override
    public boolean add(final EffectFX<P> e) {
        if (e == null || this.contains(e)) {
            return false;
        }
        this.push(e);
        return true;
    }

    @Override
    public boolean remove(final Object o) {
        if (o instanceof EffectFX<?>) {
            @SuppressWarnings("unchecked")
            final EffectFX<P> effect = (EffectFX<P>) o;
            final int index = this.search(effect);
            if (index == -1) {
                return false;
            }
            this.topIndex--;
            return this.effects.remove(effect);
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
    public boolean containsAll(@Nonnull final Collection<?> c) {
        return new HashSet<>(this.effects).containsAll(c);
    }

    @Override
    public boolean addAll(final Collection<? extends EffectFX<P>> c) {
        boolean changed = false;
        for (final EffectFX<P> effect : c) {
            if (effect != null) {
                this.push(effect);
                changed = true;
            }
        }
        return changed;
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
    public boolean retainAll(@Nonnull final Collection<?> c) {
        boolean b = false;
        for (final EffectFX<P> effect : this.effects) {
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
        return this.add(e);
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
        return Objects.hash(effects.toString(), name, topIndex, visibility);
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
        final EffectStack<?> other = (EffectStack<?>) obj;
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
            return other.effects == null;
        } else {
            return effects.equals(other.effects);
        }
    }

    /**
     * Returns a {@link com.google.gson.JsonSerializer} and {@link com.google.gson.JsonDeserializer} combo class
     * to be used as a {@code TypeAdapter} for this
     * {@code EffectStack}.
     *
     * @param <P> Position type
     *
     * @return the {@code TypeAdapter} for this class
     */
    @Nonnull
    public static <P extends Position2D<? extends P>> EffectGroupAdapter<P> getTypeAdapter() {
        return new EffectGroupAdapter<>();
    }
}
