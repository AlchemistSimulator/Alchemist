package it.unibo.alchemist.boundary.gui.view.properties;

import it.unibo.alchemist.SupportedIncarnations;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;

import it.unibo.alchemist.boundary.gui.view.properties.RangedDoubleProperty; // needed by Intellij IDEA to parse Xtend class

/**
 * Factory for custom {@code Property}.
 */
public final class PropertyFactory {
    private static final double COLOR_MAX_VALUE = 255.0;
    private static final double COLOR_MIN_VALUE = 0.0;
    private static final double PERCENTAGE_MAX_VALUE = 100.0;
    private static final double PERCENTAGE_MIN_VALUE = 0.0;

    /**
     * Default, empty, constructor, as this is an utility class.
     */
    private PropertyFactory() {
        // Empty constructor
    }

    /**
     * Returns a new {@link RangedDoubleProperty} with range between 255 and 0
     * and a name that identifies the color channel.
     * 
     * @param channel
     *            the name to give to the {@code Property}
     * @return the {@link DoubleProperty}
     */
    public static RangedDoubleProperty getColorChannelProperty(final String channel) {
        return new RangedDoubleProperty(channel, COLOR_MIN_VALUE, COLOR_MAX_VALUE);
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
    public static RangedDoubleProperty getColorChannelProperty(final String channel, final double value) {
        return new RangedDoubleProperty(channel, value, COLOR_MIN_VALUE, COLOR_MAX_VALUE);
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
        return new RangedDoubleProperty(name, PERCENTAGE_MIN_VALUE, PERCENTAGE_MAX_VALUE);
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
    public static RangedDoubleProperty getPercentageRangedProperty(final String name, final double value) {
        return new RangedDoubleProperty(name, value, PERCENTAGE_MIN_VALUE, PERCENTAGE_MAX_VALUE);
    }

    /**
     * Returns a new {@link ListProperty} with all available incarnations of
     * Alchemist found via reflection.
     * 
     * @param name
     *            the name to give to the property
     * @return the {@code ListProperty}
     */
    public static ListProperty<String> getIncarnationsListProperty(final String name) {
        return new SimpleListProperty<>(null, name, FXCollections.observableArrayList(SupportedIncarnations.getAvailableIncarnations()));
    }

    /**
     * Returns a new {@link SetProperty} with all available incarnations of
     * Alchemist found via reflection.
     * 
     * @param name
     *            the name to give to the property
     * @return the {@code SetProperty}
     */
    public static SetProperty<String> getIncarnationsSetProperty(final String name) {
        return new SimpleSetProperty<>(null, name, FXCollections.observableSet(SupportedIncarnations.getAvailableIncarnations()));
    }
}
