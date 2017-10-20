package it.unibo.alchemist.boundary.monitor;

import com.jfoenix.controls.JFXButton;
import it.unibo.alchemist.boundary.gui.utility.FXResourceLoader;
import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.core.interfaces.Status;
import it.unibo.alchemist.model.interfaces.Concentration;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;
import javafx.application.Platform;
import javafx.scene.control.Button;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconNode;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Optional;

/**
 * {@code OutputMonitor} that monitors the current {@link Status status} of the {@code Simulation}, acting as a toggle to
 * {@link Simulation#play() play} and {@link Simulation#pause() pause} the {@code Simulation}.
 *
 * @param <T> the {@link Concentration} type
 */
public class StatusMonitor<T> extends JFXButton implements OutputMonitor<T> {

    /**
     * Default {@link Status#READY ready} or {@link Status#PAUSED paused} icon.
     */
    private static final IconNode PLAY_ICON = FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.PLAY_ARROW);

    /**
     * Default {@link Status#RUNNING running} icon.
     */
    private static final IconNode PAUSE_ICON = FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.PAUSE);

    private WeakReference<Simulation<T>> simulation;

    /**
     * Default constructor.
     */
    public StatusMonitor() {
        this(null);
    }

    /**
     * Constructor.
     *
     * @param simulation the simulation to control
     */
    public StatusMonitor(final @Nullable Simulation<T> simulation) {
        setSimulation(simulation);
        setIcon();
        setOnAction(e -> Optional.ofNullable(getSimulation()).ifPresent(this::playPause));
    }

    /**
     * Plays or pause the given simulation based on current simulation status.
     *
     * @param simulation the simulation to take status from
     */
    private void playPause(final Simulation<T> simulation) {
        Optional.ofNullable(getSimulation()).ifPresent(s -> {
            if (s.getStatus() == Status.RUNNING) {
                s.pause();
            } else {
                s.play();
            }
            setIcon();
        });
    }


    @Override
    public void finished(final Environment<T> env, final Time time, final long step) {
        setSimulation(env.getSimulation());
        setIcon();
    }

    @Override
    public void initialized(final Environment<T> env) {
        setSimulation(env.getSimulation());
        setIcon();
    }

    @Override
    public void stepDone(final Environment<T> env, final Reaction<T> r, final Time time, final long step) {
        // Do nothing
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

    /**
     * Sets the icon of the {@code Button} from the current {@link #simulation} {@link Status}.
     * <p>
     * If no {@link Simulation} is set, it will simply set {@link #PLAY_ICON}.
     *
     * @see #PLAY_ICON
     * @see #PAUSE_ICON
     */
    private void setIcon() {
        final Optional<Simulation<T>> sim = Optional.ofNullable(simulation.get());

        Platform.runLater(() -> {
            if (sim.isPresent()) {
                setGraphic(sim.get().getStatus() == Status.RUNNING ? PLAY_ICON : PAUSE_ICON);
            } else {
                setGraphic(PLAY_ICON);
            }
        });
    }
}
