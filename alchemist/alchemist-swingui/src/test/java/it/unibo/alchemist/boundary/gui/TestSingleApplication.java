package it.unibo.alchemist.boundary.gui;

import it.unibo.alchemist.boundary.gui.controller.ButtonsBarController;
import it.unibo.alchemist.boundary.gui.utility.FXResourceLoader;
import it.unibo.alchemist.boundary.gui.utility.ResourceLoader;
import it.unibo.alchemist.boundary.gui.utility.SVGImageUtils;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class TestSingleApplication extends Application {
    /**
     * Main layout without nested layouts. Must inject eventual other nested layouts.
     */
    private static final String ROOT_LAYOUT = "RootLayout";

    private static final Logger L = LoggerFactory.getLogger(TestSingleApplication.class);

    private Pane rootLayout;
    private ButtonsBarController buttonsBarController;
    private Canvas canvas;

    /**
     * Method that launches the application.
     *
     * @param args arguments
     */
    public static void main(final String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(final Stage primaryStage) {
        final Parameters parameters = getParameters();

        final StringBuilder rawParamsBuilder = new StringBuilder("Parameters: ").append('\n');

        parameters.getRaw().forEach(p -> {
            rawParamsBuilder.append(p);
            rawParamsBuilder.append('\n');
        });

        L.debug(rawParamsBuilder.toString());

        primaryStage.setTitle(ResourceLoader.getStringRes("main_title"));

        try {
            initRootLayout(new Canvas());
        } catch (final IOException e) {
            throw new IllegalStateException("Could not initialize RootLayout", e);
        }

        primaryStage.getIcons().add(SVGImageUtils.getSvgImage("/icon/icon.svg"));
        primaryStage.setScene(new Scene(this.rootLayout));
        primaryStage.show();
    }

    /**
     * Initializes application layout using a specified canvas.
     *
     * @throws IOException if it cannot find default layout file
     */
    private void initRootLayout(final Canvas canvas) throws IOException, IllegalArgumentException {
        this.canvas = canvas;
        this.rootLayout = FXResourceLoader.getLayout(AnchorPane.class, this, ROOT_LAYOUT);
        final StackPane main = (StackPane) this.rootLayout.getChildren().get(0);
        main.getChildren().add(canvas);
        buttonsBarController = new ButtonsBarController();
        main.getChildren().add(FXResourceLoader.getLayout(BorderPane.class, buttonsBarController, "ButtonsBarLayout"));
    }
}
