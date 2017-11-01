package it.unibo.alchemist.boundary.gui.effects;

import it.unibo.alchemist.boundary.DrawCommand;
import it.unibo.alchemist.model.interfaces.Concentration;
import it.unibo.alchemist.model.interfaces.Environment;

import java.io.Serializable;
import java.util.Queue;

/**
 * Models a group of effects. Each effect has a different priority of
 * visualization.
 */
public interface EffectGroup extends Serializable, Queue<EffectFX> {

    /**
     * Computes all the commands for all the visible effects in this group.
     *
     * @param environment the environment to gather data from
     * @param <T>         the {@link Concentration} type
     * @return the queue of commands that should be run to draw the effects of the group
     * @see EffectFX#computeDrawCommands(Environment)
     */
    <T> Queue<DrawCommand> computeDrawCommands(Environment<T> environment);

    /**
     * Gets the name of the group.
     *
     * @return the name of the group
     */
    String getName();

    /**
     * Sets the name of the group.
     *
     * @param name the name of the group
     */
    void setName(String name);

    /**
     * Checks if an effect is present in the group.
     *
     * @param effect the effect to search
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
     * @param visibility the visibility
     */
    void setVisibility(boolean visibility);

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
     * Returns the transparency of the group.
     *
     * @return the transparency in percentage
     */
    int getTransparency();

    /**
     * Sets the transparency of the group.
     *
     * @param transparency the transparency in percentage
     * @throws IllegalArgumentException if the transparency is > 100 (fully visible) or < than 0
     *                                  (fully transparent)
     */
    void setTransparency(int transparency);

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
