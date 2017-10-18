package it.unibo.alchemist.boundary.monitors;

import it.unibo.alchemist.boundary.gui.effects.EffectFX;
import it.unibo.alchemist.boundary.gui.effects.EffectGroup;
import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.boundary.wormhole.implementation.Wormhole2D;
import it.unibo.alchemist.boundary.wormhole.interfaces.BidimensionalWormhole;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class EffectsDisplay<T> extends Canvas implements OutputMonitor<T> {

    /**
     * The default frame rate.
     */
    public static final byte DEFAULT_FRAME_RATE = 60;
    /**
     * The default time per frame.
     */
    public static final double TIME_STEP = 1 / DEFAULT_FRAME_RATE;
    /**
     * Threshold of pause detection.
     */
    public static final long PAUSE_DETECTION_THRESHOLD = 200;
    /**
     * Default number of steps.
     */
    private static final int DEFAULT_NUMBER_OF_STEPS = 1;
    private final Collection<EffectGroup> effectStack;
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
    public EffectsDisplay() {
        this(DEFAULT_NUMBER_OF_STEPS);
    }

    /**
     * Main constructor. It lets the developer specify the number of steps.
     *
     * @param steps the number of steps
     * @see #setStep(int)
     */
    public EffectsDisplay(final int steps) {
        super();
        this.firstTime = false;
        this.realTime = false;
        this.effectStack = new ArrayList<>();
        setStyle("-fx-background-color: #FFF;");
        setStep(steps);
        initMouseListener();
        initKeyboardListener();
    }

    private void initMouseListener() {
        // TODO
    }

    private void initKeyboardListener() {
        // TODO
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
        } else if (this.step < 1 || step % this.step == 0) {
            if (isRealTime()) {
                if (lastTime + TIME_STEP > time.toDouble()) {
                    return;
                }
                final long timeSimulated = (long) (time.toDouble() * 1000);
                if (timeSimulated == 0) {
                    timeInit = System.currentTimeMillis();
                }
                final long timePassed = System.currentTimeMillis() - timeInit;
                if (timePassed - timeSimulated > PAUSE_DETECTION_THRESHOLD) {
                    timeInit = timeInit + timePassed - timeSimulated;
                }
                if (timeSimulated > timePassed) {
                    try {
                        Thread.sleep(Math.min(timeSimulated - timePassed, 1000 / DEFAULT_FRAME_RATE));
                    } catch (final InterruptedException e) {
//                        getLogger().warn("Spurious wakeup"); // TODO load from ResourceBundle with ResourceLoader
                    }
                }
            }
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
     * Repaints this {@link Canvas}' {@link GraphicsContext} by drawing all the {@link EffectFX Effect}s of each {@link Node} of the specified {@link Environment}.
     * <p>
     * It calls {@link #drawBackground(GraphicsContext, Environment)} and {@link #drawEffects(GraphicsContext, Environment)} to repaint the {@code Canvas}.
     */
    protected final void repaint() {
        final GraphicsContext gc = this.getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        getCurrentEnvironment().ifPresent(environment -> {
            drawBackground(gc, environment);
            drawEffects(gc, environment);
        });
    }

    /**
     * Changes the background of the specified {@code GraphicsContext}.
     *
     * @param graphicsContext the graphic component to draw on
     * @param environment     the {@code Environment} that contains the data to pass to {@code Effects}
     * @see #repaint()
     */
    @SuppressWarnings("unused")
    protected void drawBackground(final GraphicsContext graphicsContext, final Environment<T> environment) {
        // By default, it draws nothing
    }

    /**
     * Draws each effect on the specified {@code GraphicsContext}.
     *
     * @param graphicsContext the graphic component to draw on
     * @param environment     the {@code Environment} that contains the data to pass to {@code Effects}
     * @see #repaint()
     */
    protected void drawEffects(final GraphicsContext graphicsContext, final Environment<T> environment) {
        if (getWormhole() != null && isVisible() && !isDisabled()) {
            getEffects().forEach(eg -> eg.forEach(e -> e.apply(graphicsContext, environment, getWormhole())));
        }
    }

    /**
     * Getter method for the {@link WeakReference weak-referenced} current {@link Environment}.
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

    /**
     * @return how many simulation steps this monitor updates the graphics
     */
    public int getStep() {
        return this.step;
    }

    /**
     * @param step How many steps should be computed by the engine for the
     *             display to update the graphics
     * @throws IllegalArgumentException if the step is not bigger than 0
     */
    public void setStep(final int step) throws IllegalArgumentException {
        if (step <= 0) {
            throw new IllegalArgumentException("The parameter must be a positive integer");
        }
        this.step = step;
    }

    /**
     * Getter method for the {@link BidimensionalWormhole Wormhole} object that manages point translation.
     *
     * @return the {@code wormhole} object
     */
    protected BidimensionalWormhole getWormhole() {
        return this.wormhole;
    }

    public boolean isRealTime() {
        return this.realTime;
    }

    public void setRealTime(final boolean realTime) {
        this.realTime = realTime;
    }

    public void addEffects(final Collection<EffectGroup> effects) {
        this.effectStack.addAll(effects);
    }

    public void addEffectGroup(final EffectGroup effects) {
        this.effectStack.add(effects);
    }

    public Collection<EffectGroup> getEffects() {
        return this.effectStack;
    }

    /**
     * @param effects the Effect stack to use
     */
    public void setEffects(final Collection<EffectGroup> effects) {
        this.effectStack.clear();
        this.effectStack.addAll(effects);
    }

    private void update(final Environment<T> environment, final Time time) throws IllegalStateException {
        // TODO
        lastTime = time.toDouble();
        setCurrentEnvironment(environment);
        repaint();
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
