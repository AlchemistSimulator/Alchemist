package it.unibo.alchemist.boundary.gui.effects;

import java.io.Serializable;
import java.util.Queue;

/**
 * Models a group of effects. Each effect has a different priority of
 * visualization.
 */
public interface EffectGroup extends Serializable, Queue<EffectFX> {

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
     * Checks if an effect is present in the group.
     * 
     * @param effect
     *            the effect to search
     * @return the position, or -1 if not present
     */
    int search(EffectFX effect);

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
    boolean getVisibilityOf(EffectFX effect);

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
    void setVisibilityOf(EffectFX effect, boolean visibility);

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
    void changePriority(EffectFX effect, int offset);

    @Override // Should override hashCode() method
    int hashCode();

    /**
     * Compares the {@link EffectGroup EffectGroup}s. The result is true if and
     * only if the argument is not {@code null} and every {@link Effect}
     * contained is not {@code null} and {@link Effect#equals(Object) equal} to
     * the corresponding in the comparing {@code EffectGroup} (order is
     * important!) and the group has the same name, visibility and transparency.
     * 
     * @see Object#equals(Object)
     */
    @Override // Should override equals() method
    boolean equals(Object obj);
}
