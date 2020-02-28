package it.unibo.alchemist.boundary.gui.effects;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.boundary.wormhole.interfaces.IWormhole2D;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position2D;
import org.danilopianini.lang.RangedInteger;
import org.danilopianini.view.ExportForGUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 */
public class DrawPedestrianPath implements Effect {

    /**
     *
     */
    protected static final int MAX_COLOUR_VALUE = 255;
    /**
     *
     */
    protected static final int INITIAL_ALPHA_DIVIDER = 3;
    /**
     *
     */
    protected static final Logger L = LoggerFactory.getLogger(DrawShape.class);
    private static final long serialVersionUID = 1L;
    @ExportForGUI(nameToExport = "A")
    private RangedInteger alpha = new RangedInteger(0, MAX_COLOUR_VALUE, MAX_COLOUR_VALUE / INITIAL_ALPHA_DIVIDER);
    @ExportForGUI(nameToExport = "R")
    private RangedInteger red = new RangedInteger(0, MAX_COLOUR_VALUE);
    @ExportForGUI(nameToExport = "G")
    private RangedInteger green = new RangedInteger(0, MAX_COLOUR_VALUE);
    @ExportForGUI(nameToExport = "B")
    private RangedInteger blue = new RangedInteger(0, MAX_COLOUR_VALUE, MAX_COLOUR_VALUE);
    private Color colorCache = Color.BLUE;
    @ExportForGUI(nameToExport = "to be drawn")
    private boolean toBeDrawn;
    private final List<Position2D> path = new ArrayList<>();
    @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    private transient Optional<Node> markerNode = Optional.empty();

    /**
     * @param g        graphics
     * @param n        node
     * @param env      environment
     * @param wormhole the wormhole used to map environment's coords to screen coords
     * @param <T>      concentration type
     * @param <P>      position type
     */
    @SuppressWarnings({"PMD.CompareObjectsWithEquals", "unchecked", "checkstyle:WhitespaceAfter"})
    @SuppressFBWarnings("ES_COMPARING_STRINGS_WITH_EQ")
    @Override
    public <T, P extends Position2D<P>> void apply(final Graphics2D g, final Node<T> n, final Environment<T, P> env, final IWormhole2D<P> wormhole) {
        // if marker node is no longer in the environment or it is no longer displayed, we need to change it
        if (markerNode.isPresent()
                && (!env.getNodes().contains(markerNode.get()) || !wormhole.isInsideView(wormhole.getViewPoint(env.getPosition((Node<T>) markerNode.get()))))) {
            markerNode = Optional.empty();
        }
        if (markerNode.isEmpty()) {
            markerNode = Optional.of(n);
        }
        if (markerNode.get() == n) { // at this point markerNode.isPresent() is always true, so we directly get it
            path.add(env.getPosition(n));
            if (toBeDrawn) {
                colorCache = new Color(red.getVal(), green.getVal(), blue.getVal(), alpha.getVal());
                g.setColor(colorCache);
                path.forEach(p -> {
                    final Point viewP = ((IWormhole2D<Position2D<?>>) wormhole).getViewPoint(p);
                    g.fillOval(viewP.x, viewP.y, 5, 5);
                });
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Color getColorSummary() {
        return colorCache;
    }

    /**
     * @return alpha channel
     */
    public RangedInteger getAlpha() {
        return alpha;
    }

    /**
     * @param alpha alpha channel
     */
    public void setAlpha(final RangedInteger alpha) {
        this.alpha = alpha;
    }

    /**
     * @return red channel
     */
    public RangedInteger getRed() {
        return red;
    }

    /**
     * @param red red channel
     */
    public void setRed(final RangedInteger red) {
        this.red = red;
    }

    /**
     * @return green channel
     */
    public RangedInteger getGreen() {
        return green;
    }

    /**
     * @param green green channel
     */
    public void setGreen(final RangedInteger green) {
        this.green = green;
    }

    /**
     * @return blue channel
     */
    public RangedInteger getBlue() {
        return blue;
    }

    /**
     * @param blue blue channel
     */
    public void setBlue(final RangedInteger blue) {
        this.blue = blue;
    }

    /**
     * @return if it is to be drawn
     */
    public boolean isToBeDrawn() {
        return toBeDrawn;
    }

    /**
     * @param toBeDrawn if it is to be drawn
     */
    public void setToBeDrawn(final boolean toBeDrawn) {
        this.toBeDrawn = toBeDrawn;
    }
}
