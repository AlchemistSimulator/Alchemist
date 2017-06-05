package it.unibo.alchemist.boundary.gui.effects;

import java.awt.Color;
import java.awt.Graphics2D;

import org.danilopianini.lang.CollectionWithCurrentElement;
import org.danilopianini.lang.RangedInteger;
import org.danilopianini.view.ExportForGUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.boundary.gui.ColorChannel;
import it.unibo.alchemist.boundary.gui.effects.DrawShape.Mode;
import it.unibo.alchemist.boundary.gui.view.property.EnumProperty;
import it.unibo.alchemist.boundary.gui.view.property.PropertiesFactory;
import it.unibo.alchemist.boundary.gui.view.property.RangedDoubleProperty;
import it.unibo.alchemist.boundary.gui.view.property.SerializableBooleanProperty;
import it.unibo.alchemist.boundary.gui.view.property.SerializableStringProperty;
import it.unibo.alchemist.boundary.wormhole.interfaces.IWormhole2D;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Incarnation;
import it.unibo.alchemist.model.interfaces.Molecule;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.StringProperty;

public class DrawShapeFX implements EffectFX {
    /**
     * Enumeration that models the mode to use the DrawShape.
     */
    public enum ModeFX {
        /***/
        DrawEllipse, DrawRectangle, FillEllipse, FillRectangle;

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
    /** Max value for a color scale. */
    private static final double MAX_COLOUR_VALUE = 255;
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
    /** Default logger. */
    private static final Logger L = LoggerFactory.getLogger(DrawShapeFX.class);
    /** Default generated Serial Version UID. */
    private static final long serialVersionUID = 8133306058339338028L;

    private final transient ListProperty<String> incarnations = PropertiesFactory.getIncarnationsListProperty("Incarnation to use");
    private final EnumProperty<ModeFX> mode = new EnumProperty<ModeFX>(null, "Mode", ModeFX.FillEllipse);
    private final RangedDoubleProperty red = PropertiesFactory.getColorChannelProperty("R");
    private final RangedDoubleProperty green = PropertiesFactory.getColorChannelProperty("G");
    private final RangedDoubleProperty blue = PropertiesFactory.getColorChannelProperty("B");
    private final RangedDoubleProperty alpha = PropertiesFactory.getColorChannelProperty("A");
    private final RangedDoubleProperty scaleFactor = new RangedDoubleProperty(null, "Scale Factor", SCALE_INITIAL, MIN_SCALE, MAX_SCALE);
    private final RangedDoubleProperty size = PropertiesFactory.getPercentageRangedProperty("Size", DEFAULT_SIZE);
    private final SerializableBooleanProperty molFilter = new SerializableBooleanProperty(null, "Draw only nodes containing a molecule",
            false);
    private final SerializableStringProperty molString = new SerializableStringProperty(null, "Molecule");
    private final SerializableBooleanProperty molPropertyFilter = new SerializableBooleanProperty(null,
            "Tune colors using a molecule property", false);
    private final SerializableStringProperty property = new SerializableStringProperty(null, "Molecule property");
    private final SerializableBooleanProperty writingPropertyValue = new SerializableBooleanProperty(null, "Write the value", false);
    private final EnumProperty<ColorChannel> colorChannel = new EnumProperty<ColorChannel>(null, "Channel to use", ColorChannel.Alpha);
    private final SerializableBooleanProperty reverse = new SerializableBooleanProperty(null, "Reverse effect", false);
    private final RangedDoubleProperty propoom = new RangedDoubleProperty(null, "Property order of magnitude", 0, -PROPERTY_SCALE,
            PROPERTY_SCALE);
    private final RangedDoubleProperty minprop = new RangedDoubleProperty(null, "Minimum property value", 0, -PROPERTY_SCALE,
            PROPERTY_SCALE);
    private RangedDoubleProperty maxprop = new RangedDoubleProperty(null, "Maximum property value", PROPERTY_SCALE, -PROPERTY_SCALE,
            PROPERTY_SCALE);

