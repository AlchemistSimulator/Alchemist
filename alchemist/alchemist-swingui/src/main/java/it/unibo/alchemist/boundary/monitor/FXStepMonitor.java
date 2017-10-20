package it.unibo.alchemist.boundary.monitor;

import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;
import javafx.scene.control.Label;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Optional;

public class FXStepMonitor<T> extends Label implements OutputMonitor<T> {
    private static final long DEFAULT_STEP = 0;
    private WeakReference<Simulation<T>> simulation;

    /**
     * Default constructor.
     */
    public FXStepMonitor() {
        this(null);
    }

    /**
     * Constructor.
     *
     * @param simulation the simulation to control
     */
    public FXStepMonitor(final @Nullable Simulation<T> simulation) {
        setSimulation(simulation);
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
        setSimulation(env.getSimulation());
        setShownText(step);
    }

    @Override
    public void initialized(final Environment<T> env) {
        setSimulation(env.getSimulation());
        setShownText();
    }

    @Override
    public void stepDone(final Environment<T> env, final Reaction<T> r, final Time time, final long step) {
        setSimulation(env.getSimulation());
        setShownText(step);
    }

    /**
     * Sets the given step.
     *
     * @param step the simulation step to show
     */
    private void setShownText(final long step) {
        setText(String.valueOf(step));
    }

    /**
     * Sets the current simulation step.
     */
    private void setShownText() {
        final Optional<Simulation<T>> sim = Optional.ofNullable(simulation.get());

        if (sim.isPresent()) {
            setShownText(sim.get().getStep());
        } else {
            setShownText(DEFAULT_STEP);
        }
    }
}
