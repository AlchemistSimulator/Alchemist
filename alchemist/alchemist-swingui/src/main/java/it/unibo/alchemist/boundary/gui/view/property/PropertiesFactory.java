package it.unibo.alchemist.boundary.gui.view.property;

import it.unibo.alchemist.SupportedIncarnations;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;

/**
 * Factory for custom {@code Property}.
 */
public final class PropertiesFactory {

    /**
     * Returns a new {@link RangedDoublePropertyOld} with range between 255 and 0
     * and a name that identifies the color channel.
     * 
     * @param channel
     *            the name to give to the {@code Property}
     * @return the {@link DoubleProperty}
     */
    public static RangedDoublePropertyOld getColorChannelProperty(final String channel) {
        return new ColorChannelDoubleProperty(channel);
    }

    /**
     * Returns a new {@link RangedDoublePropertyOld} with range between 255 and 0
     * and a name that identifies the color channel.
     * 
     * @param channel
     *            the name to give to the {@code Property}
     * @param value
     *            the initial value to give to the {@code Property}
     * @return the {@link DoubleProperty}
     */
    public static RangedDoublePropertyOld getColorChannelProperty(final String channel, final Double value) {
        return new ColorChannelDoubleProperty(channel, value);
    }

    /**
     * Returns a new {@link RangedDoublePropertyOld} with range between 100 and 0
     * and a name that identifies the {@code Property}.
     * 
     * @param name
     *            the name to give to the {@code Property}
     * @return the {@link DoubleProperty}
     */
    public static RangedDoublePropertyOld getPercentageRangedProperty(final String name) {
        return new PercentageDoubleProperty(name);
    }

    /**
     * Returns a new {@link RangedDoublePropertyOld} with range between 100 and 0
     * and a name that identifies the {@code Property}.
     * 
     * @param name
     *            the name to give to the {@code Property}
     * @param value
     *            the initial value to give to the {@code Property}
     * @return the {@link DoubleProperty}
     */
    public static RangedDoublePropertyOld getPercentageRangedProperty(final String name, final Double value) {
        return new PercentageDoubleProperty(name, value);
    }

    /**
     * Returns a new {@link ListProperty} with all available incarnations of Alchemist found via reflection.
     * @param name the name to give to the property
     * @return the {@code ListProperty}
     */
    public static ListProperty<String> getIncarnationsListProperty(final String name) {
        return new SimpleListProperty<>(null, name, FXCollections.observableArrayList(SupportedIncarnations.getAvailableIncarnations()));
    }

    /**
     * Returns a new {@link SetProperty} with all available incarnations of Alchemist found via reflection.
     * @param name the name to give to the property
     * @return the {@code SetProperty}
     */
    public static SetProperty<String> getIncarnationsSetProperty(final String name) {
        return new SimpleSetProperty<>(null, name, FXCollections.observableSet(SupportedIncarnations.getAvailableIncarnations()));
    }

    /**
     * Default, empty, constructor, as this is an utility class.
     */
    private PropertiesFactory() {
        // Empty constructor
    }

    /**
     * Instance of {@link RangedDoublePropertyOld} aimed to color channel
     * representation.
     * <p>
     * Default bounds are set between 255 and 0.
     */
    private static class ColorChannelDoubleProperty extends RangedDoublePropertyOld {
        /** Default generated Serial Version UID. */
        private static final long serialVersionUID = 5055891206764667192L;
        private static final Double DEFAULT_MAX_VALUE = 255.0;
        private static final Double DEFAULT_MIN_VALUE = 0.0;

        /**
         * The constructor of {@link SimpleDoubleProperty}.
         * <p>
         * Range is set between 100 and 0.
         * <p>
         * {@code Bean} is set to null.
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
         * Range is set between 100 and 0.
         * <p>
         * {@code Bean} is set to null.
         * 
         * @param name
         *            the name of this {@code DoubleProperty}
         */
        ColorChannelDoubleProperty(final String name) {
            super(null, name, DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
        }

        // @Override
        // public int hashCode() {
        // return super.hashCode();
        // }
        //
        // @Override
        // public boolean equals(final Object obj) {
        // return super.equals(obj);
        // }
    }

    /**
     * Instance of {@link RangedDoublePropertyOld} aimed to color channel
     * representation.
     * <p>
     * Default bounds are set between 100 and 0.
     */
    private static class PercentageDoubleProperty extends RangedDoublePropertyOld {
        /** Default generated Serial Version UID. */
        private static final long serialVersionUID = 4427700087280058938L;
        private static final Double DEFAULT_MAX_VALUE = 100.0;
        private static final Double DEFAULT_MIN_VALUE = 0.0;

        /**
         * The constructor of {@link SimpleDoubleProperty}.
         * <p>
         * Range is set between 100 and 0.
         * <p>
         * {@code Bean} is set to null.
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
         * Range is set between 100 and 0.
         * <p>
         * {@code Bean} is set to null.
         *
         * @param name
         *            the name of this {@code DoubleProperty}
         */
        PercentageDoubleProperty(final String name) {
            super(null, name, DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
        }

        // @Override
        // public int hashCode() {
        // return super.hashCode();
        // }
        //
        // @Override
        // public boolean equals(final Object obj) {
        // return super.equals(obj);
        // }
    }
}
