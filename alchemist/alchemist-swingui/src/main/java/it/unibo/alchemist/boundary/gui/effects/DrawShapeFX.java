package it.unibo.alchemist.boundary.gui.effects;

import java.awt.Color;
import java.awt.Graphics2D;

import org.apache.commons.math3.util.FastMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.alchemist.boundary.gui.ColorChannel;
import it.unibo.alchemist.boundary.gui.view.property.EnumProperty;
import it.unibo.alchemist.boundary.gui.view.property.PropertiesFactory;
import it.unibo.alchemist.boundary.gui.view.property.RangedDoubleProperty;
import it.unibo.alchemist.boundary.gui.view.property.SerializableBooleanProperty;
import it.unibo.alchemist.boundary.gui.view.property.SerializableStringProperty;
import it.unibo.alchemist.boundary.wormhole.interfaces.IWormhole2D;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Incarnation;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * Simple effect that draws all molecules as simple shapes.
 */
public class DrawShapeFX implements EffectFX {
    /**
     * Enumeration that models the mode to use the {@link DrawShapeFX}.
     */
    public enum ModeFX {
        /** Node as an empty ellipse. */
        DrawEllipse,
        /** Node as an empty rectangle. */
        DrawRectangle,
        /** Node as a filled ellipse. */
        FillEllipse,
        /** Node as a filled rectangle. */
        FillRectangle;

        @Override
        public String toString() {
            final String sup = super.toString();
            final StringBuilder sb = new StringBuilder(2 * sup.length());
            if (!sup.isEmpty()) {
                sb.append(sup.charAt(0));
            }
            for (int i = 1; i < sup.length(); i++) {
                final char curChar = sup.charAt(i);
                if (Character.isUpperCase(curChar)) {
                    sb.append(' ');
                }
                sb.append(curChar);
            }
            return sb.toString();
        }
    }

    /** Default size of the shape. */
    private static final double DEFAULT_SIZE = 5;
    /** Maximum value for the scale factor. */
    private static final double MAX_SCALE = 100;
    /** Minimum value for the scale factor. */
    private static final double MIN_SCALE = 0;
    /** */
    private static final double PROPERTY_SCALE = 10;
    /** Range for the scale factor. */
    private static final double SCALE_DIFF = MAX_SCALE - MIN_SCALE;
    /** Initial value of the scale factor. */
    private static final double SCALE_INITIAL = (SCALE_DIFF) / 2 + MIN_SCALE;
    /** Default {@code Color}. */
    private static final Color DEFAULT_COLOR = Color.BLACK;
    // TODO maybe should switch to JavaFX Color class
    /** Default {@code Logger}. */
    private static final Logger L = LoggerFactory.getLogger(DrawShapeFX.class);
    /** Default generated Serial Version UID. */
    private static final long serialVersionUID = 8133306058339338028L;

    private final EnumProperty<ModeFX> mode = new EnumProperty<ModeFX>(null, "Mode", ModeFX.FillEllipse);
    private final RangedDoubleProperty red = PropertiesFactory.getColorChannelProperty("R");
    private final RangedDoubleProperty green = PropertiesFactory.getColorChannelProperty("G");
    private final RangedDoubleProperty blue = PropertiesFactory.getColorChannelProperty("B");
    private final RangedDoubleProperty alpha = PropertiesFactory.getColorChannelProperty("A");
    private final RangedDoubleProperty scaleFactor = new RangedDoubleProperty(null, "Scale Factor", SCALE_INITIAL, MIN_SCALE, MAX_SCALE);
    private final RangedDoubleProperty size = PropertiesFactory.getPercentageRangedProperty("Size", DEFAULT_SIZE);
    private final SerializableBooleanProperty moleculeFilter = new SerializableBooleanProperty(null,
            "Draw only nodes containing a molecule", false);
    private final SerializableStringProperty moleculeName = new SerializableStringProperty(null, "Molecule");
    private final SerializableBooleanProperty useMoleculeProperty = new SerializableBooleanProperty(null,
            "Tune colors using a molecule property", false);
    private final SerializableStringProperty moleculePropertyName = new SerializableStringProperty(null, "Molecule property");
    private final SerializableBooleanProperty writePropertyValue = new SerializableBooleanProperty(null, "Write the value", false);
    private final EnumProperty<ColorChannel> colorChannel = new EnumProperty<ColorChannel>(null, "Channel to use", ColorChannel.Alpha);
    // TODO maybe should switch to JavaFX Color class
    private final SerializableBooleanProperty reverse = new SerializableBooleanProperty(null, "Reverse effect", false);

