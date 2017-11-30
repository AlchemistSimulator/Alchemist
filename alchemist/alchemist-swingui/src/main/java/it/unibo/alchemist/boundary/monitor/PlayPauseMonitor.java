package it.unibo.alchemist.boundary.monitor;

import com.jfoenix.controls.JFXButton;
import it.unibo.alchemist.boundary.gui.utility.FXResourceLoader;
import it.unibo.alchemist.boundary.gui.utility.SVGImageUtils;
import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.core.interfaces.Status;
import it.unibo.alchemist.model.interfaces.Concentration;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;
import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconNode;
import org.jetbrains.annotations.Nullable;

/**
 * {@code OutputMonitor} that monitors the current {@link Status status} of the {@code Simulation}, acting as a toggle to
 * {@link Simulation#play() play} and {@link Simulation#pause() pause} the {@code Simulation}.
 *
 * @param <T> The type which describes the {@link Concentration} of a molecule
 */
public class PlayPauseMonitor<T> extends JFXButton implements OutputMonitor<T> {
    /**
     * Default serial version UID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Default {@link Status#READY ready} or {@link Status#PAUSED paused} icon.
     */
    private static final IconNode PLAY_ICON = FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.PLAY_ARROW);
    /**
     * Default {@link Status#RUNNING running} icon.
     */
    private static final IconNode PAUSE_ICON = FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.PAUSE);
    private transient WeakReference<Simulation<T>> simulation;
    private Status currentStatus = Status.INIT;
    private volatile boolean isError;

    /**
     * No arguments constructor. Current {@link Simulation} is not set.
     */
    public PlayPauseMonitor() {
        this(null);
    }

    /**
     * Default constructor.
     *
     * @param simulation the simulation to control
     */
    public PlayPauseMonitor(final @Nullable Simulation<T> simulation) {
        setOnAction(e -> getSimulation().ifPresent(this::playPause));
        update(simulation);
    }

    /**
     * Plays or pause the given simulation based on current simulation status.
     *
     * @param simulation the simulation to take status from
     */
    private void playPause(final Simulation<T> simulation) {
        switch (simulation.getStatus()) {
            case INIT:
            case READY:
            case PAUSED:
                simulation.play();
                setIcon(Status.RUNNING);
                break;
            case RUNNING:
                simulation.pause();
                setIcon(Status.PAUSED);
                break;
            case TERMINATED:
            default:
                // Do nothing
                break;
        }
    }


    @Override
    public void finished(final Environment<T> environment, final Time time, final long step) {
        update(environment.getSimulation());
    }

    @Override
    public void initialized(final Environment<T> environment) {
        update(environment.getSimulation());
    }

    @Override
    public void stepDone(final Environment<T> environment, final Reaction<T> reaction, final Time time, final long step) {
        update(environment.getSimulation());
    }

    /**
     * Getter method for the current simulation.
     *
     * @return the current simulation
     */
    public final Optional<Simulation<T>> getSimulation() {
        return Optional.ofNullable(simulation.get());
    }

    /**
     * Setter method for the simulation.
     *
     * @param simulation the simulation to set
     */
    public final void setSimulation(final @Nullable Simulation<T> simulation) {
        this.simulation = new WeakReference<>(simulation);
    }

    /**
     * Updates internal status of the simulation.
     *
     * @param simulation the simulation
     */
    private void update(final @Nullable Simulation<T> simulation) {
        setSimulation(simulation);
        if (!isError) {
            if (simulation != null && simulation.getStatus() != currentStatus) {
                setIcon(currentStatus);
                currentStatus = simulation.getStatus();
            }
        } else {
            getSimulation().ifPresent(Simulation::terminate);
        }
    }

    /**
     * Sets the icon of the {@code Button} from the current {@link #simulation} {@link Status}.
     * <p>
     * If no {@link Simulation} is set, it will simply set {@link #PLAY_ICON}.
     *
     * @see #PLAY_ICON
     * @see #PAUSE_ICON
     */
    private void setIcon(final Status nextStatus) {
        getSimulation().ifPresent(s -> {
            final long t = System.currentTimeMillis();
            if (nextStatus == Status.RUNNING || nextStatus == Status.PAUSED) {
                s.waitFor(nextStatus, 1, TimeUnit.SECONDS);
                final Status currentStatus = s.getStatus();
                if (!currentStatus.equals(nextStatus)) {
                    isError = true;
                    Platform.runLater(() -> {
                        final Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Expected simulation status error");
                        alert.setContentText("The expected status was "
                                + nextStatus + ", but, after waiting "
                                + (System.currentTimeMillis() - t)
                                + "ms, current simulation status is "
                                + currentStatus);
                        final DialogPane dialogPane = alert.getDialogPane();
                        dialogPane.setMinHeight(Region.USE_PREF_SIZE);
                        ((Stage) dialogPane
                                .getScene()
                                .getWindow())
                                .getIcons()
                                .add(SVGImageUtils.getSvgImage(SVGImageUtils.DEFAULT_ALCHEMIST_ICON_PATH));
                        alert.showAndWait().ifPresent(result -> {
                            Platform.exit();
                            System.exit(1);
                        });
                    });
                }
            }
            final Node icon = nextStatus == Status.RUNNING || nextStatus == Status.READY ? PAUSE_ICON : PLAY_ICON;
            Platform.runLater(() -> setGraphic(icon));
        });
    }
}
