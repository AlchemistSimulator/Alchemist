package it.unibo.alchemist.boundary.gui.effects;

import java.io.Serializable;

/**
 * Models a group of effects. Each effect has a different priority of
 * visualization.
 */
public interface EffectGroup extends Serializable {

    /**
     * Puts the effects in the group, giving it the maximum priority.
     * 
     * @param effect
     *            the effect
     * @return the effect pushed
     */
    Effect push(Effect effect);

    /**
     * Removes the effect with maximum priority and returns it.
     * 
     * @return the effect with maximum priority
     */
    Effect pop();

    /**
     * Returns the effect with maximum priority, without removing it.
     * 
     * @return the effect with maximum priority
     */
    Effect peek();

    /**
     * Checks if the group contains effects.
     * 
     * @return true if there are effects, false otherwise
     */
    boolean empty();

    /**
     * Checks if an effect is present in the group.
     * 
     * @param effect
     *            the effect to search
     * @return the position, or -1 if not present
     */
    int search(Effect effect);

    /**
     * Returns the visibility of the specified effect.
     * 
     * @param effect
     *            the effect
     * @return the visibility
     * @throws IllegalArgumentException
     *             if can't find the effect
     */
    boolean getVisibilityOf(Effect effect);

    /**
     * Sets the visibility of the specified effect.
     * 
     * @param effect
     *            the effect
     * @param visibility
     *            the visibility to set
     * @throws IllegalArgumentException
     *             if can't find the effect
     */
    void setVisibilityOf(Effect effect, boolean visibility);

    /**
     * Changes the specified offset priority of the specified offset.
     * 
     * @param effect
     *            the effect
     * @param offset
     *            the offset; it can be positive or negative
     * @throws IllegalArgumentException
     *             if can't find the effect
     */
    void changePriority(Effect effect, int offset);

    @Override // Should override hashCode() method
    int hashCode();

    @Override // Should override equals() method
    boolean equals(Object obj);
}
