/*
 * Copyright (C) 2010-2015, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.monitors;

import it.unibo.alchemist.boundary.gui.effects.Effect;
import it.unibo.alchemist.boundary.interfaces.Graphical2DOutputMonitor;
import it.unibo.alchemist.boundary.l10n.LocalizedResourceBundle;
import it.unibo.alchemist.boundary.wormhole.implementation.*;
import it.unibo.alchemist.boundary.wormhole.interfaces.Wormhole2D;
import it.unibo.alchemist.boundary.wormhole.interfaces.Wormhole2D.Mode;
import it.unibo.alchemist.boundary.wormhole.interfaces.PointerSpeed;
import it.unibo.alchemist.boundary.wormhole.interfaces.ZoomManager;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.core.interfaces.Status;
import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.*;
import org.apache.commons.math3.util.Pair;
import org.danilopianini.lang.LangUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Base-class for each display able a graphically represent a 2D space
 * and simulation.
 * <p>
 * Tries to use OpenGL acceleration when possible.
 *
 * @param <T> the {@link Concentration} type
 */
public class Generic2DDisplay<T> extends JPanel implements Graphical2DOutputMonitor<T> {

    static {
        System.setProperty("sun.java2d.opengl", "true");
    }

    /** The default frame rate. */
    public static final byte DEFAULT_FRAME_RATE = 25;
    /** The default time per frame. */
    protected static final double TIME_STEP = 1d / DEFAULT_FRAME_RATE;
    protected static final double FREEDOM_RADIUS = 1d;
    private static final Logger L = LoggerFactory.getLogger(Generic2DDisplay.class);
    protected static final int MS_PER_SECOND = 1000;

    /**
     * 
     */
    public static final long PAUSE_DETECTION_THRESHOLD = 200;
    /**
     * How big (in pixels) the selected node should appear.
     */
    protected static final byte SELECTED_NODE_DRAWING_SIZE = 16, SELECTED_NODE_INTERNAL_SIZE = 10;
    private static final long serialVersionUID = 511631766719686842L;

    private transient AngleManagerImpl angleManager;
    private Environment<T> currentEnv;
    private List<Effect> effectStack;
    private volatile boolean firstTime = true; 
    private boolean paintLinks;
    private transient Optional<Node<T>> hooked = Optional.empty();
    private boolean initialized;
    private double lastTime;
    private final Semaphore mapConsistencyMutex = new Semaphore(1);
    private final transient PointerSpeed mouseMovement = new PointerSpeedImpl();
    private int mouseX, mouseY;
    private Node<T> nearest;
    private final ConcurrentMap<Node<T>, Neighborhood<T>> neighbors = new ConcurrentHashMap<>();
    private List<? extends Obstacle2D> obstacles;
    private final ConcurrentMap<Node<T>, Position> positions = new ConcurrentHashMap<>();
    private boolean realTime;
    private int st;

    private long timeInit = System.currentTimeMillis();

    private transient it.unibo.alchemist.boundary.wormhole.interfaces.Wormhole2D wormhole;

    private transient ZoomManager zoomManager;

    private transient boolean isPreviousStateMarking = true;
    private ViewStatus status = ViewStatus.MARK_CLOSER;
    private transient boolean isDraggingMouse;
    private transient Optional<Point> originPoint = Optional.empty();
    private transient Optional<Point> endingPoint = Optional.empty();
    private transient Set<Node<T>> selectedNodes = new HashSet<>();

    /**
     * Initializes a new display with out redrawing the first step.
     */
    public Generic2DDisplay() {
        this(1);
    }

    /**
     * Initializes a new display.
     * 
     * @param step
     *            number of steps to let pass without re-drawing
     */
    public Generic2DDisplay(final int step) {
        super();
        if (!"true".equals(System.getProperty("sun.java2d.opengl"))) {
            L.warn("OpenGL acceleration appears to be disabled on this system. This may impact performance negatively. Please enable it with -Dsun.java2d.opengl=true");
        }
        setStep(step);
        setBackground(Color.WHITE);
        initialized = false;
        final MouseManager mgr = new MouseManager();
        addMouseListener(mgr);
        addMouseMotionListener(mgr);
        addMouseWheelListener(mgr);
        bindKeys();
    }

