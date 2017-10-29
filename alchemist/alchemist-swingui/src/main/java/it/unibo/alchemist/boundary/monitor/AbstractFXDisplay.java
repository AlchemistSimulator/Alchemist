package it.unibo.alchemist.boundary.monitor;

import it.unibo.alchemist.boundary.gui.effects.EffectFX;
import it.unibo.alchemist.boundary.gui.effects.EffectGroup;
import it.unibo.alchemist.boundary.interfaces.FXOutputMonitor;
import it.unibo.alchemist.boundary.wormhole.implementation.Wormhole2D;
import it.unibo.alchemist.boundary.wormhole.interfaces.BidimensionalWormhole;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Concentration;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import org.jetbrains.annotations.Nullable;

/**
 * Base abstract class for each display able to graphically represent a 2D space and simulation.
 *
 * @param <T> the {@link Concentration} type
 */
public abstract class AbstractFXDisplay<T> extends Canvas implements FXOutputMonitor<T> {

    /**
     * The default frame rate.
     */
    public static final byte DEFAULT_FRAME_RATE = 60;

    /**
     * The default time per frame.
     */
    public static final double TIME_STEP = 1 / DEFAULT_FRAME_RATE;

    /**
     * Default number of steps.
     */
    private static final int DEFAULT_NUMBER_OF_STEPS = 1;

    private final ObservableList<EffectGroup> effectStack;
    private final Semaphore mutex = new Semaphore(1);
    private final AtomicBoolean mayRender = new AtomicBoolean(true);
    private WeakReference currentEnvironment = new WeakReference<>(null);
    private int step;
    private BidimensionalWormhole wormhole;
    private boolean firstTime;
    private boolean realTime;
    private long timeInit;
    private double lastTime;

    /**
     * Default constructor. The number of steps is set to default ({@value #DEFAULT_NUMBER_OF_STEPS}).
     */
    public AbstractFXDisplay() {
        this(DEFAULT_NUMBER_OF_STEPS);
    }

    /**
     * Main constructor. It lets the developer specify the number of steps.
     *
     * @param steps the number of steps
     * @see #setStep(int)
     */
    public AbstractFXDisplay(final int steps) {
        super();
        this.firstTime = true;
        this.realTime = false;
        this.effectStack = FXCollections.observableArrayList();
        setStyle("-fx-background-color: #FFF;");
        setStep(steps);
        initMouseListener();
        initKeybindings();
    }

    /**
     * Initializes the mouse interaction to the {@link Canvas}.
     * <p>
     * Should be overridden to implement mouse interaction with the GUI.
     */
    protected void initMouseListener() {
        // TODO
    }

    /**
     * Initializes the key bindings.
     * <p>
     * Should be overridden to implement keyboard interaction with the GUI.
     */
    protected void initKeybindings() {
        // TODO
    }


