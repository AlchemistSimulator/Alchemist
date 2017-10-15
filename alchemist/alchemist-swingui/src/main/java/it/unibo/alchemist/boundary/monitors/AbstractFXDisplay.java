package it.unibo.alchemist.boundary.monitors;

import it.unibo.alchemist.boundary.gui.effects.EffectGroup;
import it.unibo.alchemist.boundary.gui.utility.ResourceLoader;
import it.unibo.alchemist.boundary.interfaces.FXOutputMonitor;
import it.unibo.alchemist.boundary.wormhole.implementation.*;
import it.unibo.alchemist.boundary.wormhole.interfaces.BidimensionalWormhole;
import it.unibo.alchemist.boundary.wormhole.interfaces.PointerSpeed;
import it.unibo.alchemist.boundary.wormhole.interfaces.ZoomManager;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.core.interfaces.Status;
import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.*;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import org.apache.commons.math3.util.Pair;
import org.danilopianini.lang.LangUtils;
import org.slf4j.Logger;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.function.Function;
import java.util.stream.Collectors;

import static it.unibo.alchemist.boundary.monitors.AbstractFXDisplay.ViewStatus.*;
import static it.unibo.alchemist.boundary.wormhole.interfaces.BidimensionalWormhole.Mode;

/**
 * Base-class for each display able to graphically represent a 2D space
 * and simulation.
 * <p>
 * Tries to use OpenGL acceleration when possible.
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
     * Threshold of pause detection.
     */
    public static final long PAUSE_DETECTION_THRESHOLD = 200;
    /**
     * The default radius of freedom of representation.
     */
    protected static final double FREEDOM_RADIUS = 1;
    /**
     * Default number of steps.
     */
    private static final int DEFAULT_NUMBER_OF_STEPS = 1;
    /**
     * Default status of the view.
     */
    private static final ViewStatus DEFAULT_VIEW_STATUS = MARK_CLOSER;
    private static final byte SELECTED_NODE_DRAWING_SIZE = 16;
    private static final byte SELECTED_NODE_INTERNAL_SIZE = 10;

    private final ConcurrentMap<Node<T>, Position> positions;
    private final ConcurrentMap<Node<T>, Neighborhood<T>> neighbors;
    private int step;
    private BidimensionalWormhole wormhole;
    private double mouseX;
    private double mouseY;
    private Node<T> nearest;
    private ViewStatus status;
    private Environment<T> currentEnv;
    private double lastTime;
    private Set<Node<T>> selectedNodes;
    private List<? extends Obstacle2D> obstacles;
    private Semaphore mapConsistencyMutex;
    private boolean isPreviousStateMarking;
    private Optional<Node<T>> hooked;
    private transient PointerSpeed mouseMovement;
    private transient boolean isDraggingMouse;
    private transient Optional<Point> originPoint;
    private transient Optional<Point> endingPoint;
    private transient AngleManagerImpl angleManager;
    private transient ZoomManager zoomManager;
    private boolean realTime;
    private Collection<EffectGroup> effectStack;
    private boolean firstTime;
    private long timeInit;
    private boolean paintLinks;

    /**
     * Default constructor. The number of steps is set to default ({@value #DEFAULT_NUMBER_OF_STEPS}).
     */
    public AbstractFXDisplay() {
        this(DEFAULT_NUMBER_OF_STEPS);
    }

    /**
     * Main constructor. It lets the developer specify the number of steps.
     *
     * @param step the number of steps
     * @see #setStep(int)
     */
    public AbstractFXDisplay(final int step) {
        super();
        setStyle("-fx-background-color: #FFF;");
        setMouseListener();
        setkeyboardListener();
        setStep(step);
        status = DEFAULT_VIEW_STATUS;
        isPreviousStateMarking = true;
        mapConsistencyMutex = new Semaphore(1);
        positions = new ConcurrentHashMap<>();
        neighbors = new ConcurrentHashMap<>();
        hooked = Optional.empty();
        mouseMovement = new PointerSpeedImpl();
        originPoint = Optional.empty();
        endingPoint = Optional.empty();
        realTime = false;
        firstTime = true;
        paintLinks = false;
    }

    /**
     * The method checks if if the {@code Environment} is a subclass of {@link Environment2DWithObstacles} and has mobile obstacles.
     *
     * @param environment the {@code Environment} to check
     * @return true if the {@code Environment} is a subclass of {@link Environment2DWithObstacles} and has mobile obstacles
     */
    protected static boolean envHasMobileObstacles(final Environment<?> environment) {
        return environment instanceof Environment2DWithObstacles && ((Environment2DWithObstacles<?, ?>) environment).hasMobileObstacles();
    }

    /**
     * Returns a {@link Point Java AWT point} with the position of {@link MouseEvent JavaFX mouse event}.
     *
     * @param event the mouse event
     * @return the position of the event
     * @see MouseEvent#getX()
     * @see MouseEvent#getY()
     * @see java.awt.event.MouseEvent#getPoint()
     */
    private static Point getPoint(final MouseEvent event) {
        return getPoint(event.getX(), event.getY());
    }

    /**
     * Returns a {@link Point Java AWT point} with any kind of number.
     * <br/>
     * Useful to adapt JavaFX coordinates without explicitly cat arguments.
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     * @return the point
     * @see Point#Point(int, int)
     */
    private static Point getPoint(final Number x, final Number y) {
        return new Point((int) x, (int) y);
    }

    private static <I, O> Pair<O, O> mapPair(final Pair<? extends I, ? extends I> pair, final Function<? super I, ? extends O> converter) {
        return new Pair<>(converter.apply(pair.getFirst()), converter.apply(pair.getSecond()));
    }

    private static boolean isInsideRectangle(final Point viewPoint, final int rx, final int ry, final int width, final int height) {
        final double x = viewPoint.getX();
        final double y = viewPoint.getY();
        return x >= rx && x <= rx + width && y >= ry && y <= ry + height;
    }

    /**
     * The method maps the keyboard events to action on the GUI.
     * <p>
     * <b>C</b>: from {@link ViewStatus#SELECTING Select mode} to {@link ViewStatus#CLONING Clone mode}
     * <br/>
     * <b>D</b>: from {@link ViewStatus#SELECTING Select mode} to {@link ViewStatus#DELETING Delete mode}
     * <br/>
     * <b>E</b>: from {@link ViewStatus#SELECTING Select mode} to {@link ViewStatus#MOLECULING Moleculing mode}
     * <br/>
     * <b>M</b>: {@link #setMarkCloserNode(boolean) mark/unmark} closer node
     * <br/>
     * <b>O</b>: from {@link ViewStatus#SELECTING Select mode} to {@link ViewStatus#MOVING Move mode}
     * <br/>
     * <b>P</b>: {@link Simulation#play() play}/{@link Simulation#pause() pause} the {@code Simulation}
     * <br/>
     * <b>R</b>: {@link #setRealTime(boolean) set/unset} Real-Time mode
     * <br/>
     * <b>S</b>: to {@link ViewStatus#SELECTING Select mode} if not, or else {@link #resetStatus()}
     * <br/>
     * <b>LEFT</b>: {@link #setStep(int) decrease step}
     * <br/>
     * <b>RIGHT</b>: {@link #setStep(int) increase step}
     *
     * @see #setOnKeyPressed(EventHandler)
     */
    protected void setkeyboardListener() {
        setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case C:
                    if (status == SELECTING) {
                        status = CLONING;
                    }
                    break;
                case D:
                    if (status == SELECTING) {
                        this.status = DELETING;
                        selectedNodes.forEach(currentEnv::removeNode);
                        final Simulation<T> sim = currentEnv.getSimulation();
                        sim.schedule(() -> update(currentEnv, sim.getTime()));
                        resetStatus();
                    }
                    break;
                case E:
                    if (status == SELECTING) {
                        status = ViewStatus.MOLECULING;
                        // TODO display Moleculing interface v
//                        Generic2DDisplay.makeFrame("Moleculing", new MoleculeInjectorGUI<>(selectedNodes), jf -> {
//                            final JFrame mol = (JFrame) jf;
//                            mol.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//                            mol.addWindowListener(new WindowAdapter() {
//                                @Override
//                                public void windowClosed(final WindowEvent e) {
//                                    selectedNodes.clear();
//                                    resetStatus();
//                                }
//                            });
//                        });
                        // TODO display Moleculing interface ^
                    }
                    break;
                case L:
                    // Now the link drawing is an effect
                    break;
                case M:
                    setMarkCloserNode(status != MARK_CLOSER);
                    break;
                case O:
                    if (status == SELECTING) {
                        status = MOVING;
                    }
                    break;
                case P:
                    Optional.ofNullable(currentEnv.getSimulation()).ifPresent(sim -> {
                        if (sim.getStatus() == Status.RUNNING) {
                            sim.pause();
                        } else {
                            sim.play();
                        }
                    });
                    break;
                case R:
                    setRealTime(!isRealTime());
                    break;
                case S:
                    if (status == SELECTING) {
                        resetStatus();
                        selectedNodes.clear();
                    } else if (!isInteracting()) {
                        status = SELECTING;
                    }
                    repaintEnvironment(null); // TODO implement a way to have access to Environment here !!!!!!!!!!!!!!!
                    break;
                case LEFT:
                    setStep(Math.max(1, step - Math.max(step / 10, 1)));
                    break;
                case RIGHT:
                    setStep(Math.max(step, step + Math.max(step / 10, 1)));
                    break;
            }
        });
    }

    /**
     * The method maps the mouse events to action on the GUI.
     *
     * @see #setOnMouseClicked(EventHandler)
     * @see #setOnMouseDragged(EventHandler)
     * @see #setOnMouseEntered(EventHandler)
     * @see #setOnMouseExited(EventHandler)
     * @see #setOnMouseMoved(EventHandler)
     * @see #setOnMousePressed(EventHandler)
     * @see #setOnMouseReleased(EventHandler)
     * @see #setOnScroll(EventHandler)
     */
    protected void setMouseListener() {
        // On mouse clicked
        setOnMouseClicked(event -> {
            setDist(event.getX(), event.getY());
            if (isMarkCloserNode() && nearest != null && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                final FXNodeTracker<T> monitor = new FXNodeTracker<>(nearest);
                monitor.stepDone(currentEnv, null, new DoubleTime(lastTime), step); // TODO check
                final Simulation<T> simulation = currentEnv.getSimulation();
                // TODO open a drawer to the right of AbstractBuilder v
//                makeFrame("Tracker for node " + nearest.getId(), monitor, jf -> {
//                    final JFrame frame = (JFrame) jf;
//                    if (sim != null) {
//                        sim.addOutputMonitor(monitor);
//                        frame.addWindowListener(new WindowAdapter() {
//                            @Override
//                            public void windowClosing(final WindowEvent event) {
//                                sim.removeOutputMonitor(monitor);
//                            }
//                        });
//                    }
//                });
                // TODO open a drawer to the right of AbstractBuilder ^
            } else if (status == CLONING && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                final Simulation<T> engine = currentEnv.getSimulation();
                final Position envEnding = wormhole.getEnvPoint(getPoint(event));
                engine.schedule(() -> {
                    final Collection<Node<T>> newNodes = new ArrayList<>(selectedNodes.size());
                    try {
                        selectedNodes.forEach(n -> newNodes.add(n.cloneNode(engine.getTime())));
                        newNodes.forEach(n -> currentEnv.addNode(n, envEnding));

                        update(currentEnv, engine.getTime());
                    } catch (final RuntimeException exception) {
                        final Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Node cloning error"); // TODO load from ResourceBundle with ResourceLoader
                        alert.setHeaderText("Node cloning error"); // TODO load from ResourceBundle with ResourceLoader
                        alert.setContentText("One or more of your nodes do not support cloning"); // TODO load from ResourceBundle with ResourceLoader

                        final Label message = new Label("The debug information is:"); // TODO load from ResourceBundle with ResourceLoader
                        final TextArea exceptionText = new TextArea(LangUtils.stackTraceToString(exception));
                        exceptionText.setEditable(false);
                        exceptionText.setWrapText(true);

                        exceptionText.setMaxWidth(Double.MAX_VALUE);
                        exceptionText.setMaxHeight(Double.MAX_VALUE);
                        GridPane.setVgrow(exceptionText, Priority.ALWAYS);
                        GridPane.setHgrow(exceptionText, Priority.ALWAYS);

                        final GridPane expandableContent = new GridPane();
                        expandableContent.setMaxWidth(Double.MAX_VALUE);
                        expandableContent.add(message, 0, 0);
                        expandableContent.add(exceptionText, 0, 1);
                        alert.getDialogPane().setExpandableContent(expandableContent);

                        alert.showAndWait();
                    }
                });
                selectedNodes.clear();
                resetStatus();
            }
            if (nearest != null && MouseButton.MIDDLE == event.getButton()) {
                hooked = hooked.isPresent() ? Optional.empty() : Optional.of(nearest);
            }
            repaintEnvironment(null); // TODO implement a way to have access to Environment here !!!!!!!!!!!!!!!!!!!!!!!
        });

        // On mouse dragged
        setOnMouseDragged(event -> {
            setDist(event.getX(), event.getY());
            if (wormhole != null && mouseMovement != null) {
                switch (event.getButton()) {
                    case PRIMARY:
                        if (isDraggingMouse) {
                            endingPoint = Optional.of(getPoint(event));
                        }
                        if (!hooked.isPresent() && !isInteracting()) {
                            final Point previous = wormhole.getViewPosition();
                            wormhole.setViewPosition(PointAdapter
                                    .from(previous)
                                    .sum(PointAdapter.from(mouseMovement.getVariation()))
                                    .toPoint());
                        }
                        break;
                    case SECONDARY:
                        if (angleManager != null && wormhole.getMode() != Mode.MAP) {
                            angleManager.inc(mouseMovement.getVariation().getX());
                            wormhole.rotateAroundPoint(getCenter(), angleManager.getAngle());
                        }
                        break;
                }
                mouseMovement.setCurrentPosition(getPoint(event));
                repaintEnvironment(null); // TODO implement a way to have access to Environment here !!!!!!!!!!!!!!!!!!!
            }
        });

        // On mouse entered
        setOnMouseEntered(this::updateMouse);

        // On mouse exited
        setOnMouseExited(this::updateMouse);

        // On mouse moved
        setOnMouseMoved(event -> {
            if (mouseMovement != null) {
                mouseMovement.setCurrentPosition(getPoint(event));
            }
            updateMouse(event);
        });

        // On mouse pressed
        setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY && (status == MOVING || status == SELECTING)) {
                isDraggingMouse = true;
                originPoint = Optional.of(getPoint(event));
                endingPoint = Optional.of(getPoint(event));
                repaintEnvironment(null); // TODO implement a way to have access to Environment here !!!!!!!!!!!!!!!!!!!
            }
        });

        // On mouse released
        setOnMouseReleased(event -> {
            if (event.getButton() == MouseButton.PRIMARY && isDraggingMouse) {
                endingPoint = Optional.of(getPoint(event));
                if (status == MOVING && originPoint.isPresent()) {
                    if (currentEnv.getDimensions() == 2) {
                        final Simulation<T> engine = currentEnv.getSimulation();
                        if (engine != null) {
                            final Position envEnding = wormhole.getEnvPoint(endingPoint.orElseThrow(IllegalStateException::new));
                            final Position envOrigin = wormhole.getEnvPoint(originPoint.orElseThrow(IllegalStateException::new));
                            selectedNodes.forEach(n -> {
                                final Position p = currentEnv.getPosition(n);
                                final double finalX = p.getCoordinate(0) + (envEnding.getCoordinate(0) - envOrigin.getCoordinate(0));
                                final double finalY = p.getCoordinate(1) + (envEnding.getCoordinate(1) - envOrigin.getCoordinate(1));
                                final Position finalPos = PointAdapter.from(finalX, finalY).toPosition();
                                engine.schedule(() -> {
                                    currentEnv.moveNodeToPosition(n, finalPos);
                                    update(currentEnv, engine.getTime());
                                });
                            });
                        } else {
                            getLogger().warn("Can not handle node movement on a finished simulation."); // TODO: display proper error message
                        }
                    } else {
                        throw new IllegalStateException("Unable to move nodes: unsupported environment dimension.");
                    }
                    selectedNodes.clear();
                    resetStatus();
                }
                isDraggingMouse = false;
                originPoint = Optional.empty();
                endingPoint = Optional.empty();
                repaintEnvironment(null); // TODO implement a way to have access to Environment here !!!!!!!!!!!!!!!!!!!
            }
        });

        // On wheel moved
        setOnScroll(event -> {
            if (wormhole != null && zoomManager != null) {
                zoomManager.dec(-event.getDeltaY());
                wormhole.zoomOnPoint(getPoint(event.getX(), event.getY()), zoomManager.getZoom());
                updateMouse(event.getX(), event.getY());
            }
        });
    }

    /**
     * The method updates the mouse status.
     *
     * @param event the mouse event that wants to update the mouse status
     * @see #updateMouse(double, double)
     */
    private void updateMouse(final MouseEvent event) {
        updateMouse(event.getX(), event.getY());
    }

    /**
     * The method updates the mouse status.
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     */
    private void updateMouse(final double x, final double y) {
        setDist(x, y);
        if (isMarkCloserNode()) {
            repaintEnvironment(null); // TODO implement a way to have access to Environment here !!!!!!!!!!!!!!!!!!!!!!!
        }
    }

    /**
     * Gets the view center point.
     *
     * @return the center of the view
     */
    private Point getCenter() {
        return getPoint(getWidth() / 2, getHeight() / 2);
    }

    /**
     * The method checks if it's not automatically marking the closer node and it's not in view-only mode.
     *
     * @return true if it's not automatically marking the closer node and it's not in view-only mode
     */
    private boolean isInteracting() {
        return status != MARK_CLOSER && status != ViewStatus.VIEW_ONLY;
    }

    /**
     * Reset the the status of the view.
     */
    private void resetStatus() {
        if (isPreviousStateMarking) {
            this.status = MARK_CLOSER;
        } else {
            this.status = ViewStatus.VIEW_ONLY;
        }
    }

    /**
     * The method updates the representation of the simulation.
     *
     * @param environment the simulated {@code Environment}
     * @param time        the current time
     * @throws IllegalStateException if the method is not called by the simulation {@link Thread}
     */
    private void update(final Environment<T> environment, final Time time) throws IllegalStateException {
        if (Thread.holdsLock(environment)) {
            if (envHasMobileObstacles(environment)) {
                loadObstacles(environment);
            }
            lastTime = time.toDouble();
            currentEnv = environment;
            accessData();
            positions.clear();
            neighbors.clear();
            environment.getNodes().parallelStream().forEach(node -> {
                positions.put(node, environment.getPosition(node));
                try {
                    neighbors.put(node, environment.getNeighborhood(node).clone());
                } catch (final CloneNotSupportedException e) {
                    getLogger().error("Unable to clone neighborhood for " + node, e);
                }
            });
            releaseData();
            repaintEnvironment(environment); // TODO implement a better way to have access to Environment
        } else {
            throw new IllegalStateException("Only the simulation thread can dictate GUI updates");
        }
    }

    /**
     * The method releases the mutex on the data.
     */
    private void releaseData() {
        mapConsistencyMutex.release();
    }

    /**
     * The method requests the mutex on the data.
     */
    private void accessData() {
        mapConsistencyMutex.acquireUninterruptibly();
    }

    /**
     * The method loads the {@link Obstacle2D obstacles} of the given {@code Environment}.
     *
     * @param environment the {@code Environment} from where to load the obstacles
     * @throws IllegalArgumentException if the specified {@link Environment} is not instance of {@link Environment2DWithObstacles}
     * @see Environment2DWithObstacles#getObstacles()
     */
    private void loadObstacles(final Environment<T> environment) {
        try {
            obstacles = ((Environment2DWithObstacles<?, ?>) environment).getObstacles();
        } catch (final ClassCastException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Sets a new {@link Tooltip} on this component with nearest node from the mouse position.
     *
     * @param x x coordinate of the mouse
     * @param y y coordinate of the mouse
     */
    private void setDist(final double x, final double y) {
        if (wormhole != null) {
            mouseX = x;
            mouseY = y;
            final Position envMouse = wormhole.getEnvPoint(getPoint(mouseX, mouseY));
            final StringBuilder sb = new StringBuilder();
            sb.append(envMouse);
            if (nearest != null) {
                sb.append(" -- ");
                sb.append(ResourceLoader.getStringRes("nearest_node_is"));
                sb.append(": ");
                sb.append(nearest.getId());
            }
            final Tooltip tooltip = new Tooltip(sb.toString());
            Tooltip.install(this, tooltip);
        }
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
    public boolean isMarkCloserNode() {
        return status == MARK_CLOSER;
    }

    @Override
    public void setMarkCloserNode(final boolean mark) {
        if (!isInteracting()) {
            status = mark ? MARK_CLOSER : ViewStatus.VIEW_ONLY;
            isPreviousStateMarking = mark;
            repaintEnvironment(null); // TODO implement a way to have access to Environment here !!!!!!!!!!!!!!!!!!!!!!!
        }
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
    public void repaintEnvironment(final Environment<T> environment) {
        final GraphicsContext g = getGraphicsContext2D();
        g.clearRect(0, 0, getWidth(), getHeight());
        drawBackground(g);
        drawEnvOnView(environment, g);
    }

    /**
     * This method is meant to be overridden by subclasses that want to display
     * a more sophisticated background than a simple color.
     *
     * @param g the {@link GraphicsContext} to draw in
     * @see #repaintEnvironment(Environment)
     */
    protected void drawBackground(@SuppressWarnings("unused") final GraphicsContext g) {
        // By default, it does nothing
    }

    /**
     * Actually draws the environment on the view.
     *
     * @param g the {@link GraphicsContext} to draw in
     * @see #repaintEnvironment(Environment)
     */
    protected void drawEnvOnView(final Environment<T> environment, final GraphicsContext g) {
        if (wormhole == null || !isVisible() || isDisabled()) {
            return;
        }

        accessData();

        hooked.ifPresent(h -> {
            final Position hcoor = positions.get(h);
            final Point hp = wormhole.getViewPoint(hcoor);
            if (hp.distance(getCenter()) > FREEDOM_RADIUS) {
                wormhole.setViewPosition(hp);
            }
        });

        /*
         * Compute nodes in sight and their screen position
         */
        final Map<Node<T>, Point> onView = positions.entrySet()
                .parallelStream()
                .map(pair -> new Pair<>(pair.getKey(), wormhole.getViewPoint(pair.getValue())))
                .filter(p -> wormhole.isInsideView(p.getSecond()))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        g.setFill(Color.BLACK);

        if (obstacles != null) {
            obstacles.parallelStream()
                    .map(this::convertObstacle)
                    .forEachOrdered(shape -> {
                        g.setFill(shape.getFill());
                        g.fill();
                    });
        }

        // TODO better use an EffectFX to draw links
//        if (paintLinks) {
//            g.setFill(Color.GRAY);
//            onView.keySet().parallelStream()
//                    .map(neighbors::get)
//                    .flatMap(neigh -> neigh.getNeighbors()
//                            .parallelStream()
//                            .map(node -> node.compareTo(neigh.getCenter()) > 0
//                                    ? new Pair<>(neigh.getCenter(), node)
//                                    : new Pair<>(node, neigh.getCenter())))
//                    .distinct()
//                    .map(pair -> mapPair(pair, node ->
//                            Optional.ofNullable(onView.get(node))
//                                    .orElse(wormhole.getViewPoint(positions.get(node)))))
//                    .forEachOrdered(line -> {
//                        final Point p1 = line.getFirst();
//                        final Point p2 = line.getSecond();
//                        g.strokeLine(p1.x, p1.y, p2.x, p2.y);
//                    });
//        }

        releaseData();

        if (isDraggingMouse && status == MOVING && originPoint.isPresent() && endingPoint.isPresent()) {
            selectedNodes.forEach(n -> {
                if (onView.containsKey(n)) {
                    onView.put(n, new Point(onView.get(n).x + (endingPoint.get().x - originPoint.get().x),
                            onView.get(n).y + (endingPoint.get().y - originPoint.get().y)));
                }
            });
        }

        g.setFill(Color.GREEN);

        // TODO implement a way to draw only Effects for visible Nodes, or remove onView variable
        Optional.ofNullable(effectStack)
                .ifPresent(stack ->
                        stack.forEach(effectGroup
                                -> effectGroup.forEach(effectFX
                                -> effectFX.apply(g, environment, getWormhole()))));

        if (isMarkCloserNode()) {
            final Optional<Map.Entry<Node<T>, Point>> closest = onView.entrySet()
                    .parallelStream()
                    .min((pair1, pair2) -> {
                        final Point p1 = pair1.getValue();
                        final Point p2 = pair2.getValue();
                        final double d1 = Math.hypot(p1.x - mouseX, p1.y - mouseY);
                        final double d2 = Math.hypot(p2.x - mouseX, p2.y - mouseY);
                        return Double.compare(d1, d2);
                    });

            closest.ifPresent(c -> {
                nearest = c.getKey();
                final int nearestX = c.getValue().x;
                final int nearestY = c.getValue().y;
                drawFriedEgg(g, nearestX, nearestY, Color.RED, Color.YELLOW);
            });
        } else {
            nearest = null;
        }

        if (isDraggingMouse && status == SELECTING && originPoint.isPresent() && endingPoint.isPresent()) {
            g.setFill(Color.BLACK);
            final int x = originPoint.get().x < endingPoint.get().x ? originPoint.get().x : endingPoint.get().x;
            final int y = originPoint.get().y < endingPoint.get().y ? originPoint.get().y : endingPoint.get().y;
            final int width = Math.abs(endingPoint.get().x - originPoint.get().x);
            final int height = Math.abs(endingPoint.get().y - originPoint.get().y);
            g.fillRect(x, y, width, height);
            selectedNodes = onView.entrySet().parallelStream()
                    .filter(nodes -> isInsideRectangle(nodes.getValue(), x, y, width, height))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());
        }
        selectedNodes.parallelStream()
                .map(e -> Optional.ofNullable(onView.get(e)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEachOrdered(p -> drawFriedEgg(g, p.x, p.y, Color.BLUE, Color.CYAN));
    }

    /**
     * Converts an {@code Obstacle2D} in a JavaFX {@link Shape}.
     *
     * @param obstacle the obstacle to convert
     * @return the {@code Shape} as a {@link Path}
     */
    private Shape convertObstacle(final Obstacle2D obstacle) {
        final Rectangle2D bounds = obstacle.getBounds2D();
        final Rectangle rectangle = new Rectangle(bounds.getWidth(), bounds.getHeight());
        final double rX = rectangle.getX();
        final double rY = rectangle.getY();
        final double rWidth = rectangle.getWidth();
        final double rHeight = rectangle.getHeight();

        final Position[] points = new Position[]{
                new Continuous2DEuclidean(rX, rY),
                new Continuous2DEuclidean(rX + rWidth, rY),
                new Continuous2DEuclidean(rX + rWidth, rY + rHeight),
                new Continuous2DEuclidean(rX, rY + rHeight)};

        final Path path = new Path();

        for (int i = 0; i < points.length; i++) {
            final Point pt = wormhole.getViewPoint(points[i]);
            final PathElement to;
            if (i == 0) {
                to = new MoveTo(pt.getX(), pt.getY());
            } else {
                to = new LineTo(pt.getX(), pt.getY());
            }
            path.getElements().add(to);
        }

        path.getElements().add(new ClosePath());

        return path;
    }

    private void drawFriedEgg(final GraphicsContext g, final int x, final int y, final Color c1, final Color c2) {
        g.setFill(c1);
        g.fillOval(x - SELECTED_NODE_DRAWING_SIZE / 2, y - SELECTED_NODE_DRAWING_SIZE / 2, SELECTED_NODE_DRAWING_SIZE, SELECTED_NODE_DRAWING_SIZE);
        g.setFill(c2);
        g.fillOval(x - SELECTED_NODE_INTERNAL_SIZE / 2, y - SELECTED_NODE_INTERNAL_SIZE / 2, SELECTED_NODE_INTERNAL_SIZE, SELECTED_NODE_INTERNAL_SIZE);
    }

    @Override
    public void setEffects(final Collection<EffectGroup> effects) {
        this.effectStack = effects;
    }

    @Override
    public void finished(final Environment<T> environment, final Time time, final long step) {
        update(environment, time);
        firstTime = true;
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
                    initAll(environment);
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
                        getLogger().warn("Spurious wakeup"); // TODO load from ResourceBundle with ResourceLoader
                    }
                }
            }
            update(environment, time);
        }
    }

    /**
     * Initializes all the internal data.
     */
    private void initAll(final Environment<T> environment) {
        wormhole = new Wormhole2D(environment, this);
        wormhole.center();
        wormhole.optimalZoom();
        angleManager = new AngleManagerImpl(AngleManagerImpl.DEF_DEG_PER_PIXEL);
        zoomManager = new ExponentialZoomManager(wormhole.getZoom(), ExponentialZoomManager.DEF_BASE);
        if (environment instanceof Environment2DWithObstacles) {
            loadObstacles(environment);
        } else {
            obstacles = null;
        }
    }

    /**
     * Getter method for the {@link Logger} of this class.
     *
     * @return the {@code Logger}
     */
    protected abstract Logger getLogger();

    protected BidimensionalWormhole getWormhole() {
        return this.wormhole;
    }

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
