package it.unibo.alchemist.boundary.monitor;

import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.model.interfaces.Concentration;
import it.unibo.alchemist.model.interfaces.Environment;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * {@code OutputMonitor} that monitors the current {@link Simulation#getStep() steps} of the {@code Simulation}.
 *
 * @param <T> the {@link Concentration} type
 */
public abstract class NumericLabelMonitor<N, T> extends Label implements OutputMonitor<T> {
    private final N init;
    private volatile boolean mayRender = true;
    private volatile N current;

    /**
     * Constructor.
     *
     * @param init the initial {@link N} class value
     */
    public NumericLabelMonitor(final @NotNull N init) {
        this.init = Objects.requireNonNull(init);
        setTextFill(Color.WHITE);
        setText(init.toString());
    }

    @Override
    public void initialized(final Environment<T> env) {
        update(init);
    }

    /**
     * Updates the GUI.
     *
     * @param val the value to update with
     */
    protected final void update(final N val) {
        current = val;
        if (mayRender) {
            mayRender = false;
            Platform.runLater(() -> {
                mayRender = true;
                setText(String.valueOf(current.toString()));
            });
        }
    }

}
