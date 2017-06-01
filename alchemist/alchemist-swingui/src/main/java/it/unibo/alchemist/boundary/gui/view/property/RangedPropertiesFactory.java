package it.unibo.alchemist.boundary.gui.view.property;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * Factory for {@code DoubleProperties} with a standard range.
 */
public final class RangedPropertiesFactory {

    /**
     * Returns a new {@link RangedDoubleProperty} with range between 255 and 0
     * and a name that identifies the color channel.
     * 
     * @param channel
     *            the name to give to the {@code Property}
     * @return the {@link DoubleProperty}
     */
    public static RangedDoubleProperty getColorChannelProperty(final String channel) {
        return new ColorChannelDoubleProperty(channel);
    }

    /**
     * Returns a new {@link RangedDoubleProperty} with range between 255 and 0
     * and a name that identifies the color channel.
     * 
     * @param channel
     *            the name to give to the {@code Property}
     * @param value
     *            the initial value to give to the {@code Property}
     * @return the {@link DoubleProperty}
     */
    public static RangedDoubleProperty getColorChannelProperty(final String channel, final Double value) {
        return new ColorChannelDoubleProperty(channel, value);
    }

    /**
     * Returns a new {@link RangedDoubleProperty} with range between 100 and 0
     * and a name that identifies the {@code Property}.
     * 
     * @param name
     *            the name to give to the {@code Property}
     * @return the {@link DoubleProperty}
     */
    public static RangedDoubleProperty getPercentageRangedProperty(final String name) {
        return new PercentageDoubleProperty(name);
    }

    /**
     * Returns a new {@link RangedDoubleProperty} with range between 100 and 0
     * and a name that identifies the {@code Property}.
     * 
     * @param name
     *            the name to give to the {@code Property}
     * @param value
     *            the initial value to give to the {@code Property}
     * @return the {@link DoubleProperty}
     */
    public static RangedDoubleProperty getPercentageRangedProperty(final String name, final Double value) {
        return new PercentageDoubleProperty(name, value);
    }

    /**
     * Default, empty, constructor, as this is an utility class.
     */
    private RangedPropertiesFactory() {
        // Empty constructor
    }

    /**
     * Instance of {@link RangedDoubleProperty} aimed to color channel
     * representation.
     * <p>
     * Default bounds are set between 255 and 0.
     */
    private static class ColorChannelDoubleProperty extends RangedDoubleProperty {
        /** Default generated Serial Version UID. */
        private static final long serialVersionUID = 5055891206764667192L;
        private static final Double DEFAULT_MAX_VALUE = 255.0;
        private static final Double DEFAULT_MIN_VALUE = 0.0;

        /**
         * The constructor of {@link SimpleDoubleProperty}.
         * <p>
         * Range is set between 100 and 0; {@code bean} is set to null.
         *
         * @param name
         *            the name of this {@code DoubleProperty}
         * @param initialValue
         *            the initial value of the wrapped value
         */
        ColorChannelDoubleProperty(final String name, final double initialValue) {
            super(null, name, initialValue, DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
        }

        /**
         * The constructor of {@code DoubleProperty}.
         * <p>
         * Range is set between 100 and 0; {@code bean} is set to null.
         * 
         * @param name
         *            the name of this {@code DoubleProperty}
         */
        ColorChannelDoubleProperty(final String name) {
            super(null, name, DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
        }

//        @Override
//        public int hashCode() {
//            return super.hashCode();
//        }
//
//        @Override
//        public boolean equals(final Object obj) {
//            return super.equals(obj);
//        }
    }

    /**
     * Instance of {@link RangedDoubleProperty} aimed to color channel
     * representation.
     * <p>
     * Default bounds are set between 100 and 0.
     */
    private static class PercentageDoubleProperty extends RangedDoubleProperty {
        /** Default generated Serial Version UID. */
        private static final long serialVersionUID = 4427700087280058938L;
        private static final Double DEFAULT_MAX_VALUE = 100.0;
        private static final Double DEFAULT_MIN_VALUE = 0.0;

        /**
         * The constructor of {@link SimpleDoubleProperty}.
         * <p>
         * Range is set between 100 and 0; {@code bean} is set to null.
         *
         * @param name
         *            the name of this {@code DoubleProperty}
         * @param initialValue
         *            the initial value of the wrapped value
         */
        PercentageDoubleProperty(final String name, final double initialValue) {
            super(null, name, initialValue, DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
        }

        /**
         * The constructor of {@code DoubleProperty}.
         * <p>
         * Range is set between 100 and 0; {@code bean} is set to null.
         *
         * @param name
         *            the name of this {@code DoubleProperty}
         */
        PercentageDoubleProperty(final String name) {
            super(null, name, DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
        }

//        @Override
//        public int hashCode() {
//            return super.hashCode();
//        }
//
//        @Override
//        public boolean equals(final Object obj) {
//            return super.equals(obj);
//        }
    }
}
