package it.unibo.alchemist.boundary.gui.effects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class models a group of effects, stored as a stack. It can manage
 * priority of visualization and visibility of each effect inside it.
 */
public class EffectStack implements EffectGroup {
    /** Default generated serial version UID. */
    private static final long serialVersionUID = -3606828966321303483L;
    /** Default IllegalArgumentException message. */
    private static final String CANNOT_FIND_EFFECT = "Cannot find the effect in the stack";
    /** Default effect group name. */
    public static final String DEFAULT_NAME = "Unnamed group";
    private static final int FIRST_HASHCODE_CONSTANT = 1231;
    private static final int SECOND_HASHCODE_CONSTANT = 1237;
    /** Default logger. */
    private static final Logger L = LoggerFactory.getLogger(EffectStack.class);

    private final List<EffectFX> effects;
    private final List<Boolean> visibilities;
    private int topIndex;
    private String name;
    private boolean visibility;
    private int transparency;

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
        this.visibilities = new ArrayList<>();
        this.topIndex = 0;
        this.name = name;
        this.visibility = true;
        this.transparency = 100;
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
     * Acts nearly the same than using {@link #add(Effect)} or
     * {@link #offer(Effect)}.
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
    public EffectFX push(final EffectFX effect) {
        this.effects.add(effect);
        this.visibilities.add(true);
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
    public EffectFX pop() {
        final EffectFX e = this.effects.get(topIndex);
        this.effects.remove(topIndex);
        this.visibilities.remove(topIndex);
        this.topIndex--;
        return e;
    }

    @Override
    public int search(final EffectFX effect) {
        for (int i = topIndex; i >= 0; i--) {
            if (this.effects.get(i).equals(effect)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean getVisibilityOf(final EffectFX effect) {
        try {
            return this.visibilities.get(this.search(effect));
        } catch (final IndexOutOfBoundsException e) {
            throw new IllegalArgumentException(CANNOT_FIND_EFFECT);
        }
    }

    @Override
    public void setVisibilityOf(final EffectFX effect, final boolean visibility) {
        try {
            this.visibilities.set(this.search(effect), visibility);
        } catch (final IndexOutOfBoundsException e) {
            throw new IllegalArgumentException(CANNOT_FIND_EFFECT);
        }
    }

    @Override
    public void changePriority(final EffectFX effect, final int offset) {
        final int currentPos = this.search(effect);
        final int newPos = currentPos + offset;
        final EffectFX temp = this.effects.get(newPos);
        final boolean tmp = this.visibilities.get(newPos);

        this.visibilities.set(newPos, getVisibilityOf(effect));
        this.effects.set(newPos, effect);

        this.visibilities.set(currentPos, tmp);
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
    public int getTransparency() {
        return this.transparency;
    }

    @Override
    public void setTransparency(final int transparency) {
        if (transparency >= 0 && transparency <= 100) {
            this.transparency = transparency;
        } else {
            throw new IllegalArgumentException("Invalid transparency value");
        }
    }

    @Override
    public Iterator<EffectFX> iterator() {
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

    @Override
    public Object[] toArray() {
        return this.effects.toArray();
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        return this.effects.toArray(a);
    }

    @Override
    public boolean add(final EffectFX e) {
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
        if (o instanceof EffectFX) {
            final EffectFX effect = (EffectFX) o;
            final int index = this.search(effect);
            if (index == -1) {
                return false;
            } else {
                try {
                    this.visibilities.remove(index);
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
    public boolean containsAll(final Collection<?> c) {
        return this.effects.containsAll(c);
    }

    @Override
    public boolean addAll(final Collection<? extends EffectFX> c) {
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
        this.effects.forEach(effect -> this.remove(effect));
    }

    @Override
    public boolean offer(final EffectFX e) {
        try {
            return this.add(e);
        } catch (final Exception ex) {
            return false;
        }
    }

    @Override
    public EffectFX remove() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("The stack is empty");
        } else {
            return this.pop();
        }
    }

    @Override
    public EffectFX poll() {
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
    public EffectFX peek() {
        return this.effects.get(topIndex);
    }

    @Override
    public EffectFX element() {
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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((effects == null) ? 0 : effects.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + topIndex;
        result = prime * result + transparency;
        result = prime * result + ((visibilities == null) ? 0 : visibilities.hashCode());
        result = prime * result + (visibility ? FIRST_HASHCODE_CONSTANT : SECOND_HASHCODE_CONSTANT);
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
        if (transparency != other.transparency) {
            return false;
        }
        if (effects == null) {
            if (other.effects != null) {
                return false;
            }
        } else if (!effects.equals(other.effects)) {
            return false;
        }
        if (visibilities == null) {
            if (other.visibilities != null) {
                return false;
            }
        } else if (!visibilities.equals(other.visibilities)) {
            return false;
        }
        return true;
    }
}
