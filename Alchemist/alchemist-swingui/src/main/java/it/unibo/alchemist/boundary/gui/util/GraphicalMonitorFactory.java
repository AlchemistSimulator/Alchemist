package it.unibo.alchemist.boundary.gui.util;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

import it.unibo.alchemist.boundary.interfaces.GraphicalOutputMonitor;
import it.unibo.alchemist.boundary.monitors.Generic2DDisplay;
import it.unibo.alchemist.core.interfaces.Simulation;

/**
 * A factory class for reflectively instancing {@link GraphicalOutputMonitor}.
 */
public final class GraphicalMonitorFactory {

    @SuppressWarnings("rawtypes")
    private static final Class<? extends GraphicalOutputMonitor> DEFAULT_MONITOR_CLASS = Generic2DDisplay.class;
    private static final String DEFAULT_MONITOR_PACKAGE = "it.unibo.alchemist.boundary.monitors.";

    private GraphicalMonitorFactory() {
    }

    /**
     * Utility to create new monitors depending on the specific simulation
     * preference.
     * 
     * @param sim
     *            the simulation
     * @param <T>
     *            concentration type
     * @return a {@link GraphicalOutputMonitor} whose type depends on the passed
     *         {@link Simulation} preferred type
     * @throws SecurityException in case of problem with reflective instantiation
     * @throws NoSuchMethodException in case of problem with reflective instantiation
     * @throws InvocationTargetException in case of problem with reflective instantiation
     * @throws IllegalArgumentException in case of problem with reflective instantiation
     * @throws IllegalAccessException in case of problem with reflective instantiation
     * @throws InstantiationException in case of problem with reflective instantiation
     * 
     */
    @SuppressWarnings("unchecked")
    public static <T> GraphicalOutputMonitor<T> createMonitor(final Simulation<T> sim) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException {
        String monitorClassName = sim.getEnvironment().getPreferredMonitor();
        Class<? extends GraphicalOutputMonitor<T>> monitorClass;
        if (monitorClassName == null) {
            monitorClass = (Class<? extends GraphicalOutputMonitor<T>>) DEFAULT_MONITOR_CLASS;
        } else {
            if (!monitorClassName.contains(".")) {
                monitorClassName = DEFAULT_MONITOR_PACKAGE + monitorClassName;
            }
            try {
                monitorClass = (Class<GraphicalOutputMonitor<T>>) Class.forName(monitorClassName);
            } catch (final ClassNotFoundException e) {
                monitorClass = (Class<? extends GraphicalOutputMonitor<T>>) DEFAULT_MONITOR_CLASS;
            }
        }
        return monitorClass.getConstructor().newInstance();
    }

    /**
     * Utility to create new monitors depending on the specific simulation
     * preference.
     * 
     * @param sim
     *            the simulation
     * @param onException
     *            the operation to perform in case an exception is generated
     * @param <T>
     *            concentration type
     * @return null in case of {@link Exception}, the
     *         {@link GraphicalOutputMonitor} otherwise
     */
    public static <T> GraphicalOutputMonitor<T> createMonitor(final Simulation<T> sim,
            final Consumer<Exception> onException) {
        try {
            return createMonitor(sim);
        } catch (Exception e) {
            onException.accept(e);
        }
        return null;
    }

}
