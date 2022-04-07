/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.util;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import jiconfont.IconCode;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconFontFX;
import jiconfont.javafx.IconNode;
import org.kaikikm.threadresloader.ResourceLoader;

/**
 * Utility class for loading resources related to layout.
 */
public final class FXResourceLoader {
    private static final String XML_RESOURCE_PATH = "it/unibo/alchemist/gui/view/";
    private static final String LAYOUT_EXTENSION = ".fxml";
    private static final String STYLE_EXTENSION = ".css";

    /**
     * Private, empty, constructor.
     */
    private FXResourceLoader() {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    /**
     * Loads a layout FXML file returning the base pane defined by the layout.
     *
     * @param <T>          the generic type of pane
     * @param controller   the controller to associate to that layout
     * @param layoutName   the name of the layout; it should be the file name without
     *                     extension
     * @return the pane defined by the layout
     * @throws IOException if it cannot load the file for some reason
     */
    @SuppressWarnings("unchecked") // Passing a wrong class would be stupid
    public static <T extends Node> T getLayout(
            final Object controller,
            final String layoutName
    ) throws IOException {
        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(ResourceLoader.getResource(XML_RESOURCE_PATH + layoutName + LAYOUT_EXTENSION));
        loader.setController(controller);
        return (T) loader.load();
    }

    /**
     * Loads a layout FXML file returning the base pane defined by the layout.
     * It doesn't set any controller.
     *
     * @param <T>          the generic type of pane
     * @param layoutName   the name of the layout; it should be the file name without
     *                     extension
     * @return the pane defined by the layout
     * @throws IOException if it cannot load the file for some reason
     */
    public static <T extends Node> T getLayout(final String layoutName)
            throws IOException {
        return getLayout(null, layoutName);
    }

    /**
     * Returns the standard message for a problem in class injection from FXML.
     *
     * @param nodeName       the name of the node that was not injected
     * @param layoutFileName the layout file name
     * @return the message String
     */
    public static String getInjectionErrorMessage(final String nodeName, final String layoutFileName) {
        return "fx:id=\"" + nodeName + "\" was not injected: check your FXML file \"" + layoutFileName + "\"";
    }

    /**
     * Loads an icon from Google Material Design Icons filled in white.
     *
     * @param iconCode the IconCode (from {@link GoogleMaterialDesignIcons})
     * @return the IconNode with the specified icon
     */
    public static IconNode getWhiteIcon(final IconCode iconCode) {
        return getColoredIcon(iconCode, Color.WHITE);
    }

    /**
     * Loads an icon from Google Material Design Icons.
     *
     * @param iconCode the IconCode (from {@link GoogleMaterialDesignIcons})
     * @param color    the color to fill the icon with
     * @return the IconNode with the specified icon
     */
    public static IconNode getColoredIcon(final IconCode iconCode, final Color color) {
        IconFontFX.register(GoogleMaterialDesignIcons.getIconFont());
        final IconNode icon = new IconNode(iconCode);
        icon.setFill(color);
        return icon;
    }

    /**
     * Returns the {@link java.net.URL} to a CSS stylesheet of given name in string form.
     *
     * @param cssFileName the CSS file name without extension
     * @return the path
     * @see javafx.scene.Parent#getStylesheets()
     */
    public static String getStyle(final String cssFileName) {
        return ResourceLoader.getResource(XML_RESOURCE_PATH + cssFileName + STYLE_EXTENSION).toExternalForm();
    }
}
