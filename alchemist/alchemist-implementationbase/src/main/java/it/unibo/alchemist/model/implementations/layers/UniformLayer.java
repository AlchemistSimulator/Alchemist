package it.unibo.alchemist.model.implementations.layers;

import it.unibo.alchemist.model.interfaces.Layer;
import it.unibo.alchemist.model.interfaces.Position;

/**
 * a Layer where the concentration is the same at every point in space.
 *
 * @param <T> concentration type
 */
public class UniformLayer<T> implements Layer<T> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final T level;

    /**
     * @param level
     *            the concentration
     */
    public UniformLayer(final T level) {
        this.level = level;
    }

    @Override
    public T getValue(final Position p) {
        return level;
    }

}
