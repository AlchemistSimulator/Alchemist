package it.unibo.alchemist.boundary.gui;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import jiconfont.IconCode;
import jiconfont.javafx.IconNode;

public final class FXResourceLoader {
    private static final String XML_RESOURCE_PATH = "/it/unibo/alchemist/gui/view/";
    private static final String EXTENSION = ".fxml";

    /**
     * Private, empty, constructor.
     */
    private FXResourceLoader() {
    }

    /**
     * The same as call the default constructor and
     * {@link #setLayoutName(String)}.
     * 
     * @param layoutName
     *            the layout name; if null or empty String, the parameter will
     *            be unset.
     * @throws IOException
     */
    public static <T extends Pane> T getLayout(final Class<T> paneInstance, final Object controller, final String layoutName)
            throws IOException {
        FXMLLoader loader = new FXMLLoader();
        // this.setLayoutName(layoutName);
        loader.setLocation(FXResourceLoader.class.getResource(XML_RESOURCE_PATH + layoutName + EXTENSION));
        loader.setController(controller);
        return (T) loader.load();
    }

    public static String getInjectionErrorMessage(final String nodeName, final String layoutFileName) {
        return new StringBuilder("fx:id=\"").append(nodeName).append("\" was not injected: check your FXML file '").append(layoutFileName)
                .append("'").toString();

    }

    public static IconNode getWhiteIcon(final IconCode iconCode) {
        IconNode icon = new IconNode(iconCode);
        icon.setFill(Color.WHITE);
        return icon;
    }

}
