package it.unibo.alchemist.boundary.gui.effects;

import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;

import java.awt.Graphics2D;

/**
 * Horrible hack to let effects know about the zoom and environment.
 * @param <T> concentration type
 * @param <P> position type
 */
public interface AdvancedEffect<T, P extends Position<P>> extends Effect {

    /**
     *
     * @param g graphics
     * @param n node
     * @param env environment
     * @param zoom zoom level
     * @param x screen x
     * @param y screen y
     */
    void apply(Graphics2D g, Node<T> n, Environment<T, P> env, double zoom, int x, int y);

    @Deprecated
    @Override
    default void apply(final Graphics2D g, final Node<?> n, final int x, final int y) {
        // nothing
    }
}
