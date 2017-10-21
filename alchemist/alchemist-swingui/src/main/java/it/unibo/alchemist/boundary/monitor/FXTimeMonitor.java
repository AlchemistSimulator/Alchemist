package it.unibo.alchemist.boundary.monitor;

import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.model.interfaces.Concentration;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Optional;

/**
 * {@code OutputMonitor} that monitors the current {@link Simulation#getTime() time} of the {@code Simulation}.
 *
 * @param <T> the {@link Concentration} type
 */
public class FXTimeMonitor<T> extends Label implements OutputMonitor<T> {
    private static final double DEFAULT_TIME = 0;
    private WeakReference<Simulation<T>> simulation;

    /**
     * Default constructor.
     */
    public FXTimeMonitor() {
        this(null);
    }

    /**
     * Constructor.
     *
     * @param simulation the simulation to control
     */
    public FXTimeMonitor(final @Nullable Simulation<T> simulation) {
        setSimulation(simulation);
        setTextFill(Color.WHITE);
        setShownText();
    }

    /**
     * Getter method for the current simulation.
     *
     * @return the current simulation
     */
    @Nullable
    public Simulation<T> getSimulation() {
        return simulation.get();
    }

    /**
     * Setter method for the simulation.
     *
     * @param simulation the simulation to set
     */
    public void setSimulation(final @Nullable Simulation<T> simulation) {
        this.simulation = new WeakReference<>(simulation);
    }

    @Override
    public void finished(final Environment<T> env, final Time time, final long step) {
        update(env, time);
    }

    @Override
    public void initialized(final Environment<T> env) {
        update(env, null);
    }

    @Override
    public void stepDone(final Environment<T> env, final Reaction<T> r, final Time time, final long step) {
        update(env, time);
    }

    /**
     * Updates the GUI.
     *
     * @param environment the {@code Environment} that provides data
     * @param time        the current time; if null, it calls {@link #setShownText()}
     */
    private void update(final Environment<T> environment, final @Nullable Time time) {
        final Simulation<T> sim = environment.getSimulation();
        if (!sim.equals(getSimulation())) {
            setSimulation(sim);
        }

        if (time == null) {
            setShownText();
        } else {
            setShownText(time.toDouble());
        }
    }

    /**
     * Sets the given time.
     *
     * @param time the simulation time to show
     */
    private void setShownText(final double time) {
        Platform.runLater(() -> setText(String.valueOf(time)));
    }

    /**
     * Sets the current simulation time.
     */
    private void setShownText() {
        final Optional<Simulation<T>> sim = Optional.ofNullable(simulation.get());

        if (sim.isPresent()) {
            setShownText(sim.get().getTime().toDouble() * 1000);
        } else {
            setShownText(DEFAULT_TIME);
        }
    }
}
