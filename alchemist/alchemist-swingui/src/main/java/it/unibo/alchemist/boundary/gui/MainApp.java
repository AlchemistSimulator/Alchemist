package it.unibo.alchemist.boundary.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainApp extends Application {
    private Stage primaryStage;
    private FXResourceLoader loader;
    private BorderPane rootLayout;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Alchemist Simulator base UI");
        
        this.initRootLayout();
        this.primaryStage.setScene(new Scene(this.rootLayout, 640, 480));
        this.primaryStage.show();
    }

    private void initRootLayout() throws Exception {
        if (this.loader == null) {
            this.loader = new FXResourceLoader();
        }

        this.loader.setLayoutName(FXResourceLoader.DefaultLayout.ROOT_LAYOUT.getName());
        this.rootLayout = this.loader.getLayout(BorderPane.class);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