    /**
     * @return true if it's not automatically marking the closer node and it's not in view-only mode.
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
     * The method binds {@link KeyEvent}s to specific actions.
     */
    private void bindKeys() {
        bindKey(KeyEvent.VK_S, () -> {
            if (status == ViewStatus.SELECTING) {
                resetStatus();
                this.selectedNodes.clear();
            } else if (!isInteracting()) {
                this.status = ViewStatus.SELECTING;
            } 
            this.repaint();
        });
        bindKey(KeyEvent.VK_O, () -> {
            if (status == ViewStatus.SELECTING) {
                this.status = ViewStatus.MOVING;
            }
        });
        bindKey(KeyEvent.VK_C, () -> {
            if (status == ViewStatus.SELECTING) {
                this.status = ViewStatus.CLONING;
            }
        });
        bindKey(KeyEvent.VK_E, () -> {
            if (status == ViewStatus.SELECTING) {
                this.status = ViewStatus.MOLECULING;
                Generic2DDisplay.makeFrame("Moleculing", new MoleculeInjectorGUI<>(selectedNodes), jf -> {
                    final JFrame mol = (JFrame) jf;
                    mol.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    mol.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosed(final WindowEvent e) {
                            selectedNodes.clear();
                            resetStatus();
                        }
                    });
                });

            } 
        });
        bindKey(KeyEvent.VK_D, () -> {
            if (status == ViewStatus.SELECTING) {
                this.status = ViewStatus.DELETING;
                for (final Node<T> n : selectedNodes) {
                    currentEnv.removeNode(n);
                }
                final Simulation<T> sim = currentEnv.getSimulation();
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

    private Shape convertObstacle(final Obstacle2D o) {
        final Rectangle2D r = o.getBounds2D();
        final Position[] points = new Position[] {
                new Continuous2DEuclidean(r.getX(), r.getY()),
                new Continuous2DEuclidean(r.getX() + r.getWidth(), r.getY()),
                new Continuous2DEuclidean(r.getX() + r.getWidth(), r.getY() + r.getHeight()),
                new Continuous2DEuclidean(r.getX(), r.getY() + r.getHeight()) };
        final Path2D path = new GeneralPath();
        for (int i = 0; i < points.length; i++) {
            final Point pt = wormhole.getViewPoint(points[i]);
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
     * @param g
     *            the Graphics2D to use
     */
    protected void drawBackground(final Graphics2D g) {
    }

    /**
     * Actually draws the environment on the view.
     * 
     * @param g
     *            {@link Graphics2D} object responsible for drawing
     */
    protected void drawEnvOnView(final Graphics2D g) {
        if (wormhole == null || !isVisible() || !isEnabled()) {
            return;
        }
        accessData();
        if (hooked.isPresent()) {
            final Position hcoor = positions.get(hooked.get());
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
                .flatMap(neigh -> neigh.getNeighbors().parallelStream()
                    .map(node -> node.compareTo(neigh.getCenter()) > 0
                                ? new Pair<>(neigh.getCenter(), node)
                                : new Pair<>(node, neigh.getCenter())))
                .distinct()
                .map(pair -> mapPair(pair, node ->
                        Optional.ofNullable(onView.get(node))
                        .orElse(wormhole.getViewPoint(positions.get(node)))))
                .forEachOrdered(line -> {
                    final Point p1 = line.getFirst();
                    final Point p2 = line.getSecond();
                    g.drawLine(p1.x, p1.y, p2.x, p2.y);
                });
        }
        releaseData();
        if (isDraggingMouse && status == ViewStatus.MOVING && originPoint.isPresent() && endingPoint.isPresent()) {
            for (final Node<T> n : selectedNodes) {
                if (onView.containsKey(n)) {
                    onView.put(n, new Point(onView.get(n).x + (endingPoint.get().x - originPoint.get().x), 
                            onView.get(n).y + (endingPoint.get().y - originPoint.get().y)));
                }
            }
        }
        g.setColor(Color.GREEN);
        if (effectStack != null) {
            effectStack.forEach(effect -> {
                onView.entrySet().forEach(entry -> {
                    final Point p = entry.getValue();
                    effect.apply(g, entry.getKey(), p.x, p.y);
                });
            });
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
        if (isDraggingMouse && status == ViewStatus.SELECTING && originPoint.isPresent() && endingPoint.isPresent()) {
            g.setColor(Color.BLACK);
            final int x = originPoint.get().x < endingPoint.get().x ? originPoint.get().x : endingPoint.get().x;
            final int y = originPoint.get().y < endingPoint.get().y ? originPoint.get().y : endingPoint.get().y;
            final int width = Math.abs(endingPoint.get().x - originPoint.get().x);
            final int height = Math.abs(endingPoint.get().y - originPoint.get().y);
            g.drawRect(x, y, width, height);
            selectedNodes = onView.entrySet().parallelStream()
                    .filter(nodes -> isInsideRectangle(nodes.getValue(), x, y, width, height))
                    .map(onScreen -> onScreen.getKey())
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
        g.fillOval(x - SELECTED_NODE_DRAWING_SIZE / 2, y - SELECTED_NODE_DRAWING_SIZE / 2, SELECTED_NODE_DRAWING_SIZE, SELECTED_NODE_DRAWING_SIZE);
        g.setColor(c2);
        g.fillOval(x - SELECTED_NODE_INTERNAL_SIZE / 2, y - SELECTED_NODE_INTERNAL_SIZE / 2, SELECTED_NODE_INTERNAL_SIZE, SELECTED_NODE_INTERNAL_SIZE);
    }

    @Override
    public void finished(final Environment<T> environment, final Time time, final long step) {
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
    public int getStep() {
        return st;
    }

    /**
     * Lets child-classes access the wormhole.
     * 
     * @return an {@link it.unibo.alchemist.boundary.wormhole.interfaces.Wormhole2D}
     */
    protected final it.unibo.alchemist.boundary.wormhole.interfaces.Wormhole2D getWormhole() {
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

    /**
     * Initializes all the internal data.
     */
    private void initAll(final Environment<T> env) {
        wormhole = new it.unibo.alchemist.boundary.wormhole.implementation.Wormhole2D(env, this);
        wormhole.center();
        wormhole.optimalZoom();
        angleManager = new AngleManagerImpl(AngleManagerImpl.DEF_DEG_PER_PIXEL);
        zoomManager = new ExponentialZoomManager(wormhole.getZoom(), ExponentialZoomManager.DEF_BASE);
        if (env instanceof Environment2DWithObstacles) {
            loadObstacles(env);
        } else {
            obstacles = null;
        }
    }

    @Override
    public void initialized(final Environment<T> environment) {
        stepDone(environment, null, new DoubleTime(), 0);
    }

    /**
     * The method checks if the closer node is marked.
     *
     * @return true if the closer node is marked
     */
    protected final boolean isCloserNodeMarked() {
        return status == ViewStatus.MARK_CLOSER;
    }

    /**
     * Lets child-classes check if the display is initialized.
     * 
     * @return a <code>boolean</code> value
     */
    protected boolean isInitilized() {
        return initialized;
    }

    /**
     * @return true if this monitor is trying to draw in realtime
     */
    @Override
    public final boolean isRealTime() {
        return realTime;
    }

    private void loadObstacles(final Environment<T> env) {
        obstacles = ((Environment2DWithObstacles<?, ?>) env).getObstacles();
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        drawBackground((Graphics2D) g);
        drawEnvOnView((Graphics2D) g);
    }

    private void releaseData() {
        mapConsistencyMutex.release();
    }

    /**
     * Updates {@link #setToolTipText(String) tooltip} of this component with nearest node from the mouse position.
     *
     * @param x x coordinate of the mouse
     * @param y y coordinate of the mouse
     */
    protected void setDist(final int x, final int y) {
        if (wormhole != null) {
            mouseX = x;
            mouseY = y;
            final Position envMouse = wormhole.getEnvPoint(new Point(mouseX, mouseY));
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
    public void setDrawLinks(final boolean b) {
        if (paintLinks != b) {
            paintLinks = b;
            repaint();
        }
    }

    @Override
    public void setEffectStack(final List<Effect> l) {
        effectStack = l;
    }

    @Override
    public void setMarkCloserNode(final boolean mark) {
        if (!isInteracting()) {
            if (mark) {
                isPreviousStateMarking = true;
                status = ViewStatus.MARK_CLOSER;
            } else {
                isPreviousStateMarking = false;
                status = ViewStatus.VIEW_ONLY;
            }
            repaint();
        }
    }

    @Override
    public void setRealTime(final boolean rt) {
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
     * @param w
     *            an {@link Wormhole2D}
     */
    protected void setWormhole(final it.unibo.alchemist.boundary.wormhole.interfaces.Wormhole2D w) {
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
    public void stepDone(final Environment<T> environment, final Reaction<T> reaction, final Time time, final long step) {
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

    protected void update(final Environment<T> env, final Time time) {
        if (Thread.holdsLock(env)) {
            if (envHasMobileObstacles(env)) {
                loadObstacles(env);
            }
            lastTime = time.toDouble();
            currentEnv = env;
            accessData();
            positions.clear();
            neighbors.clear();
            env.getNodes().parallelStream().forEach(node -> {
                positions.put(node, env.getPosition(node));
                try {
                    neighbors.put(node, env.getNeighborhood(node).clone());
                } catch (Exception e) {
                    L.error("Unable to clone neighborhood for " + node, e);
                }
            });
            releaseData();
            repaint();
        } else {
            throw new IllegalStateException("Only the simulation thread can dictate GUI updates");
        }
    }

    @Override
    public void zoomTo(final Position center, final double zoomLevel) {
        assert center.getDimensions() == 2;
        wormhole.zoomOnPoint(wormhole.getViewPoint(center), zoomLevel);
    }

    /**
     * @param env
     *            the current environment
     * @param <N>
     *            positions
     * @param <D>
     *            distances
     * @return true if env is subclass of {@link Environment2DWithObstacles}
     *         and has mobile obstacles
     */
    protected static <N extends Number, D extends Number> boolean envHasMobileObstacles(final Environment<?> env) {
        return env instanceof Environment2DWithObstacles && ((Environment2DWithObstacles<?, ?>) env).hasMobileObstacles();
    }

    private static <I, O> Pair<O, O> mapPair(
            final Pair<? extends I, ? extends I> pair,
            final Function<? super I, ? extends O> converter) {
        return new Pair<>(converter.apply(pair.getFirst()), converter.apply(pair.getSecond()));
    }

    /**
     * Custom listener for {@link MouseEvent}s.
     */
    protected class MouseManager implements MouseInputListener, MouseWheelListener, MouseMotionListener {
        @Override
        public void mouseClicked(final MouseEvent e) {
            setDist(e.getX(), e.getY());
            if (isCloserNodeMarked() && nearest != null && SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                final NodeTracker<T> monitor = new NodeTracker<>(nearest);
                monitor.stepDone(currentEnv, null, new DoubleTime(lastTime), st);
                final Simulation<T> sim = currentEnv.getSimulation();
                makeFrame("Tracker for node " + nearest.getId(), monitor, jf -> {
                    final JFrame frame = (JFrame) jf;
                    if (sim != null) {
                        sim.addOutputMonitor(monitor);
                        frame.addWindowListener(new WindowAdapter() {
                            @Override
                            public void windowClosing(final WindowEvent e) {
                                sim.removeOutputMonitor(monitor);
                            }
                        });
                    }
                });

            } else if (status == ViewStatus.CLONING && SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
                final Simulation<T> engine = currentEnv.getSimulation();
                final Position envEnding = wormhole.getEnvPoint(e.getPoint());
                engine.schedule(() -> {
                    final Collection<Node<T>> newNodes = new ArrayList<>(selectedNodes.size());
                    try {
                        for (final Node<T> n : selectedNodes) {
                            newNodes.add(n.cloneNode(engine.getTime()));
                        }
                        for (final Node<T> n: newNodes) {
                            currentEnv.addNode(n, envEnding);
                        }
                        update(currentEnv, engine.getTime());
                    } catch (RuntimeException exp) {
                        final String title = "Node cloning error";
                        final String message = "One or more of your nodes do not support cloning, the debug information is:\n"
                                + LangUtils.stackTraceToString(exp);
                        // TODO: switch to JavaFX alerts
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

        @Override
        public void mouseDragged(final MouseEvent e) {
            setDist(e.getX(), e.getY());
            if (wormhole == null || mouseMovement == null) {
                return;
            }
            if (SwingUtilities.isLeftMouseButton(e)) {
                if (isDraggingMouse) {
                    endingPoint = Optional.of(e.getPoint());
                }
                if (mouseMovement != null && !hooked.isPresent() && !isInteracting()) {
                    final Point previous = wormhole.getViewPosition();
                    wormhole.setViewPosition(
                            PointAdapter.from(previous)
                                .sum(PointAdapter.from(mouseMovement.getVariation())).toPoint());
                }
            } else if (SwingUtilities.isRightMouseButton(e) && mouseMovement != null && angleManager != null && wormhole.getMode() != Mode.MAP) {
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
            if (SwingUtilities.isLeftMouseButton(e) && (status == ViewStatus.MOVING || status == ViewStatus.SELECTING)) {
                isDraggingMouse = true;
                originPoint = Optional.of(e.getPoint());
                endingPoint = Optional.of(e.getPoint());
                repaint();
            }
        }

        @Override
        public void mouseReleased(final MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e) && isDraggingMouse) {
                endingPoint = Optional.of(e.getPoint());
                if (status == ViewStatus.MOVING && originPoint.isPresent() && endingPoint.isPresent()) {
                    if (currentEnv.getDimensions() == 2) {
                        final Simulation<T> engine = currentEnv.getSimulation();
                        if (engine != null) {
                            final Position envEnding = wormhole.getEnvPoint(endingPoint.get());
                            final Position envOrigin = wormhole.getEnvPoint(originPoint.get());
                            for (final Node<T> n : selectedNodes) {
                                final Position p = currentEnv.getPosition(n);
                                final double finalX = p.getCoordinate(0) + (envEnding.getCoordinate(0) - envOrigin.getCoordinate(0));
                                final double finalY = p.getCoordinate(1) + (envEnding.getCoordinate(1) - envOrigin.getCoordinate(1));
                                final Position finalPos = PointAdapter.from(finalX, finalY).toPosition();
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
                originPoint = Optional.empty();
                endingPoint = Optional.empty();
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
         * @see #setDist(int, int)
         */
        private void updateMouse(final MouseEvent e) {
            setDist(e.getX(), e.getY());
            if (isCloserNodeMarked()) {
                repaint();
            }
        }
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
        frameEditor.accept(frame);
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

    private static boolean isInsideRectangle(final Point viewPoint, final int rx, final int ry, final int width, final int height) {
        final double x = viewPoint.getX();
        final double y = viewPoint.getY();
        return x >= rx && x <= rx + width && y >= ry && y <= ry + height;
    }

    /**
     * This enum models the status of the {@link Generic2DDisplay}.
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
