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
import it.unibo.alchemist.boundary.wormhole.interfaces.IWormhole2D;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Incarnation;
import it.unibo.alchemist.model.interfaces.Molecule;

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
    private static final int DEFAULT_SIZE = 5;
    /** Max value for a color scale. */
    private static final int MAX_COLOUR_VALUE = 255;
    /** Maximum value of the scale. */
    private static final int MAX_SCALE = 100;
    /** Minimum value of the scale. */
    private static final int MIN_SCALE = 0;
    /** */
    private static final int PROPERTY_SCALE = 10;
    /** Range of the scale. */
    private static final int SCALE_DIFF = MAX_SCALE - MIN_SCALE;
    /** Initial value of the scale. */
    private static final int SCALE_INITIAL = (SCALE_DIFF) / 2 + MIN_SCALE;
    /** Default {@code Color} */
    private static final Color DEFAULT_COLOR = Color.BLACK;
    /** Default logger. */
    private static final Logger L = LoggerFactory.getLogger(DrawShapeFX.class);
    /** Default generated Serial Version UID. */
    private static final long serialVersionUID = 8133306058339338028L;

    @ExportForGUI(nameToExport = "Incarnation to use")
    private CollectionWithCurrentElement<String> curIncarnation;
    @ExportForGUI(nameToExport = "Mode")
    private ModeFX mode = ModeFX.FillEllipse;
    @ExportForGUI(nameToExport = "R")
    private RangedInteger red = new RangedInteger(0, MAX_COLOUR_VALUE);
    @ExportForGUI(nameToExport = "B")
    private RangedInteger blue = new RangedInteger(0, MAX_COLOUR_VALUE);
    @ExportForGUI(nameToExport = "G")
    private RangedInteger green = new RangedInteger(0, MAX_COLOUR_VALUE);
    @ExportForGUI(nameToExport = "A")
    private RangedInteger alpha = new RangedInteger(0, MAX_COLOUR_VALUE, MAX_COLOUR_VALUE);
    @ExportForGUI(nameToExport = "Scale Factor")
    private RangedInteger scaleFactor = new RangedInteger(MIN_SCALE, MAX_SCALE, SCALE_INITIAL);
    @ExportForGUI(nameToExport = "Size")
    private RangedInteger size = new RangedInteger(0, 100, DEFAULT_SIZE);
    @ExportForGUI(nameToExport = "Draw only nodes containing a molecule")
    private boolean molFilter;
    @ExportForGUI(nameToExport = "Molecule")
    private String molString = "";
    @ExportForGUI(nameToExport = "Tune colors using a molecule property")
    private boolean molPropertyFilter;
    @ExportForGUI(nameToExport = "Molecule property")
    private String property = "";
    @ExportForGUI(nameToExport = "Write the value")
    private boolean writingPropertyValue;
    @ExportForGUI(nameToExport = "Channel to use")
    private ColorChannel c = ColorChannel.Alpha;
    @ExportForGUI(nameToExport = "Reverse effect")
    private boolean reverse;
    @ExportForGUI(nameToExport = "Property order of magnitude")
    private RangedInteger propoom = new RangedInteger(-PROPERTY_SCALE, PROPERTY_SCALE, 0);
    @ExportForGUI(nameToExport = "Minimum property value")
    private RangedInteger minprop = new RangedInteger(-PROPERTY_SCALE, PROPERTY_SCALE, 0);
    @ExportForGUI(nameToExport = "Maximum property value")
    private RangedInteger maxprop = new RangedInteger(-PROPERTY_SCALE, PROPERTY_SCALE, PROPERTY_SCALE);

    private Color colorCache = DEFAULT_COLOR;
    private transient Molecule molecule;
    private transient Object molStringCached;
    @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED", justification = "If null, it gets reinitialized anyway if needed")
    private transient CollectionWithCurrentElement<String> prevIncarnation;
    private transient Incarnation<?> incarnation;

    @Override
    public void apply(final Graphics2D graphic, final Environment<?> environment, final IWormhole2D wormhole) {
        // TODO Auto-generated method stub

    }

}