    private final RangedDoubleProperty orderOfMagnitude = new RangedDoubleProperty(null, "Property order of magnitude", 0, -PROPERTY_SCALE,
            PROPERTY_SCALE);
    private final RangedDoubleProperty minprop = new RangedDoubleProperty(null, "Minimum property value", 0, -PROPERTY_SCALE,
            PROPERTY_SCALE);
    private final RangedDoubleProperty maxprop = new RangedDoubleProperty(null, "Maximum property value", PROPERTY_SCALE, -PROPERTY_SCALE,
            PROPERTY_SCALE);

    private Color colorCache = DEFAULT_COLOR;
    // TODO maybe should switch to JavaFX Color class
    private transient Molecule moleculeObject;
    private transient String moleculeNameCached;
    private String name;

    /**
     * Default constructor.
     */
    public DrawShapeFX() {
        moleculeName.addListener(this.updateMoleculeCachedName());
    }

    /**
     * This method returns a {@link ChangeListener} for {@link Molecule} name
     * update.
     * 
     * @return a {@code ChangeListener} for {@code Molecule} name update
     */
    private ChangeListener<String> updateMoleculeCachedName() {
        return (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            this.moleculeNameCached = newValue;
        };

    }

    /**
     * This method updates current internal instance of molecule with a new
     * instance.
     * <p>
     * The process of instantiation runs in a separate thread: if it fails, it
     * will not kill EDT.
     * 
     * @param <T>
     *            the {@link Environment} type
     * @param incarnation
     *            the incarnation that will parse the molecule name cached
     * @see Incarnation#createMolecule(String)
     */
    private <T> void instanziateMolecule(final Incarnation<T> incarnation) {
        // Process in a separate thread: if it fails, does not kill EDT.
        final Thread createMolecule = new Thread(() -> moleculeObject = incarnation.createMolecule(moleculeNameCached));
        createMolecule.start();
        try {
            createMolecule.join();
        } catch (final InterruptedException e) {
            L.error("Bug: ", e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * For each {@link Node} in the specified {@link Environment}, it will draw
     * a shape of a specified {@link Color} (default: {@link Color#BLACK black}).
     * Is it possible to tune the shape's scale factor and to change shape color
     * according to a {@link Molecule} property.
     * 
     * @throws IllegalStateException
     *             if no {@link Incarnation} is available
     */
    @Override
    public <T> void apply(final Graphics2D graphic, final Environment<T> environment, final IWormhole2D wormhole) {
        final Incarnation<T> incarnation = environment.getIncarnation()
                .orElseThrow(() -> new IllegalStateException("The specified Environment does not specify any Incarnation"));
        if (!this.moleculeName.get().equals(moleculeNameCached)) {
            instanziateMolecule(incarnation);
        }

        environment.forEach((Node<T> node) -> {
            if (!moleculeFilter.get() || (moleculeObject != null && node.contains(moleculeObject))) {
                final double ks = (scaleFactor.get() - MIN_SCALE) * 2 / SCALE_DIFF;
                final double sizex = size.get();
                final double startx = wormhole.getViewPoint(environment.getPosition(node)).getX() - sizex / 2;
                final double sizey = FastMath.ceil(sizex * ks);
                final double starty = wormhole.getViewPoint(environment.getPosition(node)).getY() - sizey / 2;
                final Color toRestore = graphic.getColor();
                colorCache = new Color(red.getValue().intValue(), green.getValue().intValue(), blue.getValue().intValue(),
                        alpha.getValue().intValue());
                // TODO maybe should switch to JavaFX Color class
                Color newcolor = colorCache;
                if (useMoleculeProperty.get() && moleculeObject != null) {
                    final double minV = minprop.get() * FastMath.pow(PROPERTY_SCALE, orderOfMagnitude.get());
                    final double maxV = maxprop.get() * FastMath.pow(PROPERTY_SCALE, orderOfMagnitude.get());
                    if (minV < maxV) {
                        // TODO not so good to use unchecked Node, but for now
                        // it's ok
                        double propval = incarnation.getProperty(node, moleculeObject, moleculePropertyName.get());
                        if (writePropertyValue.get()) {
                            graphic.setColor(colorCache);
                            graphic.drawString(Double.toString(propval), (int) (startx + sizex), (int) (starty + sizey));
                        }
                        propval = FastMath.min(FastMath.max(propval, minV), maxV);
                        propval = (propval - minV) / (maxV - minV);
                        if (reverse.get()) {
                            propval = 1f - propval;
                        }
                        newcolor = colorChannel.get().alter(newcolor, (float) propval);
                    }
                }
                graphic.setColor(newcolor);
                switch (mode.get()) {
                case FillEllipse:
                    graphic.fillOval((int) startx, (int) starty, (int) sizex, (int) sizey);
                    break;
                case DrawEllipse:
                    graphic.drawOval((int) startx, (int) starty, (int) sizex, (int) sizey);
                    break;
                case DrawRectangle:
                    graphic.drawRect((int) startx, (int) starty, (int) sizex, (int) sizey);
                    break;
                case FillRectangle:
                    graphic.fillRect((int) startx, (int) starty, (int) sizex, (int) sizey);
                    break;
                default:
                    graphic.fillOval((int) startx, (int) starty, (int) sizex, (int) sizey);
                }
                graphic.setColor(toRestore);
            }

        });
    }

    /**
     * The alpha channel of the color of the shapes, representing each
     * {@link Node} in the {@link Environment} specified when calling
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
     * @return the alpha channel of the color of the shapes
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
     * The blue channel of the color of the shapes, representing each {@link Node}
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
     * @return the blue channel of the color of the shapes
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
     * The color channel of the shapes, representing which {@link Color color}
     * channel would be tuned according to {@link Molecule} property value.
     * 
     * @return the color channel property
     */
    protected EnumProperty<ColorChannel> colorChannelProperty() {
        return this.colorChannel;
    }

    /**
     * Gets the value of {@code colorChannelProperty}.
     * 
     * @return the color channel
     */
    protected ColorChannel getColorChannel() {
        return this.colorChannel.get();
    }

    /**
     * Sets the value of {@code colorChannelProperty}.
     * 
     * @param colorChannel
     *            the color channel to set
     */
    protected void setColorChannel(final ColorChannel colorChannel) {
        this.colorChannel.set(colorChannel);
    }

    /**
     * The green channel of the color of the shapes, representing each
     * {@link Node} in the {@link Environment} specified when calling
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
     * @return the green channel of the color of the shapes
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
     * The maximum value of the molecule property.
     * 
     * @return the maxpropProperty
     */
    protected DoubleProperty maxpropProperty() {
        return this.maxprop;
    }

    /**
     * Gets the value of {@code maxpropProperty}.
     * 
     * @return the value of {@code maxpropProperty}
     */
    protected double getMaxprop() {
        return this.maxprop.get();
    }

    /**
     * Sets the value of {@code maxpropProperty}.
     * 
     * @param maxprop
     *            the value of {@code maxpropProperty} to set
     */
    protected void setMaxprop(final double maxprop) {
        this.maxprop.set(maxprop);
    }

    /**
     * The minimum value of the molecule property.
     * 
     * @return the minpropProperty
     */
    protected DoubleProperty minpropProperty() {
        return this.minprop;
    }

    /**
     * Gets the value of {@code minpropProperty}.
     * 
     * @return the value of {@code minpropProperty}
     */
    protected double getMinprop() {
        return this.minprop.get();
    }

    /**
     * Sets the value of {@code minpropProperty}.
     * 
     * @param minprop
     *            the value of {@code minpropProperty} to set
     */
    protected void setMinprop(final double minprop) {
        this.minprop.set(minprop);
    }

    /**
     * The {@link ModeFX} of this effect, representing which type of shape
     * ({@link Graphics2D#fillOval(int, int, int, int) oval} or
     * {@link Graphics2D#fillRect(int, int, int, int) rectangle}, full or empty)
     * each {@link Node} in the {@link Environment} would be drawn with.
     * 
     * @return the mode property
     */
    protected EnumProperty<ModeFX> modeProperty() {
        return this.mode;
    }

    /**
     * Gets the value of {@code modeProperty}.
     * 
     * @return the value of {@code modeProperty}
     */
    protected ModeFX getMode() {
        return this.mode.get();
    }

    /**
     * Sets the value of {@code modeProperty}.
     * 
     * @param mode
     *            the value of {@code modeProperty} to set
     */
    protected void setMode(final ModeFX mode) {
        this.mode.set(mode);
    }

    /**
     * The name of the {@link Molecule} that will be represented.
     * <p>
     * A new instance of the {@code Molecule} is created every time the value of
     * this property changes.
     * 
     * @return the molecule name property
     */
    protected StringProperty moleculeNameProperty() {
        return this.moleculeName;
    }

    /**
     * Gets the value of {@code moleculeNameProperty}.
     * 
     * @return the value of {@code moleculeNameProperty}
     */
    protected String getMoleculeName() {
        return this.moleculeName.get();
    }

    /**
     * Sets the value of {@code moleculeNameProperty}.
     * 
     * @param moleculeName
     *            the value of {@code moleculeNameProperty} to set
     */
    protected void setMoleculeName(final String moleculeName) {
        this.moleculeName.set(moleculeName);
    }

    /**
     * The name of the {@link Molecule} property which value will be used to
     * tune shape color.
     * 
     * @return the molecule property name
     */
    protected StringProperty moleculePropertyNameProperty() {
        return this.moleculePropertyName;
    }

    /**
     * Gets the value of {@code moleculePropertyNameProperty}.
     * 
     * @return the value of {@code moleculePropertyNameProperty}
     */
    protected String getMoleculePropertyName() {
        return this.moleculePropertyName.get();
    }

    /**
     * Sets the value of {@code moleculePropertyNameProperty}.
     * 
     * @param moleculePropertyName
     *            the value of {@code moleculePropertyNameProperty} to set
     */
    protected void setMoleculePropertyName(final String moleculePropertyName) {
        this.moleculePropertyName.set(moleculePropertyName);
    }

    /**
     * The order of magnitude the {@link Molecule} property value will influence
     * the color variations of the shapes.
     * 
     * @return the order of magnitude property
     */
    protected DoubleProperty orderOfMagnitudeProperty() {
        return this.orderOfMagnitude;
    }

    /**
     * Gets the value of {@code orderOfMagnitudeProperty}.
     * 
     * @return the value of {@code orderOfMagnitudeProperty}
     */
    protected double getOrderOfMagnitude() {
        return this.orderOfMagnitude.get();
    }

    /**
     * Sets the value of {@code orderOfMagnitudeProperty}.
     * 
     * @param orderOfMagnitude
     *            the value of {@code orderOfMagnitudeProperty} to set
     */
    protected void setOrderOfMagnitude(final double orderOfMagnitude) {
        this.orderOfMagnitude.set(orderOfMagnitude);
    }

    /**
     * The red channel of the color of the shapes, representing each {@link Node}
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
     * @return the red channel of the color of the shapes
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

    /**
     * The scale factor used when representing the shapes; tuning it will modify
     * the regularity of the shape.
     * 
     * @return the scale factor property
     * @see Graphics2D
     */
    protected DoubleProperty scaleFactorProperty() {
        return this.scaleFactor;
    }

    /**
     * Gets the value of {@code scaleFactorProperty}.
     * 
     * @return the value of {@code scaleFactorProperty}
     */
    protected double getScaleFactor() {
        return this.scaleFactor.get();
    }

    /**
     * Sets the value of {@code scaleFactorProperty}.
     * 
     * @param scaleFactor
     *            the value of {@code scaleFactorProperty} to set
     */
    protected void setScaleFactor(final double scaleFactor) {
        this.scaleFactor.set(scaleFactor);
    }

    /**
     * The size of the shapes, representing each {@link Node} in the
     * {@link Environment} specified when calling
     * {@link #apply(Graphics2D, Environment, IWormhole2D) apply} in percentage.
     * 
     * @return the size property
     */
    protected DoubleProperty sizeProperty() {
        return this.size;
    }

    /**
     * Gets the value of the property {@code sizeProperty}.
     * 
     * @return the size of the shapes
     */
    protected Double getSize() {
        return this.size.get();
    }

    /**
     * Sets the value of the property {@code sizeProperty}.
     * 
     * @param size
     *            the size to set
     * @throws IllegalArgumentException
     *             if the provided value is not a valid percentage
     */
    protected void setSize(final Double size) {
        this.size.set(size);
    }

    /**
     * If true, only the {@link Node}s containing a {@link Molecule} will be
     * drawn.
     * 
     * @return the property for this filter
     */
    protected BooleanProperty moleculeFilterProperty() {
        return this.moleculeFilter;
    }

    /**
     * Gets the value of {@code moleculeFilterProperty}.
     * 
     * @return the value of {@code moleculeFilterProperty}
     */
    protected boolean isMoleculeFilter() {
        return this.moleculeFilter.get();
    }

    /**
     * Sets the value of {@code moleculeFilterProperty}.
     * 
     * @param moleculeFilter
     *            the value of {@code moleculeFilterProperty} to set
     */
    protected void setMoleculeFilter(final boolean moleculeFilter) {
        this.moleculeFilter.set(moleculeFilter);
    }

    /**
     * If true, the value of the specified {@link Molecule} property will
     * influence the color of the shape.
     * 
     * @return the property for this filter
     */
    protected BooleanProperty useMoleculePropertyProperty() {
        return this.useMoleculeProperty;
    }

    /**
     * Gets the value of {@code useMoleculePropertyProperty}.
     * 
     * @return the value of {@code useMoleculePropertyProperty}
     */
    protected boolean isUseMoleculeProperty() {
        return this.useMoleculeProperty.get();
    }

    /**
     * Sets the value of {@code useMoleculePropertyProperty}.
     * 
     * @param useMoleculeProperty
     *            the value of {@code useMoleculePropertyProperty} to set
     */
    protected void setUseMoleculeProperty(final boolean useMoleculeProperty) {
        this.useMoleculeProperty.set(useMoleculeProperty);
    }

    /**
     * If true, it will reverse the effects of color variations.
     * 
     * @return the reverse property
     */
    protected BooleanProperty reverseProperty() {
        return this.reverse;
    }

    /**
     * Gets the value of {@code reverseProperty}.
     * 
     * @return the value of {@code reverseProperty}
     */
    protected boolean isReverse() {
        return this.reverse.get();
    }

    /**
     * Sets the value of {@code reverseProperty}.
     * 
     * @param reverse
     *            the value of {@code reverseProperty} to set
     */
    protected void setReverse(final boolean reverse) {
        this.reverse.set(reverse);
    }

    /**
     * If true, the value of the specified {@link Molecule} property will be
     * written near the related shape.
     * 
     * @return the write property value property
     */
    protected BooleanProperty writePropertyValuePropery() {
        return this.writePropertyValue;
    }

    /**
     * Gets the value of {@code writePropertyValuePropery}.
     * 
     * @return the value of {@code writePropertyValuePropery}
     */
    protected boolean isWritePropertyValue() {
        return this.writePropertyValue.get();
    }

    /**
     * Sets the value of {@code writePropertyValuePropery}.
     * 
     * @param writePropertyValue
     *            the value of {@code writePropertyValuePropery} to set
     */
    protected void setWritePropertyValue(final boolean writePropertyValue) {
        this.writePropertyValue.set(writePropertyValue);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Returns the internal {@link Molecule} instance used for references.
     * 
     * @return the internal {@code Molecule} object
     */
    protected Molecule getMoleculeObject() {
        return this.moleculeObject;
    }

}