    private Color colorCache = DEFAULT_COLOR;
    private transient Molecule molecule;
    private transient Object molStringCached;
    private final SerializableStringProperty currentIncarnation = new SerializableStringProperty();
    private transient SerializableStringProperty prevIncarnation;
    private transient Incarnation<?> incarnation;

    @Override
    public void apply(final Graphics2D graphic, final Environment<?> environment, final IWormhole2D wormhole) {
        // TODO Auto-generated method stub

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

    protected void getCurrentIncarnation(final String currentIncarnation) {
        this.currentIncarnation.set(currentIncarnation);
    }

    protected DoubleProperty maxpropProperty() {
        return this.maxprop;
    }

    protected double getMaxprop() {
        return this.maxprop.get();
    }

    protected void setMaxprop(final double mp) {
        this.maxprop.set(mp);
    }

    protected DoubleProperty minpropProperty() {
        return this.minprop;
    }

    protected double getMinprop() {
        return this.minprop.get();
    }

    protected void setMinprop(final double mp) {
        this.minprop.set(mp);
    }

    protected EnumProperty<ModeFX> modeProperty() {
        return this.mode;
    }

    protected ModeFX getMode() {
        return this.mode.get();
    }

    protected void setMode(final ModeFX m) {
        this.mode.set(m);
    }

    protected Molecule getMolecule() {
        return this.molecule;
    }

    protected StringProperty molStringProperty() {
        return this.molString;
    }

    protected String getMolString() {
        return this.molString.get();
    }

    protected void setMolString(final String mols) {
        this.molString.set(mols);
    }

    protected StringProperty molPropertyProperty() {
        return this.property;
    }

    protected String getMolProperty() {
        return this.property.get();
    }

    protected void setMolProperty(final String pr) {
        this.property.set(pr);
    }

    protected DoubleProperty propoomProperty() {
        return this.propoom;
    }

    protected double getPropoom() {
        return this.propoom.get();
    }

    protected void setPropoom(final double oom) {
        this.propoom.set(oom);
    }

    protected DoubleProperty redProperty() {
        return this.red;
    }

    protected double getRed() {
        return this.red.get();
    }

    protected void setRed(final double r) {
        this.red.set(r);
    }

    protected DoubleProperty scaleFactorProperty() {
        return this.scaleFactor;
    }

    protected double getScaleFactor() {
        return this.scaleFactor.get();
    }

    protected void setScaleFactor(final double sf) {
        this.scaleFactor.set(sf);
    }

    protected DoubleProperty sizeProperty() {
        return this.size;
    }

    protected double getSize() {
        return this.size.get();
    }

    protected void setSize(final double s) {
        this.size.set(s);
    }

    protected BooleanProperty molFilterProperty() {
        return this.molFilter;
    }

    protected boolean isMolFilter() {
        return this.molFilter.get();
    }

    protected void setMolFilter(final boolean mol) {
        this.molFilter.set(mol);
    }

    protected BooleanProperty molPropertyFilterProperty() {
        return this.molPropertyFilter;
    }

    protected boolean isMolPropertyFilter() {
        return this.molPropertyFilter.get();
    }

    protected void setMolPropertyFilter(final boolean molpf) {
        this.molPropertyFilter.set(molpf);
    }

    protected BooleanProperty reverseProperty() {
        return this.reverse;
    }

    protected boolean isReverse() {
        return this.reverse.get();
    }

    protected void setReverse(final boolean r) {
        this.reverse.set(r);
    }

    protected BooleanProperty writingPropertyValuePropery() {
        return this.writingPropertyValue;
    }

    protected boolean isWritingPropertyValue() {
        return this.writingPropertyValue.get();
    }

    protected void setWritingPropertyValue(final boolean writingPropertyValue) {
        this.writingPropertyValue.set(writingPropertyValue);
    }

}
