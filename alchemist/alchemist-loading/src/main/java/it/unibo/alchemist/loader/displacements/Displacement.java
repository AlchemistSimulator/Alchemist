package it.unibo.alchemist.loader.displacements;

import java.util.Iterator;
import java.util.stream.Stream;

import it.unibo.alchemist.model.interfaces.Position;

/**
 *
 */
@FunctionalInterface
public interface Displacement extends Iterable<Position> {

    /**
     * @return a {@link Stream} over the positions of this {@link Displacement}
     */
    Stream<Position> stream();

    @Override
    default Iterator<Position> iterator() {
        return stream().iterator();
    }

}
