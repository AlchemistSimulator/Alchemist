package it.unibo.alchemist.loader.displacements;

import java.util.Iterator;
import java.util.stream.Stream;

import it.unibo.alchemist.model.interfaces.Position;

/**
 *
 */
@FunctionalInterface
public interface Displacement<P extends Position<? extends P>> extends Iterable<P> {

    /**
     * @return a {@link Stream} over the positions of this {@link Displacement}
     */
    Stream<P> stream();

    @Override
    default Iterator<P> iterator() {
        return stream().iterator();
    }

}
