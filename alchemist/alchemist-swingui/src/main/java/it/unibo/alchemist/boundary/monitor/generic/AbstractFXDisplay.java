package it.unibo.alchemist.boundary.monitor.generic;

import it.unibo.alchemist.boundary.gui.effects.EffectGroup;
import it.unibo.alchemist.boundary.gui.utility.DataFormatFactory;
import it.unibo.alchemist.boundary.interfaces.FXOutputMonitor;
import it.unibo.alchemist.boundary.wormhole.implementation.Wormhole2D;
import it.unibo.alchemist.boundary.wormhole.interfaces.BidimensionalWormhole;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Concentration;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Position2D;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;
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
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import org.jetbrains.annotations.Nullable;

/**
 * Base abstract class for each display able to graphically represent a 2D space and simulation.
 *
 * @param <T> The type which describes the {@link Concentration} of a molecule
 * @param <P> The type of position
 */
public abstract class AbstractFXDisplay<T, P extends Position2D<? extends P>> extends Canvas implements FXOutputMonitor<T, P> {
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
     * Default serial version UID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * The default view status.
     */
    private static final FXOutputMonitor.ViewStatus DEFAULT_VIEW_STATUS = FXOutputMonitor.ViewStatus.PAN;
    private static final String GET_X_METHOD_NAME = "getX";
    private static final String GET_Y_METHOD_NAME = "getY";
    private final ObservableList<EffectGroup> effectStack;
    private final Semaphore mutex = new Semaphore(1);
    private final AtomicBoolean mayRender = new AtomicBoolean(true);
    private int step;
    private BidimensionalWormhole<P> wormhole;
    private volatile boolean firstTime;
    private boolean realTime;
    private volatile ConcurrentLinkedQueue<Runnable> commandQueue;
    private FXOutputMonitor.ViewStatus viewStatus;

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
        initMouseListener(); // NOPMD - the method is meant to be overridable
        setViewStatus(DEFAULT_VIEW_STATUS); // NOPMD - the method is meant to be overridable
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

    @Override
    public final ViewStatus getViewStatus() {
        return this.viewStatus;
    }

    @Override
    public final void setViewStatus(final ViewStatus viewStatus) {
        this.viewStatus = viewStatus;
    }

    /**
     * The method returns the {@link Position} in the {@link Environment} of the given {@code Event}, if any.
     *
     * @param event the event to check
     * @return the position, if any
     */
    protected final Optional<P> getEventPosition(final InputEvent event) {
        final BidimensionalWormhole<P> wormhole = getWormhole();
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

    /**
     * The method is meant to handle what should the monitor do when a drag'n'drop of a {@link Node} starts.
     *
     * @param event the event
     */
    protected void startNodeDragNDrop(final MouseEvent event) {
        // TODO
    }

    /**
     * The method is meant to handle what should the monitor do when a pan gesture in the {@link Environment} starts.
     *
     * @param event the event
     */
    protected void startEnvironmentDragNDrop(final MouseEvent event) {
        final Optional<P> position = getEventPosition(event);
        position.ifPresent(p -> {
            final Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
            final ClipboardContent content = new ClipboardContent();
            content.put(POSITION_DATA_FORMAT, p);
            dragboard.setContent(content);
        });

        event.consume();
    }

    /**
     * The method is meant to handle what should the monitor do when the mouse enters in a valid position during a pan gesture in the {@link Environment}.
     *
     * @param event the event
     */
    protected void onEnvironmentDragEntered(final DragEvent event) {
//        final BidimensionalWormhole wormhole = getWormhole();
//        if (wormhole != null) {
//            final Position previousMousePos = (Position) event.getDragboard().getContent(POSITION_DATA_FORMAT);
//            repaint();
//        }
        // TODO
    }

    /**
     * The method is meant to handle what should the monitor do when the mouse enters in a valid position during a drag'n'drop gesture of a {@link Node}.
     *
     * @param event the event
     */
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
     * @return a function of what to do to draw the background
     * @see #repaint()
     */
    protected Runnable drawBackground(final GraphicsContext graphicsContext, final Environment<T, P> environment) {
        return () -> graphicsContext.clearRect(0, 0, getWidth(), getHeight());
    }

    /**
     * Getter method for the wormhole the converts {@link Environment} positions to GUI positions.
     *
     * @return the wormhole object
     */
    @Nullable
    protected BidimensionalWormhole<P> getWormhole() {
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
    public void initialized(final Environment<T, P> environment) {
        stepDone(environment, null, new DoubleTime(), 0);
    }

    @Override
    public void stepDone(final Environment<T, P> environment, final Reaction<T> reaction, final Time time, final long step) {
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
     *
     * @param environment the {@code Environment}
     */
    protected void init(final Environment<T, P> environment) {
        wormhole = new Wormhole2D<>(environment, this);
        wormhole.center();
        wormhole.optimalZoom();
        firstTime = false;
        System.currentTimeMillis();
    }

    @Override
    public void finished(final Environment<T, P> environment, final Time time, final long step) {
        update(environment, time);
        firstTime = true;
    }

    /**
     * Updates parameter for correct {@code Environment} representation.
     *
     * @param environment the {@code Environment}
     * @param time        the current {@code Time} of simulation
     */
    private void update(final Environment<T, P> environment, final Time time) {
        if (Thread.holdsLock(environment)) {
            time.toDouble();
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
}
