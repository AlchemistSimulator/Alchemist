/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.swingui.monitor.impl;

import com.google.common.collect.ImmutableList;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.boundary.swingui.api.Graphical2DOutputMonitor;
import it.unibo.alchemist.boundary.swingui.effect.api.Effect;
import it.unibo.alchemist.boundary.swingui.impl.LocalizedResourceBundle;
import it.unibo.alchemist.boundary.ui.api.PointerSpeed;
import it.unibo.alchemist.boundary.ui.api.Wormhole2D;
import it.unibo.alchemist.boundary.ui.api.Wormhole2D.Mode;
import it.unibo.alchemist.boundary.ui.api.ZoomManager;
import it.unibo.alchemist.boundary.ui.impl.ExponentialZoomManager;
import it.unibo.alchemist.boundary.ui.impl.PointerSpeedImpl;
import it.unibo.alchemist.boundary.wormhole.impl.AngleManagerImpl;
import it.unibo.alchemist.boundary.wormhole.impl.WormholeSwing;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.core.interfaces.Status;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Actionable;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Obstacle2D;
import it.unibo.alchemist.model.interfaces.Position2D;
import it.unibo.alchemist.model.interfaces.Time;
import it.unibo.alchemist.model.interfaces.environments.Environment2DWithObstacles;
import org.apache.commons.math3.util.Pair;
import org.danilopianini.lang.LangUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static it.unibo.alchemist.boundary.ui.impl.PointAdapter.from;

/**
 * Base-class for each display able a graphically represent a 2D space
 * and simulation.
 * 
 * @param <T> Concentration type
 * @param <P> {@link Position2D} type
 */
@Deprecated
@SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "This class is not meant to get serialized")
public class Generic2DDisplay<T, P extends Position2D<P>> extends JPanel implements Graphical2DOutputMonitor<T, P> {

    /**
     * The default frame rate.
     */
    public static final byte DEFAULT_FRAME_RATE = 25;
    private static final String OPENGL = "sun.java2d.opengl";
    private static final double TIME_STEP = 1d / DEFAULT_FRAME_RATE;
    private static final double FREEDOM_RADIUS = 1d;
    private static final Logger L = LoggerFactory.getLogger(Generic2DDisplay.class);
    private static final int MS_PER_SECOND = 1000;
    /**
     *
     */
    private static final long PAUSE_DETECTION_THRESHOLD = 200;

    static {
        /*
         * Enable OpenGL unless it's explicitly set otherwise by the user
         */
        if (System.getProperty(OPENGL) == null) {
            System.setProperty(OPENGL, "true");
        }
    }

    /**
     * How big (in pixels) the selected node should appear.
     */
    protected static final byte SELECTED_NODE_DRAWING_SIZE = 16, SELECTED_NODE_INTERNAL_SIZE = 10;
    private static final long serialVersionUID = 511631766719686842L;

    private final Semaphore mapConsistencyMutex = new Semaphore(1);
    private final transient PointerSpeed mouseMovement = new PointerSpeedImpl();
    private transient AngleManagerImpl angleManager;
    private Environment<T, P> currentEnv;
    private List<Effect> effectStack;
    private volatile boolean firstTime = true;
    private boolean paintLinks;
    private transient Optional<Node<T>> hooked = Optional.empty();
    private boolean init;
    private double lastTime;
    private int mouseX, mouseY;
    private Node<T> nearest;
    private final ConcurrentMap<Node<T>, Neighborhood<T>> neighbors = new ConcurrentHashMap<>();
    private List<? extends Obstacle2D<?>> obstacles;
    private final ConcurrentMap<Node<T>, P> positions = new ConcurrentHashMap<>();
    private boolean realTime;
    private int st;
    private long timeInit = System.currentTimeMillis();
    private transient Wormhole2D<P> wormhole;
    private transient ZoomManager zoomManager;
    private boolean isPreviousStateMarking = true;
    private ViewStatus status = ViewStatus.VIEW_WITH_MARKER;
    private boolean isDraggingMouse;
    private @Nullable Point originPoint;
    private @Nullable Point endingPoint;
    private Set<Node<T>> selectedNodes = new HashSet<>();

