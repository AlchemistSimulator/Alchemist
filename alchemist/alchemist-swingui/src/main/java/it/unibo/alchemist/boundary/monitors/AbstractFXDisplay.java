package it.unibo.alchemist.boundary.monitors;

import it.unibo.alchemist.boundary.gui.effects.EffectFX;
import it.unibo.alchemist.boundary.gui.utility.ResourceLoader;
import it.unibo.alchemist.boundary.interfaces.FXOutputMonitor;
import it.unibo.alchemist.boundary.wormhole.implementation.AngleManagerImpl;
import it.unibo.alchemist.boundary.wormhole.implementation.PointAdapter;
import it.unibo.alchemist.boundary.wormhole.implementation.PointerSpeedImpl;
import it.unibo.alchemist.boundary.wormhole.interfaces.IWormhole2D;
import it.unibo.alchemist.boundary.wormhole.interfaces.PointerSpeed;
import it.unibo.alchemist.boundary.wormhole.interfaces.ZoomManager;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.danilopianini.lang.LangUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;

import static it.unibo.alchemist.boundary.wormhole.interfaces.IWormhole2D.Mode;

/**
 * Base-class for each display able a graphically represent a 2D space
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
    public static final byte DEFAULT_FRAME_RATE = 25;
    /**
     * The default time per frame.
     */
    public static final /* TODO float */ double TIME_STEP = 1 / DEFAULT_FRAME_RATE;
    /**
     * The default radius of freedom of representation.
     */
    protected static final /* TODO byte */ double FREEDOM_RADIUS = 1;
    /**
     * Default number of steps.
     */
    private static final int DEFAULT_NUMBER_OF_STEPS = 1;
    /**
     * Default status of the view.
     */
    private static final ViewStatus DEFAULT_VIEW_STATUS = ViewStatus.MARK_CLOSER;
    /**
     * Default logger for this class.
     */
    private static final Logger L = LoggerFactory.getLogger(AbstractFXDisplay.class);
    // TODO

    static {
        System.setProperty("sun.java2d.opengl", "true");
    }

    private final ConcurrentMap<Node<T>, Position> positions;
    private final ConcurrentMap<Node<T>, Neighborhood<T>> neighbors;
    private int step;
    private IWormhole2D wormhole;
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

    private void setkeyboardListener() {
        // TODO
    }

    protected void setMouseListener() {

        // On mouse clicked
        setOnMouseClicked(event -> {
            setDist(event.getX(), event.getY());
            if (status == ViewStatus.MARK_CLOSER && nearest != null && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                final NodeTracker<T> monitor = new NodeTracker<>(nearest);
                monitor.stepDone(currentEnv, null, new DoubleTime(lastTime), step);
                final Simulation<T> simulation = currentEnv.getSimulation();
                // TODO open a drawer to the right of MainApp v
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
                // TODO open a drawer to the right of MainApp ^
            } else if (status == ViewStatus.CLONING && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
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
            repaint();
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
                repaint();
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
            if (event.getButton() == MouseButton.PRIMARY && (status == ViewStatus.MOVING || status == ViewStatus.SELECTING)) {
                isDraggingMouse = true;
                originPoint = Optional.of(getPoint(event));
                endingPoint = Optional.of(getPoint(event));
                repaint();
            }
        });

        // On mouse released
        setOnMouseReleased(event -> {
            if (event.getButton() == MouseButton.PRIMARY && isDraggingMouse) {
                endingPoint = Optional.of(getPoint(event));
                if (status == ViewStatus.MOVING && originPoint.isPresent()) {
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
                            L.warn("Can not handle node movement on a finished simulation."); // TODO: display proper error message
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
                repaint();
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
        if (status == ViewStatus.MARK_CLOSER) {
            repaint();
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
        return status != ViewStatus.MARK_CLOSER && status != ViewStatus.VIEW_ONLY;
    }

    /**
     * Reset the the status of the view.
     */
    private void resetStatus() {
        if (isPreviousStateMarking) {
            this.status = ViewStatus.MARK_CLOSER;
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
                    L.error("Unable to clone neighborhood for " + node, e);
                }
            });
            releaseData();
            repaint();
        } else {
            throw new IllegalStateException("Only the simulation thread can dictate GUI updates");
        }
    }

    private void releaseData() {
        mapConsistencyMutex.release();
    }

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
        // TODO
        return 0;
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
        // TODO
        return false;
    }

    @Override
    public void setMarkCloserNode(final boolean mark) {
        // TODO
    }

    @Override
    public boolean isRealTime() {
        // TODO
        return false;
    }

    @Override
    public void setRealTime(final boolean realTime) {
        // TODO
    }

    @Override
    public void repaint() {
        getGraphicsContext2D().clearRect(0, 0, getWidth(), getHeight());
    }

    @Override
    public void setEffectStack(final Collection<EffectFX> effects) {
        // TODO
    }

    @Override
    public void finished(final Environment<T> environment, final Time time, final long step) {
        // TODO
    }

    @Override
    public void initialized(final Environment<T> environment) {
        // TODO
    }

    @Override
    public void stepDone(final Environment<T> environment, final Reaction<T> reaction, final Time time, final long step) {
        // TODO
    }

    private enum ViewStatus {
        VIEW_ONLY,

        MARK_CLOSER,

        SELECTING,

        MOVING,

        CLONING,

        DELETING,

        MOLECULING;
    }
}
