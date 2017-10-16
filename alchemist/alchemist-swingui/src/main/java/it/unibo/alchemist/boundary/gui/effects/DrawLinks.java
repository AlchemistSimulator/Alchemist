package it.unibo.alchemist.boundary.gui.effects;

import it.unibo.alchemist.boundary.gui.utility.ResourceLoader;
import it.unibo.alchemist.boundary.gui.view.properties.PropertyFactory;
import it.unibo.alchemist.boundary.gui.view.properties.RangedDoubleProperty;
import it.unibo.alchemist.boundary.wormhole.interfaces.BidimensionalWormhole;
import it.unibo.alchemist.model.interfaces.Environment;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class DrawLinks implements EffectFX {
//    /**
//     * Default generated Serial Version UID.
//     */
//    private static final long serialVersionUID = 1L; // TODO

    /**
     * Default effect name.
     */
    private static final String DEFAULT_NAME = ResourceLoader.getStringRes("drawlinks_default_name");

    /**
     * Default dot size.
     */
    private static final double DEFAULT_SIZE = 5;

    /**
     * Maximum value for the scale factor.
     */
    private static final double MAX_SCALE = 100;
    /**
     * Minimum value for the scale factor.
     */
    private static final double MIN_SCALE = 0;
    /**
     * Range for the scale factor.
     */
    private static final double SCALE_DIFF = MAX_SCALE - MIN_SCALE;
    /**
     * Default value of the scale factor.
     */
    private static final double DEFAULT_SCALE = (SCALE_DIFF) / 2 + MIN_SCALE;

    /**
     * Default {@code Color}.
     */
    private static final Color DEFAULT_COLOR = Color.BLACK;

    private RangedDoubleProperty size = PropertyFactory.getPercentageRangedProperty(ResourceLoader.getStringRes("drawdot_size"), DEFAULT_SIZE);
    private Color color = DEFAULT_COLOR;
    private String name;
    private boolean visibility;

    @Override
    public <T> void apply(GraphicsContext graphic, Environment<T> environment, BidimensionalWormhole wormhole) {

    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void setName(String name) {

    }

    @Override
    public boolean isVisibile() {
        return false;
    }

    @Override
    public void setVisibility(boolean vilibility) {

    }

//    @Override
//    public int hashCode() {
//        // TODO
////        final int prime = 31;
////        int result = 1;
////        result = prime * result + ((color == null) ? 0 : color.hashCode());
////        result = prime * result + ((name == null) ? 0 : name.hashCode());
////        result = prime * result + ((size == null) ? 0 : size.hashCode());
////        result = prime * result + (visibility ? HASHCODE_NUMBER_1 : HASHCODE_NUMBER_2);
////        return result;
//        return 0; // TODO
//    }
//
//    @Override
//    public boolean equals(final Object obj) {
//        // TODO
////        if (this == obj) {
////            return true;
////        }
////        if (obj == null) {
////            return false;
////        }
////        if (getClass() != obj.getClass()) {
////            return false;
////        }
////        final DrawLinks other = (DrawLinks) obj;
////        if (isVisibile() != other.isVisibile()) {
////            return false;
////        }
////        if (getColor() == null) {
////            if (other.getColor() != null) {
////                return false;
////            }
////        } else if (!getColor().equals(other.getColor())) {
////            return false;
////        }
////        if (getName() == null) {
////            if (other.getName() != null) {
////                return false;
////            }
////        } else if (!getName().equals(other.getName())) {
////            return false;
////        }
////        if (getSize() == null) {
////            if (other.getSize() != null) {
////                return false;
////            }
////        } else if (!getSize().equals(other.getSize())) {
////            return false;
////        }
////        return true;
//        return true; // TODO
//    }
}
