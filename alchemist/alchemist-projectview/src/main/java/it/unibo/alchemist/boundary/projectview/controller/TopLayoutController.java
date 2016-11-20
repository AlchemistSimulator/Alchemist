package it.unibo.alchemist.boundary.projectview.controller;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.alchemist.boundary.l10n.LocalizedResourceBundle;
import it.unibo.alchemist.boundary.projectview.ProjectGUI;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * 
 *
 */
public class TopLayoutController {

    private static final Logger L = LoggerFactory.getLogger(ProjectGUI.class);
    private static final ResourceBundle RESOURCES = LocalizedResourceBundle.get("it.unibo.alchemist.l10n.ProjectViewUIStrings");

    @FXML
    private Button btnNew;
    @FXML
    private Button btnOpen;
    /*@FXML
    private Button btnImport;*/
    @FXML
    private Button btnSave;
    /*@FXML
    private Button btnSaveAs;*/

    private CenterLayoutController ctrlCenter;
    private ProjectGUI main;
    private LeftLayoutController ctrlLeft;
    private Watcher watcher;

    /**
     * 
     */
    public void initialize() {
        this.btnNew.setText(RESOURCES.getString("new"));
        this.btnOpen.setText(RESOURCES.getString("open"));
        //this.btnImport.setText(RESOURCES.getString("import"));
        this.btnSave.setText(RESOURCES.getString("save"));
        //this.btnSaveAs.setText(RESOURCES.getString("save_as"));
        this.btnSave.setDisable(true);
    }

    /**
     * Sets the main class.
     * @param main main class
     */
    public void setMain(final ProjectGUI main) {
        this.main = main;
    }

    /**
     * 
     * @param controller LeftLayout controller
     */
    public void setCtrlLeft(final LeftLayoutController controller) {
        this.ctrlLeft = controller;
    }

    /**
     * 
     * @param controller CenterLayout controller
     */
    public void setCtrlCenter(final CenterLayoutController controller) {
        this.ctrlCenter = controller;
    }

    /**
     * Terminates the watcher.
     */
    public void terminateWatcher() {
        if (this.watcher != null) {
            this.watcher.terminate();
        }
    }

    /**
     * 
     */
    @FXML
    public void clickNew() {
        if (this.ctrlCenter.getProject() != null) {
            this.ctrlCenter.checkChanges();
        }
        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(ProjectGUI.class.getResource("view/NewProjLayoutFolder.fxml"));
        try {
            final AnchorPane pane = (AnchorPane) loader.load();
            final Stage stage = new Stage();
            stage.setTitle(RESOURCES.getString("new_proj"));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(this.main.getStage());
            final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            final double width = screenSize.getWidth() * 20.83 / 100;
            final double height = screenSize.getHeight() * 13.89 / 100;
            final Scene scene = new Scene(pane, width, height);
            stage.setScene(scene);
            final NewProjLayoutFolderController ctrl = loader.getController();
            ctrl.setMain(this.main);
            ctrl.setStage(stage);
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(final WindowEvent event) {
                    ctrl.setFolderPath(null);
                }
            });
            stage.showAndWait();
            if (ctrl.getFolderPath() != null) {
                setView(new File(ctrl.getFolderPath()));
            }
        } catch (IOException e) {
            L.error("Error loading the graphical interface. This is most likely a bug.", e);
            System.exit(1);
        }
    }

    /**
     * 
     */
    @FXML
    public void clickOpen() {
        if (this.ctrlCenter.getProject() != null) {
            this.ctrlCenter.checkChanges();
        }
        final DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle(RESOURCES.getString("select_folder_proj"));
        dirChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        final File dir = dirChooser.showDialog(this.main.getStage());
        if (dir != null) {
            final int containsFile =  dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(final File dir, final String filename) {
                    return filename.endsWith(".alchemist_project_descriptor.json");
                }
            }).length;
            if (containsFile == 0) {
                final Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle(RESOURCES.getString("proj_folder_wrong"));
                alert.setHeaderText(RESOURCES.getString("proj_folder_wrong_header"));
                alert.setContentText(RESOURCES.getString("proj_folder_wrong_content"));
                alert.showAndWait();
            } else {
                setView(dir);
            }
        }
    }

    /**
     * 
     */
    @FXML
    public void clickSave() {
        this.ctrlCenter.saveProject();
    }

    private void setView(final File dir) {
        final String pathFolder = dir.getAbsolutePath();
        this.ctrlLeft.setTreeView(dir);
        this.btnSave.setDisable(false);
        this.ctrlCenter.setField();
        if (this.watcher == null) {
            this.watcher = createWatcher();
        } else if (this.watcher.isWatcherAlive() && !this.watcher.getFolderPath().equals(pathFolder)) {
            terminateWatcher();
            this.watcher = createWatcher();
        }
        this.watcher.registerPath(pathFolder);
        new Thread(this.watcher, "WatcherProjectView").start();
    }

    private Watcher createWatcher() {
        return new Watcher(this.ctrlLeft, this.ctrlCenter);
    }

}
