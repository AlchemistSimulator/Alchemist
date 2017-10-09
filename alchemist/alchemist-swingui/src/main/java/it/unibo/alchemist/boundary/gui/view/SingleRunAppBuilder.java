package it.unibo.alchemist.boundary.gui.view;

import it.unibo.alchemist.core.interfaces.Simulation;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import org.jooq.lambda.tuple.Tuple2;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static it.unibo.alchemist.boundary.gui.view.SingleRunApp.Parameter.getParam;
import static javafx.application.Application.launch;

/**
 * Builder class for {@link SingleRunApp}, meant to be used to build and run the single run gui as the only JavaFX Application of the software.
 *
 * @param <T> the concentration type
 */
public class SingleRunAppBuilder<T> extends SingleRunApp.AbstractBuilder<T> {
    private final List<String> effectsGroups;
    private OptionalInt jFrameCloseOperation;
    private Optional<String> effectsGroupFile;

    /**
     * Default constructor of the builder.
     *
     * @param simulation the simulation to build the view for
     */
    public SingleRunAppBuilder(final Simulation<T> simulation) {
        super(simulation);
        this.jFrameCloseOperation = OptionalInt.empty();
        this.effectsGroupFile = Optional.empty();
        this.effectsGroups = new ArrayList<>();
    }

    /**
     * Simply runs the {@link SingleRunApp Application} with given params.
     *
     * @param args params for the {@link Application}
     */
    public static void main(final String... args) {
        launch(SingleRunApp.class, args);
    }

    @Override
    public SingleRunAppBuilder<T> monitorDisplay(final boolean monitorDisplay) {
        return (SingleRunAppBuilder<T>) super.monitorDisplay(monitorDisplay);
    }

    @Override
    public SingleRunAppBuilder<T> monitorSteps(final boolean monitorSteps) {
        return (SingleRunAppBuilder<T>) super.monitorSteps(monitorSteps);
    }

    @Override
    public SingleRunAppBuilder<T> monitorTime(final boolean monitorTime) {
        return (SingleRunAppBuilder<T>) super.monitorTime(monitorTime);
    }

    @Override
    public SingleRunAppBuilder<T> setEffectGroups(final File file) {
        this.effectsGroups.clear();
        this.effectsGroupFile = Optional.of(file.getAbsolutePath());
        return this;
    }

    @Override
    public SingleRunAppBuilder<T> addEffectGroup(final File file) {
        this.effectsGroups.add(file.getAbsolutePath());
        return this;
    }

    /**
     * Set the {@link Stage#setOnCloseRequest(EventHandler) default close operation} to a standard handler, identified by {@link JFrame} default close operations.
     *
     * @param jFrameCloseOperation the identifier of a standard handler to call when close operation is requested to the {@link Stage}
     * @return this builder
     * @throws IllegalArgumentException if specified operation is not a valid {@link JFrame} close operation
     * @see JFrame#DO_NOTHING_ON_CLOSE
     * @see JFrame#HIDE_ON_CLOSE
     * @see JFrame#DISPOSE_ON_CLOSE
     * @see JFrame#EXIT_ON_CLOSE
     */
    public SingleRunAppBuilder<T> setDefaultOnCloseOperation(final int jFrameCloseOperation) {
        if (jFrameCloseOperation == JFrame.DO_NOTHING_ON_CLOSE
                || jFrameCloseOperation == JFrame.HIDE_ON_CLOSE
                || jFrameCloseOperation == JFrame.DISPOSE_ON_CLOSE
                || jFrameCloseOperation == JFrame.EXIT_ON_CLOSE) {
            this.jFrameCloseOperation = OptionalInt.of(jFrameCloseOperation);
        } else {
            throw new IllegalArgumentException();
        }
        return this;
    }

    /**
     * Builds an array with the set of params specified to this builder.
     * <p>
     * Only
     *
     * @return
     */
    public String[] buildParams() {
        final List<String> params = new ArrayList<>();

        if (isMonitorDisplay()) {
            params.add(getParam(new Tuple2<>(
                    SingleRunApp.Parameter.USE_DEFAULT_DISPLAY_MONITOR_FOR_ENVIRONMENT_CLASS.getName(),
                    getSimulation().getEnvironment().getClass().getName())));
        }

        if (isMonitorSteps()) {
            params.add(getParam(new Tuple2<>(SingleRunApp.Parameter.USE_STEP_MONITOR.getName(), "")));
        }

        if (isMonitorTime()) {
            params.add(getParam(new Tuple2<>(SingleRunApp.Parameter.USE_TIME_MONITOR.getName(), "")));
        }

        // TODO effects

        // how TODO simulation ?

        return params.toArray(new String[]{});
    }

    /**
     * {@inheritDoc}
     * <br>
     * The new Application is started automatically.
     *
     * @see Application#launch(String...)
     */
    @Override
    public void build() {
        launch(SingleRunApp.class, buildParams());
    }
}
