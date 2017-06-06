package it.unibo.alchemist.boundary.gui.effects;

import java.awt.Color;
import java.awt.Graphics2D;

import org.apache.commons.math3.util.FastMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.alchemist.SupportedIncarnations;
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
import javafx.beans.property.ListProperty;
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

    /** */
    private static final double DEFAULT_SIZE = 5;
    /** Maximum value of the scale. */
    private static final double MAX_SCALE = 100;
    /** Minimum value of the scale. */
    private static final double MIN_SCALE = 0;
    /** */
    private static final double PROPERTY_SCALE = 10;
    /** Range of the scale. */
    private static final double SCALE_DIFF = MAX_SCALE - MIN_SCALE;
    /** Initial value of the scale. */
    private static final double SCALE_INITIAL = (SCALE_DIFF) / 2 + MIN_SCALE;
    /** Default {@code Color} */
    private static final Color DEFAULT_COLOR = Color.BLACK;
    // TODO maybe should switch to JavaFX Color class
    /** Default {@code Logger}. */
    private static final Logger L = LoggerFactory.getLogger(DrawShapeFX.class);
    /** Default generated Serial Version UID. */
    private static final long serialVersionUID = 8133306058339338028L;

    private final transient ListProperty<String> incarnations;
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
    private final SerializableStringProperty currentIncarnation = new SerializableStringProperty();
    private transient String previousIncarnation;
    private transient Incarnation<?> incarnation;

    /**
     * Default constructor. Builds a new {@code DrawShapeFX} effect.
     * 
     * @throws IllegalStateException
     *             if no {@link Incarnation} is available
     */
    public DrawShapeFX() {
        if (SupportedIncarnations.getAvailableIncarnations().isEmpty()) {
            throw new IllegalStateException(getClass().getSimpleName() + " can't work if no incarnation is available.");
        } else {
            incarnations = PropertiesFactory.getIncarnationsListProperty("Incarnation to use");
        }

        moleculeName.addListener(this::updateMoleculeCachedName);
        currentIncarnation.addListener(this::updateIncarnations);
    }

    /**
     * Method meant to be used in a {@link ChangeListener} to update the
     * {@code Molecule} name cached whenever the property is updated.
     * 
     * @see ChangeListener#changed(ObservableValue, Object, Object)
     * 
     * @param observable
     *            the ObservableValue which value changed
     * @param oldValue
     *            the old name
     * @param newValue
     *            the new name
     */
    private void updateMoleculeCachedName(final ObservableValue<? extends String> observable, final String oldValue, // NOPMD
            final String newValue) {
        // - unused parameters are needed to be compatible with ChangeListener
        this.moleculeNameCached = newValue;
        instanziateMolecule();
    }

    /**
     * Method meant to be used in a {@link ChangeListener} to update the
     * incarnation whenever the respective property is updated.
     * 
     * @see ChangeListener#changed(ObservableValue, Object, Object)
     * 
     * @param observable
     *            the ObservableValue which value changed
     * @param oldValue
     *            the old incarnation name
     * @param newValue
     *            the new incarnation name
     */
    private void updateIncarnations(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) { // NOPMD
        // - unused parameters are needed to be compatible with ChangeListener
        this.previousIncarnation = oldValue; // TODO what's for ?
        incarnation = SupportedIncarnations.get(newValue).get();
        instanziateMolecule();
    }

    private void instanziateMolecule() {
        // Process in a separate thread: if it fails, does not kill EDT.
        final Thread createMolecule = new Thread(() -> moleculeObject = incarnation.createMolecule(moleculeName.get()));
        createMolecule.start();
        try {
            createMolecule.join();
        } catch (final InterruptedException e) {
            L.error("Bug: ", e);
        }
    }

    @Override
    public void apply(final Graphics2D graphic, final Environment<?> environment, final IWormhole2D wormhole) {
        environment.forEach(node -> {
            if (!moleculeFilter.get() || (moleculeObject != null && node.contains(moleculeObject))) {
                final double ks = (scaleFactor.get() - MIN_SCALE) * 2 / SCALE_DIFF;
                final double sizex = size.get();
                final double startx = wormhole.getViewPosition().getX() - sizex / 2;
                final double sizey = FastMath.ceil(sizex * ks);
                final double starty = wormhole.getViewPosition().getY() - sizey / 2;
                final Color toRestore = graphic.getColor();
                colorCache = new Color(red.getValue().intValue(), green.getValue().intValue(), blue.getValue().intValue(),
                        alpha.getValue().intValue());
                // TODO maybe should switch to JavaFX Color class
                Color newcolor = colorCache;
                if (useMoleculeProperty.get() && moleculeObject != null) {
                    final int minV = (int) (minprop.get() * FastMath.pow(PROPERTY_SCALE, orderOfMagnitude.get()));
                    final int maxV = (int) (maxprop.get() * FastMath.pow(PROPERTY_SCALE, orderOfMagnitude.get()));
                    if (minV < maxV) {
                        @SuppressWarnings({ "rawtypes", "unchecked" })
                        double propval = incarnation.getProperty((Node) node, moleculeObject, moleculePropertyName.get());
                        if (isWritingPropertyValue()) {
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

    protected DoubleProperty alphaProperty() {
        return this.alpha;
    }

    protected double getAlpha() {
        return this.alpha.get();
    }

    protected void setAlpha(final double alpha) {
        this.alpha.set(alpha);
    }

    protected DoubleProperty blueProperty() {
        return this.blue;
    }

    protected double getBlue() {
        return this.blue.get();
    }

    protected void setBlue(final double blue) {
        this.blue.set(blue);
    }

    protected EnumProperty<ColorChannel> colorChannelProperty() {
        return this.colorChannel;
    }

    protected ColorChannel getColorChannel() {
        return this.colorChannel.get();
    }

    protected void setColorChannel(final ColorChannel colorChannel) {
        this.colorChannel.set(colorChannel);
    }

    protected DoubleProperty greenProperty() {
        return this.green;
    }

    protected double getGreen() {
        return this.green.get();
    }

    protected void setGreen(final double green) {
        this.green.set(green);
    }

    protected StringProperty currentIncarnationProperty() {
        return this.currentIncarnation;
    }

    protected String getCurrentIncarnation() {
        return this.currentIncarnation.get();
    }

    protected void setCurrentIncarnation(final String currentIncarnation) {
        this.currentIncarnation.set(currentIncarnation);
    }

    protected DoubleProperty maxpropProperty() {
        return this.maxprop;
    }

    protected double getMaxprop() {
        return this.maxprop.get();
    }

    protected void setMaxprop(final double maxprop) {
        this.maxprop.set(maxprop);
    }

    protected DoubleProperty minpropProperty() {
        return this.minprop;
    }

    protected double getMinprop() {
        return this.minprop.get();
    }

    protected void setMinprop(final double minprop) {
        this.minprop.set(minprop);
    }

    protected EnumProperty<ModeFX> modeProperty() {
        return this.mode;
    }

    protected ModeFX getMode() {
        return this.mode.get();
    }

    protected void setMode(final ModeFX mode) {
        this.mode.set(mode);
    }

    protected Molecule getMoleculeObject() {
        return this.moleculeObject;
    }

    protected StringProperty moleculeNameProperty() {
        return this.moleculeName;
    }

    protected String getMoleculeName() {
        return this.moleculeName.get();
    }

    protected void setMoleculeName(final String moleculeName) {
        this.moleculeName.set(moleculeName);
    }

    protected StringProperty moleculePropertyNameProperty() {
        return this.moleculePropertyName;
    }

    protected String getMoleculePropertyName() {
        return this.moleculePropertyName.get();
    }

    protected void setMoleculePropertyName(final String moleculePropertyName) {
        this.moleculePropertyName.set(moleculePropertyName);
    }

    protected DoubleProperty orderOfMagnitudeProperty() {
        return this.orderOfMagnitude;
    }

    protected double getOrderOfMagnitude() {
        return this.orderOfMagnitude.get();
    }

    protected void setOrderOfMagnitude(final double orderOfMagnitude) {
        this.orderOfMagnitude.set(orderOfMagnitude);
    }

    protected DoubleProperty redProperty() {
        return this.red;
    }

    protected double getRed() {
        return this.red.get();
    }

    protected void setRed(final double red) {
        this.red.set(red);
    }

    protected DoubleProperty scaleFactorProperty() {
        return this.scaleFactor;
    }

    protected double getScaleFactor() {
        return this.scaleFactor.get();
    }

    protected void setScaleFactor(final double scaleFactor) {
        this.scaleFactor.set(scaleFactor);
    }

    protected DoubleProperty sizeProperty() {
        return this.size;
    }

    protected double getSize() {
        return this.size.get();
    }

    protected void setSize(final double size) {
        this.size.set(size);
    }

    protected BooleanProperty moleculeFilterProperty() {
        return this.moleculeFilter;
    }

    protected boolean isMoleculeFilter() {
        return this.moleculeFilter.get();
    }

    protected void setMoleculeFilter(final boolean moleculeFilter) {
        this.moleculeFilter.set(moleculeFilter);
    }

    protected BooleanProperty useMoleculePropertyProperty() {
        return this.useMoleculeProperty;
    }

    protected boolean isUseMoleculeProperty() {
        return this.useMoleculeProperty.get();
    }

    protected void setUseMoleculeProperty(final boolean useMoleculeProperty) {
        this.useMoleculeProperty.set(useMoleculeProperty);
    }

    protected BooleanProperty reverseProperty() {
        return this.reverse;
    }

    protected boolean isReverse() {
        return this.reverse.get();
    }

    protected void setReverse(final boolean reverse) {
        this.reverse.set(reverse);
    }

    protected BooleanProperty writingPropertyValuePropery() {
        return this.writePropertyValue;
    }

    protected boolean isWritingPropertyValue() {
        return this.writePropertyValue.get();
    }

    protected void setWritingPropertyValue(final boolean writingPropertyValue) {
        this.writePropertyValue.set(writingPropertyValue);
    }

}
