package it.unibo.alchemist.boundary.gui.effects;

import java.awt.Graphics2D;
import java.io.Serializable;

import it.unibo.alchemist.boundary.wormhole.interfaces.IWormhole2D;
import it.unibo.alchemist.model.interfaces.Environment;
import javafx.scene.input.DataFormat;

/**
 * Graphical visualization of something happening in the environment.
 */
public interface EffectFX extends Serializable {
    /** Default DataFormat. */
    DataFormat DATA_FORMAT = new DataFormat(EffectFX.class.getName());

    /**
     * Applies the effect.
     * 
     * @param graphic
     *            the {@code Graphics2D} to use
     * @param environment
     *            the {@code Environment} containing the nodes to draw
     * @param wormhole
     *            the {@code Wormhole2D} object to calculate positions
     * @param <T>
     *            the {@link Environment} type
     */
    <T> void apply(Graphics2D graphic, Environment<T> environment, IWormhole2D wormhole);

    /**
     * Gets the name of the effect.
     * 
     * @return the name of the effect
     */
    String getName();

    /**
     * Sets the name of the effect.
     * 
     * @param name
     *            the name of the effect to set
     */
    void setName(String name);

    /**
     * Gets the visibility of the effect.
     * 
     * @return the visibility of the effect
     */
    boolean isVisibile();

    /**
     * Sets the visibility of the effect.
     * 
     * @param vilibility
     *            the visibility of the effect to set
     */
    void setVisibility(boolean vilibility);

    @Override // Should override hashCode() method
    int hashCode();

    @Override // Should override equals() method
    boolean equals(Object obj);
}
