package it.unibo.alchemist.boundary.gui.effects;

import java.io.Serializable;

/**
 * Models a group of effects. Each effect has a different priority of
 * visualization.
 */
public interface EffectGroup extends Serializable {

    /**
     * Gets the name of the group.
     * 
     * @return the name of the group
     */
    String getName();

    /**
     * Sets the name of the group.
     * 
     * @param name
     *            the name of the group
     */
    void setName(String name);

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
     * Returns the visibility of the group.
     * 
     * @return the visibility
     */
    boolean isVisible();

    /**
     * Sets the visibility of the group.
     * 
     * @param visibility
     *            the visibility
     */
    void setVisibility(boolean visibility);

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
     * Returns the transparency of the group.
     * 
     * @return the transparency in percentage
     */
    int getTransparency();

    /**
     * Sets the transparency of the group.
     * 
     * @param transparency
     *            the transparency in percentage
     * @throws IllegalArgumentException
     *             if the transparency is > 100 (fully visible) or < than 0
     *             (fully transparent)
     */
    void setTransparency(int transparency);

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