    /**
     * Initializes a new display without redrawing the first step.
     */
    public Generic2DDisplay() {
        this(1);
    }

    /**
     * Initializes a new display.
     *
     * @param step number of steps to let pass without re-drawing
     */
    @SuppressFBWarnings("MC_OVERRIDABLE_METHOD_CALL_IN_CONSTRUCTOR")
    public Generic2DDisplay(final int step) {
        super();
        if (!"true".equals(System.getProperty(OPENGL))) {
            L.warn(
                "OpenGL acceleration appears to be disabled on this system. "
                    + "This may impact performance negatively. "
                    + "Please enable it with -D" + OPENGL + "=true"
            );
        }
        setStep(step);
        setBackground(Color.WHITE);
        init = false;
        final MouseManager mgr = new MouseManager();
        addMouseListener(mgr);
        addMouseMotionListener(mgr);
        addMouseWheelListener(mgr);
        bindKeys();
    }

    private boolean isNotInteracting() {
        return status == ViewStatus.VIEW_WITH_MARKER || status == ViewStatus.VIEW_ONLY;
    }

    private static <I, O> Pair<O, O> mapPair(
            final Pair<? extends I, ? extends I> pair,
            final Function<? super I, ? extends O> converter) {
        return new Pair<>(converter.apply(pair.getFirst()), converter.apply(pair.getSecond()));
    }

    /**
     * Builds a frame. After building a {@link JFrame}, it performs the given operation on it, if any.
     *
     * @param title       the title of the frame
     * @param content     the content of the frame
     * @param frameEditor the operation to perform on the built {@code JFrame}
     */
    protected static void makeFrame(final String title, final JPanel content, final @Nullable Consumer<Object> frameEditor) {
        final JFrame frame = new JFrame(title);
        frame.getContentPane().add(content);
        frame.setLocationByPlatform(true);
        frame.pack();
        frame.setVisible(true);
        if (frameEditor != null) {
            frameEditor.accept(frame);
        }
    }

    /**
     * Reset the status of the view.
     */
    private void resetStatus() {
        if (isPreviousStateMarking) {
            status = ViewStatus.VIEW_WITH_MARKER;
        } else {
            status = ViewStatus.VIEW_ONLY;
        }
    }

