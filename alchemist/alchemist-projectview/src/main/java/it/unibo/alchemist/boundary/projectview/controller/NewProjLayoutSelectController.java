package it.unibo.alchemist.boundary.projectview.controller;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.alchemist.boundary.l10n.LocalizedResourceBundle;
import it.unibo.alchemist.boundary.projectview.ProjectGUI;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * 
 *
 */
public class NewProjLayoutSelectController {

    private static final Logger L = LoggerFactory.getLogger(ProjectGUI.class);
    private static final ResourceBundle RESOURCES = LocalizedResourceBundle.get("it.unibo.alchemist.l10n.ProjectViewUIStrings");
    private static final String FILE_DUMMY = "dummy";

    @FXML
    private Button backBtn;
    @FXML
    private Button finishBtn;
    @FXML
    private ChoiceBox<String> choiceTempl;
    @FXML
    private Label select;

    private ProjectGUI main;
    private String folderPath;
    private String selectedTemplate;
    private Stage stage;
    private final ObservableList<String> templates = FXCollections.observableArrayList();

    /**
     * 
     */
    public void initialize() {
        this.backBtn.setText(RESOURCES.getString("back"));
        this.finishBtn.setText(RESOURCES.getString("finish"));
        this.finishBtn.setDisable(true);
        this.select.setText(RESOURCES.getString("select"));
        final File file = getResourceTemplate("templates/");
        for (final File f : file.listFiles()) {
            this.templates.add(Character.toUpperCase(f.getName().charAt(0)) + f.getName().substring(1));
        }
        this.choiceTempl.setItems(this.templates);
        this.choiceTempl.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
                selectedTemplate = newValue;
                finishBtn.setDisable(false);
            }
        });
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
    public void clickFinish() {
        createFile(this.folderPath, this.selectedTemplate);
        this.stage.close();
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
            ctrl.setFolderPath(this.folderPath);
        } catch (IOException e) {
            L.error("Error loading the graphical interface. This is most likely a bug.", e);
            System.exit(1);
        }
    }

    private void createFile(final String folder, final String folderTempl) {
        final File file = getResourceTemplate("templates/" + folderTempl + "/");
        for (final File f : file.listFiles()) {
            if (f.isDirectory()) {
                new File(folder + File.separator + f.getName()).mkdir();
                createFile(folder + File.separator + f.getName(), folderTempl + "/" + f.getName());
            } else {
                if (!f.getName().equals(FILE_DUMMY)) {
                    try {
                        new File(folder + File.separator + f.getName()).createNewFile();
                    } catch (IOException e) {
                        L.error("I/O error during the creation of new file.", e);
                    }
                }
            }
        }
    }

    private File getResourceTemplate(final String folder) {
        final URL url = ProjectGUI.class.getClassLoader().getResource(folder);
        File file = null;
        try {
            file = new File(url.toURI());
        } catch (URISyntaxException e) {
            file = new File(url.getPath());
        }
        return file;
    }

}
