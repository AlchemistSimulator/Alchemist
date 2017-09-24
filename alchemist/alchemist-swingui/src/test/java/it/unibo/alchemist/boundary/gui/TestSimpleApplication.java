package it.unibo.alchemist.boundary.gui;

import it.unibo.alchemist.boundary.gui.utility.FXResourceLoader;
import it.unibo.alchemist.boundary.gui.utility.ResourceLoader;
import it.unibo.alchemist.boundary.gui.utility.SVGImageUtils;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class TestSimpleApplication extends Application {
    private static final String ROOT_LAYOUT = "TestSimpleLayout";

    @Override
    public void start(final Stage primaryStage) throws Exception {
        primaryStage.setTitle(ResourceLoader.getStringRes("main_title"));
        primaryStage.getIcons().add(SVGImageUtils.getSvgImage("/icon/TestSimpleIcon.svg"));
        primaryStage.setScene(new Scene(new Pane()));
        primaryStage.setScene(new Scene(FXResourceLoader.getLayout(AnchorPane.class, this, ROOT_LAYOUT)));
        primaryStage.show();
    }

}
