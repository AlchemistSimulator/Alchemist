/**
 * 
 */
package it.unibo.alchemist.loader;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.danilopianini.lang.PrimitiveUtils;

import it.unibo.alchemist.model.interfaces.Position;

/**
 *
 */
public final class PositionMaker {

    private final Class<? extends Position> clazz;

    /**
     * @param clazz
     *            the type of {@link Position} to be produced
     */
    public PositionMaker(final Class<? extends Position> clazz) {
        this.clazz = clazz;
    }

    /**
     * @param coordinates
     *            the coordinates for this position
     * @return the position
     */
    public Position makePosition(final Number... coordinates) {
        try {
            return (Position) Arrays.stream(clazz.getConstructors())
                    .filter(c -> c.getParameterCount() == coordinates.length)
                    .filter(c -> Arrays.stream(c.getParameterTypes())
                            .allMatch(pClass -> PrimitiveUtils.classIsNumber(pClass)))
                    .findAny().get().newInstance((Object[]) coordinates);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | SecurityException e) {
            throw new IllegalArgumentException("cannot build " + clazz + " with parameters " + Arrays.toString(coordinates), e);
        }
    }

}
