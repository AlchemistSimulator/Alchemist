package it.unibo.alchemist.model.implementations.layers;

import it.unibo.alchemist.model.interfaces.Layer;
import it.unibo.alchemist.model.interfaces.Position;

/**
 * Implementation of an EmptyLayer.
 *
 * @param <T>
 */
public class EmptyLayer<T> implements Layer<T> {

    @Override
    public T getValue(final Position p) {
        return null;
    }

}
