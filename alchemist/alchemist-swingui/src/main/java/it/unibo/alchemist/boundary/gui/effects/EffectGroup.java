package it.unibo.alchemist.boundary.gui.effects;

import java.io.Serializable;
import java.util.Queue;

/**
 * Models a group of effects. Each effect has a different priority of
 * visualization.
 */
public interface EffectGroup extends Serializable, Queue<EffectFX>, EffectFX {

    /**
     * Checks if an effect is present in the group.
     *
     * @param effect the effect to search
     * @return the position, or -1 if not present
     */
    int search(EffectFX effect);

    /**
     * Returns the visibility of the specified effect.
     *
     * @param effect the effect
     * @return the visibility
     * @throws IllegalArgumentException if can't find the effect
     * @see EffectFX#isVisible()
     */
    boolean getVisibilityOf(EffectFX effect);

    /**
     * Sets the visibility of the specified effect.
     *
     * @param effect     the effect
     * @param visibility the visibility to set
     * @throws IllegalArgumentException if can't find the effect
     * @see EffectFX#setVisibility(boolean)
     */
    void setVisibilityOf(EffectFX effect, boolean visibility);

    /**
     * Changes the specified offset priority of the specified offset.
     *
     * @param effect the effect
     * @param offset the offset; it can be positive or negative
     * @throws IllegalArgumentException if can't find the effect
     */
    void changePriority(EffectFX effect, int offset);

    /* Is suggested to override Object default equals method. */
    @Override
    int hashCode();

    /**
     * Compares the {@link EffectGroup EffectGroup}s. The result is true if and
     * only if the argument is not {@code null} and every {@link EffectFX}
     * contained is not {@code null} and {@link EffectFX#equals(Object) equal} to
     * the corresponding in the comparing {@code EffectGroup} (order is
     * important!) and the group has the same name, visibility and transparency.
     *
     * @see Object#equals(Object)
     */
    /* Is suggested to override Object default equals method. */
    @Override
    boolean equals(Object obj);
}