    @Override
    public int getStep() {
        return this.step;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if the step is not bigger than 0
     */
    @Override
    public void setStep(final int step) throws IllegalArgumentException {
        if (step <= 0) {
            throw new IllegalArgumentException("The parameter must be a positive integer");
        }
        this.step = step;
    }

    @Override
    public boolean isRealTime() {
        return this.realTime;
    }

    @Override
    public void setRealTime(final boolean realTime) {
        this.realTime = realTime;
    }

    /**
     * {@inheritDoc}
     * <p>
     * It calls {@link #drawBackground(GraphicsContext, Environment)} and
     * {@link #drawEffects(GraphicsContext, Environment)} to repaint the {@code Canvas}.
     */
    @Override
    public final void repaint() {
        final GraphicsContext gc = this.getGraphicsContext2D();
        mutex.acquireUninterruptibly();
        getCurrentEnvironment().ifPresent(environment -> drawEffects(gc, environment));
        mutex.release();
    }

    /**
     * Changes the background of the specified {@code GraphicsContext}.
     *
     * @param graphicsContext the graphic component to draw on
     * @param environment     the {@code Environment} that contains the data to pass to {@code Effects}
     * @see #repaint()
     */
    protected void drawBackground(final GraphicsContext graphicsContext, final Environment<T> environment) {
        graphicsContext.clearRect(0, 0, getWidth(), getHeight());
    }

    /**
     * Draws each effect on the specified {@code GraphicsContext}.
     *
     * @param graphicsContext the graphic component to draw on
     * @param environment     the {@code Environment} that contains the data to pass to {@code Effects}
     * @see #repaint()
     */
    protected final void drawEffects(final GraphicsContext graphicsContext, final Environment<T> environment) {
        if (mayRender.get() && getWormhole() != null && isVisible() && !isDisabled()) {
            mayRender.set(false);
            final List<Runnable> commandQueue = getEffects().stream()
                    .filter(EffectGroup::isVisible)
                    .flatMap(EffectGroup::stream)
                    .filter(EffectFX::isVisibile)
                    .map(e -> e.apply(graphicsContext, environment, getWormhole()))
                    .collect(Collectors.toList());
            Platform.runLater(() -> {
                drawBackground(graphicsContext, environment);
                commandQueue.forEach(Runnable::run);
                mayRender.set(true);
            });
        }
    }

    /**
     * Getter method for the wormhole the converts {@link Environment} positions to GUI positions.
     *
     * @return the wormhole object
     */
    protected BidimensionalWormhole getWormhole() {
        return this.wormhole;
    }

    /**
     * Getter method for the {@link WeakReference weakly referenced} current {@link Environment}.
     *
     * @return the optional current {@code Environment}
     */
    @SuppressWarnings("unchecked")
    protected Optional<Environment<T>> getCurrentEnvironment() {

        return Optional.ofNullable((Environment<T>) currentEnvironment.get());
    }

    /**
     * Setter method for the current {@link Environment}.
     *
     * @param environment the current {@code Environment}; it could be null
     */
    protected void setCurrentEnvironment(final @Nullable Environment<T> environment) {
        this.currentEnvironment = new WeakReference<>(environment);
    }

    @Override
    public void addEffects(final Collection<EffectGroup> effects) {
        this.effectStack.addAll(effects);
    }

    @Override
    public void addEffectGroup(final EffectGroup effects) {
        this.effectStack.add(effects);
    }

    @Override
    public Collection<EffectGroup> getEffects() {
        return this.effectStack;
    }

    @Override
    public void setEffects(final Collection<EffectGroup> effects) {
        this.effectStack.clear();
        this.effectStack.addAll(effects);
    }

    @Override
    public void initialized(final Environment<T> environment) {
        stepDone(environment, null, new DoubleTime(), 0);
    }

    @Override
    public void stepDone(final Environment<T> environment, final Reaction<T> reaction, final Time time, final long step) {
        // TODO

        if (firstTime) {
            synchronized (this) {
                if (firstTime) {
                    wormhole = new Wormhole2D(environment, this);
                    wormhole.center();
                    wormhole.optimalZoom();
                    firstTime = false;
                    lastTime = -TIME_STEP;
                    timeInit = System.currentTimeMillis();
                    update(environment, time);
                }
            }
        } else {
            update(environment, time);
        }

        // TODO
    }

    @Override
    public void finished(final Environment<T> env, final Time time, final long step) {
        // TODO
        // update(environment, time);
        // firstTime = true;
    }

    /**
     * Updates parameter for correct {@code Environment} representation.
     *
     * @param environment the {@code Environment}
     * @param time        the current {@code Time} of simulation
     */
    private void update(final Environment<T> environment, final Time time) {
        if (Thread.holdsLock(environment)) {
            lastTime = time.toDouble();
            setCurrentEnvironment(environment);
            repaint();
        } else {
            throw new IllegalStateException("Only the simulation thread can dictate GUI updates");
        }
    }


    /**
     * The enum models the status of the view.
     */
    protected enum ViewStatus {
        VIEW_ONLY,
        MARK_CLOSER,
        SELECTING,
        MOVING,
        CLONING,
        DELETING,
        MOLECULING;
    }
}
