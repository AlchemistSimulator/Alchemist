package it.unibo.alchemist.boundary.gui;

import java.io.IOException;
import java.util.Optional;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

public class FXResourceLoader {

    /**
     * Enumeration for default JavaFX layouts used for this module.
     */
    public enum DefaultLayout {
        /**
         * The root layout.
         */
        ROOT_LAYOUT("RootLayout");

        private String defaultLayoutName;

        DefaultLayout(final String name) {
            this.defaultLayoutName = name;
        }

        /**
         * Getter method for default layout name.
         * 
         * @return the default layout name
         */
        public String getName() {
            return this.defaultLayoutName;
        }
    }

    private static final String XML_RESOURCE_PATH = "/it/unibo/alchemist/gui/view/";
    private static final String EXTENSION = ".fxml";

    private final FXMLLoader loader;
    private Optional<String> layoutName;

    /**
     * Default constructor.
     */
    public FXResourceLoader() {
        this("");
    }

    /**
     * The same as call the default constructor and
     * {@link #setLayoutName(String)}.
     * 
     * @param layoutName
     *            the layout name; if null or empty String, the parameter will
     *            be unset.
     */
    public FXResourceLoader(final String layoutName) {
        this.loader = new FXMLLoader();
        this.setLayoutName(layoutName);
    }

    /**
     * This method returns the layout previously specified.
     * 
     * @param paneInstance
     *            the specific instance of the layout
     * @return the layout
     * @throws NoLayoutSpecifiedException
     *             if no layout was specified
     * @throws IOException
     *             if it can't find the .fxml layout file
     * @param <T>
     *            the type of Pane to get
     */
    @SuppressWarnings("unchecked") // Calling this specifying wrong class type
                                   // would be stupid
    public <T extends Pane> T getLayout(final Class<T> paneInstance) throws NoLayoutSpecifiedException, IOException {
        loader.setLocation(this.getClass().getResource(
                new StringBuilder(XML_RESOURCE_PATH).append(getLayoutName()).append(EXTENSION).toString()));

        return (T) loader.load();
    }

    /**
     * This method returns the default FXMLLoader.
     * 
     * @return the FXMLLoader instance
     */
    public FXMLLoader getLoader() {
        return this.loader;
    }

    /**
     * This method sets the current layout name. If already set, it will be
     * overwritten.
     * 
     * @param layoutName
     *            the layout name; if null or empty String, the paramether will
     *            be unset
     */
    public void setLayoutName(final String layoutName) {
        if (layoutName == null || layoutName.equals("")) {
            this.unsetLayoutName();
        } else {
            this.layoutName = Optional.of(layoutName);
        }
    }

    /**
     * This method unsets the current layout.
     */
    public void unsetLayoutName() {
        this.layoutName = Optional.empty();
    }

    /**
     * This method returns the current layout, if any.
     * 
     * @return the layout, if any
     * @throws NoLayoutSpecifiedException
     *             if no layout was previously specified
     */
    public String getLayoutName() throws NoLayoutSpecifiedException {
        return this.layoutName.orElseThrow(NoLayoutSpecifiedException::new);
    }

}
