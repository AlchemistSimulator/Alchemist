package it.unibo.alchemist.boundary.gui;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import jiconfont.IconCode;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconFontFX;
import jiconfont.javafx.IconNode;

/**
 * Utility class for loading resources related to layout.
 */
public final class FXResourceLoader {
    private static final String XML_RESOURCE_PATH = "/it/unibo/alchemist/gui/view/";
    private static final String EXTENSION = ".fxml";

    /**
     * Private, empty, constructor.
     */
    private FXResourceLoader() {
    }

    /**
     * Loads a layout FXML file returning the base pane defined by the layout.
     * 
     * @param <T>
     *            the generic type of pane
     * @param paneInstance
     *            the class of pain to load
     * @param controller
     *            the controller to associate to that layout
     * @param layoutName
     *            the name of the layout; it should be the file name without
     *            extension
     * @return the pane defined by the layout
     * @throws IOException
     *             if it cannot load the file for some reason
     */
    @SuppressWarnings("unchecked") // Passing a wrong class would be stupid
    public static <T extends Pane> T getLayout(final Class<T> paneInstance, final Object controller, final String layoutName)
            throws IOException {
        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(FXResourceLoader.class.getResource(XML_RESOURCE_PATH + layoutName + EXTENSION));
        loader.setController(controller);
        return (T) loader.load();
    }

    /**
     * Returns the standard message for a problem in class injection from FXML.
     * 
     * @param nodeName
     *            the name of the node that was not injected
     * @param layoutFileName
     *            the layout file name
     * @return the message String
     */
    public static String getInjectionErrorMessage(final String nodeName, final String layoutFileName) {
        return "fx:id=\"" + nodeName + "\" was not injected: check your FXML file \"" + layoutFileName + "\"";

    }

    /**
     * Loads an icon from Google Material Design Icons filled in white.
     * 
     * @param iconCode
     *            the IconCode (from {@link GoogleMaterialDesignIcons}
     * @return the IconNode with the specified icon
     */
    public static IconNode getWhiteIcon(final IconCode iconCode) {
        IconFontFX.register(GoogleMaterialDesignIcons.getIconFont());

        final IconNode icon = new IconNode(iconCode);
        icon.setFill(Color.WHITE);
        return icon;
    }

}
