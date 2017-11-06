package it.unibo.alchemist.boundary.monitor.generic;

import it.unibo.alchemist.boundary.gui.effects.EffectGroup;
import it.unibo.alchemist.boundary.gui.utility.DataFormatFactory;
import it.unibo.alchemist.boundary.interfaces.FXOutputMonitor;
import it.unibo.alchemist.boundary.wormhole.implementation.Wormhole2D;
import it.unibo.alchemist.boundary.wormhole.interfaces.BidimensionalWormhole;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.*;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Base abstract class for each display able to graphically represent a 2D space and simulation.
 *
 * @param <T> The type which describes the {@link Concentration} of a molecule
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
    protected static final int DEFAULT_NUMBER_OF_STEPS = 1;
    /**
     * Position {@code DataFormat}.
     */
    protected static final DataFormat POSITION_DATA_FORMAT = DataFormatFactory.getDataFormat(Position.class);
    /**
     * The default view status.
     */
    private static final ViewStatus DEFAULT_VIEW_STATUS = ViewStatus.PAN;
    private static final String GET_X_METHOD_NAME = "getX";
    private static final String GET_Y_METHOD_NAME = "getY";
    private final ObservableList<EffectGroup> effectStack;
    private final Semaphore mutex = new Semaphore(1);
    private final AtomicBoolean mayRender = new AtomicBoolean(true);
    private int step;
    private BidimensionalWormhole wormhole;
    private volatile boolean firstTime;
    private boolean realTime;
    private long timeInit;
    private double lastTime;
    private volatile ConcurrentLinkedQueue<Runnable> commandQueue;
    private ViewStatus viewStatus;

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
        setStep(steps);
        this.commandQueue = new ConcurrentLinkedQueue<>();
        enableEventReceiving();
        setStyle("-fx-background-color: #FFF;");
        initMouseListener();
        setViewStatus(DEFAULT_VIEW_STATUS);
    }

    /**
     * Enables {@link MouseEvent} receiving by enabling Focus and requesting it.
     */
    private void enableEventReceiving() {
        setFocusTraversable(true);
        setFocused(true);
    }

    /**
     * Initializes the mouse interaction to the {@link Canvas}.
     * <p>
     * Should be overridden to implement mouse interaction with the GUI.
     */
    protected void initMouseListener() {
        // TODO
        setOnMouseClicked(event -> {
            switch (event.getButton()) {
                case PRIMARY:
                    // TODO Handle primary button
                    break;
                case SECONDARY:
                    // TODO Handle secondary button
                    break;
                default:
                    // Do nothing
                    break;
            }
        });

        setOnDragDetected(event -> {
            switch (getViewStatus()) {
                case PAN:
                    startEnvironmentDragNDrop(event);
                    break;
                case MOVING:
                    startNodeDragNDrop(event);
                    break;
                // TODO
                default:
                    break;
            }
        });

        setOnDragEntered(event -> {
            switch (getViewStatus()) {
                case PAN:
                    onEnvironmentDragEntered(event);
                    break;
                case MOVING:
                    onNodeDragEntered(event);
                    break;
                // TODO
                default:
                    break;
            }
        });
    }

    /**
     * Getter method for the current view status.
     *
     * @return the current {@code ViewStatus}
     */
    protected ViewStatus getViewStatus() {
        return this.viewStatus;
    }

    /**
     * Setter method for the current view status.
     *
     * @param viewStatus the {@code ViewStatus} to set
     */
    protected void setViewStatus(final ViewStatus viewStatus) {
        this.viewStatus = viewStatus;
    }

    /**
     * The method returns the {@link Position} in the {@link Environment} of the given {@code Event}, if any.
     *
     * @param event the event to check
     * @return the position, if any
     */
    protected final Optional<Position> getEventPosition(final InputEvent event) {
        final BidimensionalWormhole wormhole = getWormhole();
        final Method[] methods = event.getClass().getMethods();
        Optional<Method> getX = Optional.empty();
        Optional<Method> getY = Optional.empty();

        for (final Method method : methods) {
            final int modifier = method.getModifiers();
            if (Modifier.isPublic(modifier) && !Modifier.isAbstract(modifier)) {
                final String name = method.getName();
                if (name.equals(GET_X_METHOD_NAME)) {
                    getX = Optional.of(method);
                } else if (name.equals(GET_Y_METHOD_NAME)) {
                    getY = Optional.of(method);
                }
                if (getX.isPresent() && getY.isPresent()) {
                    break;
                }
            }
        }

        if (wormhole != null && getX.isPresent() && getY.isPresent()) {
            try {
                final Number x = (Number) getX.get().invoke(event);
                final Number y = (Number) getY.get().invoke(event);
                return Optional.of(wormhole.getEnvPoint(new Point(x.intValue(), y.intValue())));
            } catch (final IllegalAccessException | InvocationTargetException e) {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    protected void startNodeDragNDrop(final MouseEvent event) {
        // TODO
    }

    protected void startEnvironmentDragNDrop(final MouseEvent event) {
        final Optional<Position> position = getEventPosition(event);
        position.ifPresent(p -> {
            final Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
            final ClipboardContent content = new ClipboardContent();
            content.put(POSITION_DATA_FORMAT, p);
            dragboard.setContent(content);
        });

        event.consume();
    }

    protected void onEnvironmentDragEntered(final DragEvent event) {
        final BidimensionalWormhole wormhole = getWormhole();
        if (wormhole != null) {
            final Position previousMousePos = (Position) event.getDragboard().getContent(POSITION_DATA_FORMAT);
            repaint();
        }
    }

    protected void onNodeDragEntered(final DragEvent event) {
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

    @Override
    public final void repaint() {
        mutex.acquireUninterruptibly();
        if (mayRender.get() && getWormhole() != null && isVisible() && !isDisabled()) {
            mayRender.set(false);
            Platform.runLater(() -> {
                commandQueue.forEach(Runnable::run);
                mayRender.set(true);
            });
        }
        mutex.release();
    }

    /**
     * Changes the background of the specified {@code GraphicsContext}.
     *
     * @param graphicsContext the graphic component to draw on
     * @param environment     the {@code Environment} that contains the data to pass to {@code Effects}
     * @see #repaint()
     */
    protected Runnable drawBackground(final GraphicsContext graphicsContext, final Environment<T> environment) {
        return () -> graphicsContext.clearRect(0, 0, getWidth(), getHeight());
    }

    /**
     * Getter method for the wormhole the converts {@link Environment} positions to GUI positions.
     *
     * @return the wormhole object
     */
    @Nullable
    protected BidimensionalWormhole getWormhole() {
        return this.wormhole;
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
        if (firstTime) {
            synchronized (this) {
                if (firstTime) {
                    init(environment);
                    update(environment, time);
                }
            }
        } else {
            update(environment, time);
        }
    }

    /**
     * The method initializes everything is not initializable before first step.
     */
    protected void init(final Environment<T> environment) {
        wormhole = new Wormhole2D(environment, this);
        wormhole.center();
        wormhole.optimalZoom();
        firstTime = false;
        lastTime = -TIME_STEP;
        timeInit = System.currentTimeMillis();
    }

    @Override
    public void finished(final Environment<T> environment, final Time time, final long step) {
        update(environment, time);
        firstTime = true;
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
            final GraphicsContext graphicsContext = this.getGraphicsContext2D();
            final Stream<Runnable> background = Stream.of(drawBackground(graphicsContext, environment));
            final Stream<Runnable> effects = getEffects()
                    .stream()
                    .map(group -> group.computeDrawCommands(environment))
                    .flatMap(Collection::stream)
                    .map(cmd -> () -> cmd.accept(graphicsContext, getWormhole()));
            commandQueue = Stream
                    .concat(background, effects)
                    .collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
            repaint();
        } else {
            throw new IllegalStateException("Only the simulation thread can dictate GUI updates");
        }
    }

    /**
     * The enum models the status of the view.
     */
    protected enum ViewStatus {
        SELECTING,
        MOVING,
        CLONING,
        DELETING,
        EDITING,
        PAN
    }
}
