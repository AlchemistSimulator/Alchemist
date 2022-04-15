package it.unibo.alchemist.boundary.gui.view;

import it.unibo.alchemist.boundary.fxui.util.FXResourceLoader;
import it.unibo.alchemist.boundary.fxui.util.SingleRunApp;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import static it.unibo.alchemist.boundary.fxui.util.SingleRunApp.ROOT_LAYOUT;

/**
 * Dummy {@link Application} class.
 */
public class TestApplication extends Application {

    /**
     * {@inheritDoc}
     * <p>
     * It does nothing.
     */
    @Override
    public void start(final Stage primaryStage) throws Exception {
        // Do nothing
        primaryStage.setTitle("Test application");
        final AnchorPane rootLayout = FXResourceLoader.getLayout(this, ROOT_LAYOUT);
        final StackPane main = (StackPane) rootLayout.getChildren().get(0);
        final Button button = new Button("Open from another thread");
        button.setOnAction(event -> new Thread(() -> Platform.runLater(() -> {
            final SingleRunApp<?, ?> app = new SingleRunApp<>();
            // TODO app.setParams(buildParams());
            app.start(new Stage());
        })).start());
        final Button button2 = new Button("Open from JFX thread");
        button2.setOnAction(event -> {
            final SingleRunApp<?, ?> app = new SingleRunApp<>();
            // TODO app.setParams(buildParams());
            app.start(new Stage());
        });
        main.getChildren().add(new HBox(button, button2));
        primaryStage.setScene(new Scene(rootLayout));
        primaryStage.show();
    }
}
