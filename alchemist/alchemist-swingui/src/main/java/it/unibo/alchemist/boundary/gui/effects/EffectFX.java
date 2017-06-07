package it.unibo.alchemist.boundary.gui.effects;

import java.awt.Graphics2D;
import java.io.Serializable;

import it.unibo.alchemist.boundary.wormhole.interfaces.IWormhole2D;
import it.unibo.alchemist.model.interfaces.Environment;
import javafx.scene.input.DataFormat;

/**
 * Graphical visualization of something happening in the environment.
 * <p>
 * It is implemented as a {@link FunctionalInterface}.
 */
@FunctionalInterface
public interface EffectFX extends Serializable {
    /** Default DataFormat. */
    DataFormat DATA_FORMAT = new DataFormat(EffectFX.class.getName());

    /**
     * Applies the effect.
     * 
     * @param graphic
     *            Graphics2D to use
     * @param environment
     *            the node to draw
     * @param wormhole
     *            the position
     */
    <T> void apply(Graphics2D graphic, Environment<T> environment, IWormhole2D wormhole);

    @Override // Should override hashCode() method
    int hashCode();

    @Override // Should override equals() method
    boolean equals(Object obj);
}