    /**
     * The method binds {@link KeyEvent}s to specific actions.
     */
    private void bindKeys() {
        bindKey(KeyEvent.VK_S, () -> {
            if (status == ViewStatus.SELECTING_NODES) {
                resetStatus();
                selectedNodes.clear();
            } else if (isNotInteracting()) {
                status = ViewStatus.SELECTING_NODES;
            }
            repaint();
        });
        bindKey(KeyEvent.VK_O, () -> {
            if (status == ViewStatus.SELECTING_NODES) {
                status = ViewStatus.MOVING_SELECTED_NODES;
            }
        });
        bindKey(KeyEvent.VK_C, () -> {
            if (status == ViewStatus.SELECTING_NODES) {
                status = ViewStatus.CLONING_NODES;
            }
        });
        bindKey(KeyEvent.VK_E, () -> {
            if (status == ViewStatus.SELECTING_NODES) {
                status = ViewStatus.EDITING_NODES_CONTENT;
                final JFrame mol = Generic2DDisplay.makeFrame("Moleculing", new MoleculeInjectorGUI<>(selectedNodes));
                mol.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                mol.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(final WindowEvent e) {
                        selectedNodes.clear();
                        resetStatus();
                    }
                });

            }
        });
        bindKey(KeyEvent.VK_D, () -> {
            if (status == ViewStatus.SELECTING_NODES) {
                status = ViewStatus.DELETING_NODES;
                final Simulation<T, P> sim = currentEnv.getSimulation();
                for (final Node<T> n : selectedNodes) {
                    sim.schedule(() -> currentEnv.removeNode(n));
                }
                sim.schedule(() -> update(currentEnv, sim.getTime()));
                resetStatus();
            }
        });
        bindKey(KeyEvent.VK_M, () -> setMarkCloserNode(!isCloserNodeMarked()));
        bindKey(KeyEvent.VK_L, () -> setDrawLinks(!paintLinks));
        bindKey(KeyEvent.VK_P, () -> Optional.ofNullable(currentEnv.getSimulation())
                .ifPresent(sim -> {
                    if (sim.getStatus() == Status.RUNNING) {
                        sim.pause();
                    } else {
                        sim.play();
                    }
                }));
        bindKey(KeyEvent.VK_R, () -> setRealTime(!isRealTime()));
        bindKey(KeyEvent.VK_LEFT, () -> setStep(Math.max(1, st - Math.max(st / 10, 1))));
        bindKey(KeyEvent.VK_RIGHT, () -> setStep(Math.max(st, st + Math.max(st / 10, 1))));
    }

    private void accessData() {
        mapConsistencyMutex.acquireUninterruptibly();
    }

    private Shape convertObstacle(final Obstacle2D<?> o) {
        final Rectangle2D r = o.getBounds2D();
        final List<P> points = ImmutableList.of(
                currentEnv.makePosition(r.getX(), r.getY()),
                currentEnv.makePosition(r.getX() + r.getWidth(), r.getY()),
                currentEnv.makePosition(r.getX() + r.getWidth(), r.getY() + r.getHeight()),
                currentEnv.makePosition(r.getX(), r.getY() + r.getHeight())
        );
        final Path2D path = new GeneralPath();
        for (int i = 0; i < points.size(); i++) {
            final Point pt = wormhole.getViewPoint(points.get(i));
            if (i == 0) {
                path.moveTo(pt.getX(), pt.getY());
            }
            path.lineTo(pt.getX(), pt.getY());
        }
        path.closePath();
        return path;
    }

    /**
     * This method is meant to be overridden by subclasses that want to display
     * a more sophisticated background than a simple color.
     *
     * @param g the Graphics2D to use
     */
    protected void drawBackground(final Graphics2D g) {
    }

    /**
     * Actually draws the environment on the view.
     *
     * @param g {@link Graphics2D} object responsible for drawing
     */
    protected void drawEnvOnView(final Graphics2D g) {
        if (wormhole == null || !isVisible() || !isEnabled()) {
            return;
        }
        accessData();
        if (hooked.isPresent()) {
            final P hcoor = positions.get(hooked.get());
            final Point hp = wormhole.getViewPoint(hcoor);
            if (hp.distance(getCenter()) > FREEDOM_RADIUS) {
                wormhole.setViewPosition(hp);
            }
        }
        /*
         * Compute nodes in sight and their screen position
         */
        final Map<Node<T>, Point> onView = positions.entrySet().parallelStream()
                .map(pair -> new Pair<>(pair.getKey(), wormhole.getViewPoint(pair.getValue())))
                .filter(p -> wormhole.isInsideView(p.getSecond()))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        g.setColor(Color.BLACK);
        if (obstacles != null) {
            /*
             * TODO: only draw obstacles if on view
             */
            obstacles.parallelStream()
                    .map(this::convertObstacle)
                    .forEachOrdered(g::fill);
        }
        if (paintLinks) {
            g.setColor(Color.GRAY);
            onView.keySet().parallelStream()
                .map(neighbors::get)
                .flatMap(neigh ->
                    neigh.getNeighbors().parallelStream()
                        .map(node ->
                            node.compareTo(neigh.getCenter()) > 0
                                ? new Pair<>(neigh.getCenter(), node)
                                : new Pair<>(node, neigh.getCenter())
                        )
                )
                .distinct()
                .map(pair ->
                    mapPair(
                        pair,
                        node -> Optional
                            .ofNullable(onView.get(node))
                            .orElse(wormhole.getViewPoint(positions.get(node)))
                    )
                )
                .forEachOrdered(line -> {
                    final Point p1 = line.getFirst();
                    final Point p2 = line.getSecond();
                    g.drawLine(p1.x, p1.y, p2.x, p2.y);
                });
        }
        releaseData();
        if (
            isDraggingMouse
                && status == ViewStatus.MOVING_SELECTED_NODES
                && originPoint != null
                && endingPoint != null
        ) {
            for (final Node<T> n : selectedNodes) {
                if (onView.containsKey(n)) {
                    onView.put(n, new Point(onView.get(n).x + (endingPoint.x - originPoint.x),
                            onView.get(n).y + (endingPoint.y - originPoint.y)));
                }
            }
        }
        g.setColor(Color.GREEN);
        if (effectStack != null) {
            effectStack.forEach(effect -> onView.forEach((node, point) ->
                effect.apply(g, node, currentEnv, wormhole)));
        }
        if (isCloserNodeMarked()) {
            final Optional<Map.Entry<Node<T>, Point>> closest = onView.entrySet().parallelStream()
                    .min((pair1, pair2) -> {
                        final Point p1 = pair1.getValue();
                        final Point p2 = pair2.getValue();
                        final double d1 = Math.hypot(p1.x - mouseX, p1.y - mouseY);
                        final double d2 = Math.hypot(p2.x - mouseX, p2.y - mouseY);
                        return Double.compare(d1, d2);
                    });
            if (closest.isPresent()) {
                nearest = closest.get().getKey();
                final int nearestX = closest.get().getValue().x;
                final int nearestY = closest.get().getValue().y;
                drawFriedEgg(g, nearestX, nearestY, Color.RED, Color.YELLOW);
            }
        } else {
            nearest = null;
        }
        if (isDraggingMouse && status == ViewStatus.SELECTING_NODES && originPoint != null && endingPoint != null) {
            g.setColor(Color.BLACK);
            final int x = Math.min(originPoint.x, endingPoint.x);
            final int y = Math.min(originPoint.y, endingPoint.y);
            final int width = Math.abs(endingPoint.x - originPoint.x);
            final int height = Math.abs(endingPoint.y - originPoint.y);
            g.drawRect(x, y, width, height);
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

    private void drawFriedEgg(final Graphics g, final int x, final int y, final Color c1, final Color c2) {
        g.setColor(c1);
        g.fillOval(
                x - SELECTED_NODE_DRAWING_SIZE / 2,
                y - SELECTED_NODE_DRAWING_SIZE / 2,
                SELECTED_NODE_DRAWING_SIZE,
                SELECTED_NODE_DRAWING_SIZE
        );
        g.setColor(c2);
        g.fillOval(
                x - SELECTED_NODE_INTERNAL_SIZE / 2,
                y - SELECTED_NODE_INTERNAL_SIZE / 2,
                SELECTED_NODE_INTERNAL_SIZE,
                SELECTED_NODE_INTERNAL_SIZE
        );
    }

    /**
     * Override to change or add operations to be done after a simulation is concluded.
     *
     * @param environment the {@link Environment}
     * @param time
     *            The time at which the simulation ended
     * @param step the step at which the simulation ended
     */
    @Override
    public void finished(@Nonnull final Environment<T, P> environment, @Nonnull final Time time, final long step) {
        update(environment, time);
        firstTime = true;
    }

    /**
     * Gets the view center point.
     *
     * @return the center
     */
    private Point getCenter() {
        return new Point(getWidth() / 2, getHeight() / 2);
    }

    @Override
    public final int getStep() {
        return st;
    }

    /**
     * Lets child-classes access the wormhole.
     *
     * @return an {@link Wormhole2D}
     */
    protected final Wormhole2D<P> getWormhole() {
        return wormhole;
    }

    /**
     * Lets child-classes access the zoom manager.
     *
     * @return an {@link ZoomManager}
     */
    protected final ZoomManager getZoomManager() {
        return zoomManager;
    }

    private void initAll(final Environment<T, P> environment) {
        wormhole = new WormholeSwing<>(environment, this);
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
     *  Defines what to do when the UI is initialized.
     *
     * @param environment the {@link Environment}
     */
    @Override
    public void initialized(@Nonnull final Environment<T, P> environment) {
        stepDone(environment, null, new DoubleTime(), 0);
    }

    /**
     * The method checks if the closer node is marked.
     *
     * @return true if the closer node is marked
     */
    protected final boolean isCloserNodeMarked() {
        return status == ViewStatus.VIEW_WITH_MARKER;
    }

    /**
     * Lets child-classes check if the display is initialized.
     *
     * @return a <code>boolean</code> value
     */
    protected boolean isInitialized() {
        return init;
    }

    /**
     * @return true if this monitor is trying to draw in realtime
     */
    @Override
    public final boolean isRealTime() {
        return realTime;
    }

    private void loadObstacles(final Environment<T, P> environment) {
        obstacles = ((Environment2DWithObstacles<?, ?>) environment).getObstacles();
    }

    /**
     * Override as per {@link javax.swing.JComponent#paintComponent(Graphics)}.
     *
     * @param g the {@link Graphics} in use
     */
    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        drawBackground((Graphics2D) g);
        drawEnvOnView((Graphics2D) g);
    }

    private void releaseData() {
        mapConsistencyMutex.release();
    }

    @Override
    public final void repaint() {
        super.repaint();
    }

    /**
     * Updates {@link #setToolTipText(String) tooltip} of this component with the nearest node from the mouse position.
     *
     * @param x x coordinate of the mouse
     * @param y y coordinate of the mouse
     */
    protected void setMouseTooltipTo(final int x, final int y) {
        if (wormhole != null) {
            mouseX = x;
            mouseY = y;
            final P envMouse = wormhole.getEnvPoint(new Point(mouseX, mouseY));
            final StringBuilder sb = new StringBuilder();
            sb.append(envMouse);
            if (nearest != null) {
                sb.append(" -- ");
                sb.append(LocalizedResourceBundle.getString("nearest_node_is"));
                sb.append(": ");
                sb.append(nearest.getId());
            }
            setToolTipText(sb.toString());
        }
    }

    @Override
    public final void setDrawLinks(final boolean b) {
        if (paintLinks != b) {
            paintLinks = b;
            repaint();
        }
    }

    @Override
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "This is intentional")
    public final void setEffectStack(final List<Effect> l) {
        effectStack = l;
    }

    @Override
    public final void setMarkCloserNode(final boolean mark) {
        if (isNotInteracting()) {
            if (mark) {
                isPreviousStateMarking = true;
                status = ViewStatus.VIEW_WITH_MARKER;
            } else {
                isPreviousStateMarking = false;
                status = ViewStatus.VIEW_ONLY;
            }
            repaint();
        }
    }

    @Override
    public final void setRealTime(final boolean rt) {
        realTime = rt;
    }

    @Override
    public final void setStep(final int step) {
        if (step <= 0) {
            throw new IllegalArgumentException("The parameter must be a positive integer");
        }
        st = step;
    }

    /**
     * Lets child-classes change the wormhole.
     *
     * @param w an {@link Wormhole2D}
     */
    protected void setWormhole(final Wormhole2D<P> w) {
        Objects.requireNonNull(w);
        wormhole = w;
    }

    /**
     * Lets child-classes change the zoom manager.
     * 
     * @param zm
     *            an {@link ZoomManager}
     */
    protected void setZoomManager(final ZoomManager zm) {
        zoomManager = zm;
        wormhole.setZoom(zoomManager.getZoom());
    }

    @Override
    public final void stepDone(
            @Nonnull final Environment<T, P> environment,
            final Actionable<T> reaction,
            @Nonnull final Time time,
            final long step
    ) {
        if (firstTime) {
            synchronized (this) {
                if (firstTime) {
                    initAll(environment);
                    lastTime = -TIME_STEP;
                    firstTime = false;
                    timeInit = System.currentTimeMillis();
                    update(environment, time);
                }
            }
        } else if (st < 1 || step % st == 0) {
            if (isRealTime()) {
                if (lastTime + TIME_STEP > time.toDouble()) {
                    return;
                }
                final long timeSimulated = (long) (time.toDouble() * MS_PER_SECOND);
                if (timeSimulated == 0) {
                    timeInit = System.currentTimeMillis();
                }
                final long timePassed = System.currentTimeMillis() - timeInit;
                if (timePassed - timeSimulated > PAUSE_DETECTION_THRESHOLD) {
                    timeInit = timeInit + timePassed - timeSimulated;
                }
                if (timeSimulated > timePassed) {
                    try {
                        Thread.sleep(Math.min(timeSimulated - timePassed, MS_PER_SECOND / DEFAULT_FRAME_RATE));
                    } catch (final InterruptedException e) {
                        L.warn("Damn spurious wakeups.");
                    }
                }
            }
            update(environment, time);
        }
    }

    /**
     * Updates parameter for correct {@code Environment} representation.
     *
     * @param environment  the {@code Environment}
     * @param time the current {@code Time} of simulation
     */
    private void update(final Environment<T, P> environment, final Time time) {
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
                neighbors.put(node, environment.getNeighborhood(node));
            });
            releaseData();
            repaint();
        } else {
            throw new IllegalStateException("Only the simulation thread can dictate GUI updates");
        }
    }

    @Override
    public final void zoomTo(final P center, final double zoomLevel) {
        assert center.getDimensions() == 2;
        wormhole.zoomOnPoint(wormhole.getViewPoint(center), zoomLevel);
    }

    /**
     * @param environment
     *            the current environment
     * @return true if env is subclass of {@link Environment2DWithObstacles}
     *         and has mobile obstacles
     */
    protected static boolean envHasMobileObstacles(final Environment<?, ?> environment) {
        return environment instanceof Environment2DWithObstacles && ((Environment2DWithObstacles<?, ?>) environment)
                .hasMobileObstacles();
    }

    /**
     * Custom listener for {@link MouseEvent}s.
     */
    protected final class MouseManager implements MouseInputListener, MouseWheelListener, MouseMotionListener {
        /**
         * @param e the {@link MouseEvent}
         */
        @Override
        public void mouseClicked(final MouseEvent e) {
            setMouseTooltipTo(e.getX(), e.getY());
            if (isCloserNodeMarked() && nearest != null && SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                final NodeTracker<T, P> monitor = new NodeTracker<>(nearest);
                monitor.stepDone(currentEnv, null, new DoubleTime(lastTime), st);
                final Simulation<T, P> sim = currentEnv.getSimulation();
                final JFrame frame = makeFrame("Tracker for node " + nearest.getId(), monitor);
                if (sim != null) {
                    sim.addOutputMonitor(monitor);
                    frame.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(final WindowEvent e) {
                            sim.removeOutputMonitor(monitor);
                        }
                    });
                }
            } else if (status == ViewStatus.CLONING_NODES && SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
                final Simulation<T, P> engine = currentEnv.getSimulation();
                final P envEnding = wormhole.getEnvPoint(e.getPoint());
                engine.schedule(() -> {
                    final Collection<Node<T>> newNodes = new ArrayList<>(selectedNodes.size());
                    try {
                        for (final Node<T> n : selectedNodes) {
                            newNodes.add(n.cloneNode(engine.getTime()));
                        }
                        for (final Node<T> n : newNodes) {
                            currentEnv.addNode(n, envEnding);
                        }
                        update(currentEnv, engine.getTime());
                    } catch (final RuntimeException exp) { // NOPMD
                        final String title = "Node cloning error";
                        final String message = "One or more of your nodes do not support cloning, the debug information is:\n"
                                + LangUtils.stackTraceToString(exp);
                        JOptionPane.showMessageDialog(Generic2DDisplay.this, message, title, JOptionPane.ERROR_MESSAGE);
                    }
                });
                selectedNodes.clear();
                resetStatus();
            }
            if (nearest != null && SwingUtilities.isMiddleMouseButton(e)) {
                hooked = hooked.isPresent() ? Optional.empty() : Optional.of(nearest);
            }
            repaint();
        }

        /**
         * @param e the {@link MouseEvent}
         */
        @Override
        public void mouseDragged(final MouseEvent e) {
            setMouseTooltipTo(e.getX(), e.getY());
            if (wormhole == null || mouseMovement == null) {
                return;
            }
            if (SwingUtilities.isLeftMouseButton(e)) {
                if (isDraggingMouse) {
                    endingPoint = e.getPoint();
                }
                if (hooked.isEmpty() && isNotInteracting()) {
                    final Point previous = wormhole.getViewPosition();
                    wormhole.setViewPosition(
                            from(previous)
                                    .sum(from(mouseMovement.getVariation())).toPoint());
                }
            } else if (SwingUtilities.isRightMouseButton(e) && angleManager != null && wormhole.getMode() != Mode.MAP) {
                angleManager.inc(mouseMovement.getVariation().getX());
                wormhole.rotateAroundPoint(getCenter(), angleManager.getAngle());
            }
            mouseMovement.setCurrentPosition(e.getPoint());
            repaint();
        }

        @Override
        public void mouseEntered(final MouseEvent e) {
            updateMouse(e);
        }

        @Override
        public void mouseExited(final MouseEvent e) {
            updateMouse(e);
        }

        @Override
        public void mouseMoved(final MouseEvent e) {
            if (mouseMovement != null) {
                mouseMovement.setCurrentPosition(e.getPoint());
            }
            updateMouse(e);
        }

        @Override
        public void mousePressed(final MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)
                    && (status == ViewStatus.MOVING_SELECTED_NODES || status == ViewStatus.SELECTING_NODES)
            ) {
                isDraggingMouse = true;
                originPoint = e.getPoint();
                endingPoint = e.getPoint();
                repaint();
            }
        }

        @Override
        public void mouseReleased(final MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e) && isDraggingMouse) {
                endingPoint = e.getPoint();
                if (status == ViewStatus.MOVING_SELECTED_NODES && originPoint != null) {
                    if (currentEnv.getDimensions() == 2) {
                        final Simulation<T, P> engine = currentEnv.getSimulation();
                        if (engine != null) {
                            final P envEnding = wormhole.getEnvPoint(endingPoint);
                            final P envOrigin = wormhole.getEnvPoint(originPoint);
                            for (final Node<T> n : selectedNodes) {
                                final P p = currentEnv.getPosition(n);
                                final P finalPos = p.plus(envEnding.minus(envOrigin.getCoordinates()).getCoordinates());
                                engine.schedule(() -> {
                                    currentEnv.moveNodeToPosition(n, finalPos);
                                    update(currentEnv, engine.getTime());
                                });
                            }
                        } else {
                            // TODO: display proper error message
                            L.warn("Can not handle node movement on a finished simulation.");
                        }
                    } else {
                        throw new IllegalStateException("Unable to move nodes: unsupported environment dimension.");
                    }
                    selectedNodes.clear();
                    resetStatus();
                }
                isDraggingMouse = false;
                originPoint = null;
                endingPoint = null;
                repaint();
            }
        }

        @Override
        public void mouseWheelMoved(final MouseWheelEvent e) {
            if (wormhole != null && zoomManager != null) {
                zoomManager.dec(e.getWheelRotation());
                wormhole.zoomOnPoint(e.getPoint(), zoomManager.getZoom());
                updateMouse(e);
            }
        }

        /**
         * Sets the tooltip of the parent component and marks the nearest node if it's not already marked.
         *
         * @param e the mouse event
         * @see #setMouseTooltipTo(int, int)
         */
        private void updateMouse(final MouseEvent e) {
            setMouseTooltipTo(e.getX(), e.getY());
            if (isCloserNodeMarked()) {
                repaint();
            }
        }

    }

    private static JFrame makeFrame(final String title, final JPanel content) {
        final JFrame frame = new JFrame(title);
        frame.getContentPane().add(content);
        frame.setLocationByPlatform(true);
        frame.pack();
        frame.setVisible(true);
        return frame;
    }

    private void bindKey(final int key, final Runnable fun) {
        final Object binder = "Key: " + key;
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key, 0), binder);
        getActionMap().put(binder, new AbstractAction() {
            private static final long serialVersionUID = 7927420406960259675L;
            @Override
            public void actionPerformed(final ActionEvent e) {
                fun.run();
            }
        });
    }

    private static boolean isInsideRectangle(
            final Point viewPoint,
            final int rx,
            final int ry,
            final int width,
            final int height
    ) {
        final double x = viewPoint.getX();
        final double y = viewPoint.getY();
        return x >= rx && x <= rx + width && y >= ry && y <= ry + height;
    }

    private enum ViewStatus {

        VIEW_ONLY,

        VIEW_WITH_MARKER,

        SELECTING_NODES,

        MOVING_SELECTED_NODES,

        CLONING_NODES,

        DELETING_NODES,

        EDITING_NODES_CONTENT
    }
}
