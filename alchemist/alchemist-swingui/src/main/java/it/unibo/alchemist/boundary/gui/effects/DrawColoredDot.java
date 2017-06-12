package it.unibo.alchemist.boundary.gui.effects;

import java.awt.Color;
import java.awt.Graphics2D;

import it.unibo.alchemist.boundary.gui.view.property.PropertiesFactory;
import it.unibo.alchemist.boundary.gui.view.property.RangedDoubleProperty;
import it.unibo.alchemist.boundary.wormhole.interfaces.IWormhole2D;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * Simple effect that draws a colored dot for each {@link Node}.
 * <p>
 * It's possible to set the size and the color of the dots.
 */
public class DrawColoredDot extends DrawDot implements EffectFX {
    /** Default generated Serial Version UID. */
    private static final long serialVersionUID = -2329825220099191395L;
    private final RangedDoubleProperty red;
    private final RangedDoubleProperty green;
    private final RangedDoubleProperty blue;
    private final RangedDoubleProperty alpha;

    /**
     * Default constructor.
     */
    public DrawColoredDot() {
        super();

        // Set properties to default color of DrawDot
        red = PropertiesFactory.getColorChannelProperty("R", (double) super.getColor().getRed());
        green = PropertiesFactory.getColorChannelProperty("G", (double) super.getColor().getGreen());
        blue = PropertiesFactory.getColorChannelProperty("B", (double) super.getColor().getBlue());
        alpha = PropertiesFactory.getColorChannelProperty("A", (double) super.getColor().getAlpha());

        // Update the color at each change
        red.addListener(this.updateColor());
        green.addListener(this.updateColor());
        blue.addListener(this.updateColor());
        alpha.addListener(this.updateColor());
    }

    /**
     * {@inheritDoc}
     * <p>
     * For each {@link Node} in the specified {@link Environment}, it will draw
     * a dot of a specified {@link Color} (default: {@link Color#BLACK black}).
     */
    @Override
    public <T> void apply(final Graphics2D graphic, final Environment<T> environment, final IWormhole2D wormhole) {
        super.apply(graphic, environment, wormhole);
    }

    /**
     * Returns a {@link ChangeListener} that updates the color of the dots.
     * 
     * @return the {@code ChangeListener}
     */
    private ChangeListener<Number> updateColor() {
        return (final ObservableValue<? extends Number> observable, final Number oldValue, final Number newValue) -> {
            super.setColor(new Color(red.getValue().intValue(), green.getValue().intValue(), blue.getValue().intValue(),
                    alpha.getValue().intValue()));
        };
    }

    /**
     * The alpha channel of the color of the dots representing each {@link Node}
     * in the {@link Environment} specified when calling
     * {@link #apply(Graphics2D, Environment, IWormhole2D) apply} in percentage.
     * 
     * @return the alpha channel property
     */
    protected DoubleProperty alphaProperty() {
        return this.alpha;
    }

    /**
     * Gets the value of {@code alphaProperty}.
     * 
     * @return the alpha channel of the color of the dots
     */
    protected double getAlpha() {
        return this.alpha.get();
    }

    /**
     * Sets the value of {@code alphaProperty}.
     * 
     * @param alpha
     *            the alpha channel to set
     */
    protected void setAlpha(final double alpha) {
        this.alpha.set(alpha);
    }

    /**
     * The blue channel of the color of the dots representing each {@link Node}
     * in the {@link Environment} specified when calling
     * {@link #apply(Graphics2D, Environment, IWormhole2D) apply} in percentage.
     * 
     * @return the blue channel property
     */
    protected DoubleProperty blueProperty() {
        return this.blue;
    }

    /**
     * Gets the value of {@code blueProperty}.
     * 
     * @return the blue channel of the color of the dots
     */
    protected double getBlue() {
        return this.blue.get();
    }

    /**
     * Sets the value of {@code blueProperty}.
     * 
     * @param blue
     *            the blue channel to set
     */
    protected void setBlue(final double blue) {
        this.blue.set(blue);
    }

    /**
     * The green channel of the color of the dots representing each {@link Node}
     * in the {@link Environment} specified when calling
     * {@link #apply(Graphics2D, Environment, IWormhole2D) apply} in percentage.
     * 
     * @return the green channel property
     */
    protected DoubleProperty greenProperty() {
        return this.green;
    }

    /**
     * Gets the value of {@code greenProperty}.
     * 
     * @return the green channel of the color of the dots
     */
    protected double getGreen() {
        return this.green.get();
    }

    /**
     * Sets the value of {@code greenProperty}.
     * 
     * @param green
     *            the green channel to set
     */
    protected void setGreen(final double green) {
        this.green.set(green);
    }

    /**
     * The red channel of the color of the dots representing each {@link Node}
     * in the {@link Environment} specified when calling
     * {@link #apply(Graphics2D, Environment, IWormhole2D) apply} in percentage.
     * 
     * @return the red channel property
     */
    protected DoubleProperty redProperty() {
        return this.red;
    }

    /**
     * Gets the value of {@code redProperty}.
     * 
     * @return the red channel of the color of the dots
     */
    protected double getRed() {
        return this.red.get();
    }

    /**
     * Sets the value of {@code redProperty}.
     * 
     * @param red
     *            the red channel to set
     */
    protected void setRed(final double red) {
        this.red.set(red);
    }
}
