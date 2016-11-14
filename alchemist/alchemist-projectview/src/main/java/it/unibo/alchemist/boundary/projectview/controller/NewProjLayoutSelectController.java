package it.unibo.alchemist.boundary.projectview.controller;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;

import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.alchemist.boundary.l10n.ResourceAccess;
import it.unibo.alchemist.boundary.projectview.ProjectGUI;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * 
 *
 */
public class NewProjLayoutSelectController {

    private static final Logger L = LoggerFactory.getLogger(ProjectGUI.class);

    @FXML
    private Button backBtn;
    @FXML
    private Button finishBtn;
    @FXML
    private Label select;

    private ProjectGUI main;
    private String folderPath;
    private Stage stage;

    /**
     * 
     */
    public void initialize() {
        this.backBtn.setText(ResourceAccess.getString("back"));
        this.finishBtn.setText(ResourceAccess.getString("finish"));
        this.finishBtn.setDisable(true);
        this.select.setText(ResourceAccess.getString("select"));

        final Reflections ref = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("templates"))
                .setScanners(new ResourcesScanner()));
        Set<String> set = ref.getResources(Pattern.compile("."));
        //System.out.println("TEMPLATES\nSize: " + set.size());

        /*Collection<URL> coll = ClasspathHelper.forPackage("/icon");
        for (URL url: coll) {
            System.out.println("- " + url.toString());
        }*/

        /*for (String prop: set) {
            System.out.println("- " + prop);
        }*/
    }

    /**
     * 
     * @param main main
     */
    public void setMain(final ProjectGUI main) {
        this.main = main;
    }

    /**
     * 
     * @param stage Stage
     */
    public void setStage(final Stage stage) {
        this.stage = stage;
    }

    /**
     * 
     * @param path Folder path
     */
    public void setFolderPath(final String path) {
        this.folderPath = path;
    }

    /**
     * 
     */
    @FXML
    public void clickBack() {
        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(ProjectGUI.class.getResource("view/NewProjLayoutFolder.fxml"));
        try {
            final AnchorPane pane = (AnchorPane) loader.load();
            final Scene scene = new Scene(pane);
            this.stage.setScene(scene);

            final NewProjLayoutFolderController ctrl = loader.getController();
            ctrl.setMain(this.main);
            ctrl.setStage(this.stage);
            ctrl.setPath(this.folderPath);
        } catch (IOException e) {
            L.error("Error loading the graphical interface. This is most likely a bug.", e);
            System.exit(1);
        }
    }

}
