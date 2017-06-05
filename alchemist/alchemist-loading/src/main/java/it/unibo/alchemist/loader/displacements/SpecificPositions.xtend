package it.unibo.alchemist.loader.displacements;

import java.util.stream.Stream;

import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Environment
import java.util.List

public class SpecificPositions implements Displacement {
	
	List<Position> positions;

    new(Environment<?> env, Iterable<? extends Number>... positions) {
    	this.positions = positions.map[env.makePosition(it)].toList
    }

    override Stream<Position> stream() {
        return positions.stream;
    }

}
