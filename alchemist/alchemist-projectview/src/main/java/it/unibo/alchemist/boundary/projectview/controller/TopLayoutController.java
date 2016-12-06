package it.unibo.alchemist.boundary.projectview.controller;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.alchemist.boundary.l10n.LocalizedResourceBundle;
import it.unibo.alchemist.boundary.projectview.ProjectGUI;
import it.unibo.alchemist.boundary.projectview.utils.SVGImageUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
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
    private static final double IMG_WIDTH = 2.8646;
    private static final double IMG_HEIGHT = 5.093;

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
        SVGImageUtils.installSvgLoader();
        this.btnNew.setGraphic(new ImageView(SVGImageUtils.getSvgImage("icon/new.svg", IMG_WIDTH, IMG_HEIGHT)));
        this.btnNew.setText(RESOURCES.getString("new"));
        this.btnOpen.setGraphic(new ImageView(SVGImageUtils.getSvgImage("icon/open.svg", IMG_WIDTH, IMG_HEIGHT)));
        this.btnOpen.setText(RESOURCES.getString("open"));
        //this.btnImport.setText(RESOURCES.getString("import"));
        this.btnSave.setGraphic(new ImageView(SVGImageUtils.getSvgImage("icon/save.svg", IMG_WIDTH, IMG_HEIGHT)));
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
     * Show a view to create new project.
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
     * Show a directory chooser to open an existing project.
     */
    @FXML
    public void clickOpen() {
        if (this.ctrlCenter.getProject() != null) {
            this.ctrlCenter.checkChanges();
        }
        final String folderPath = System.getProperty("user.home") + File.separator + ".alchemist" + File.separator;
        if (!new File(folderPath).exists() && !new File(folderPath).mkdirs()) {
            L.error("Error creating the folder to save the Alchemist settings.");
        }
        final String filePath = folderPath + File.separator + "alchemist-settings";
        String pathLastVisited = "";
        try {
            if (!new File(filePath).createNewFile()) {
                try {
                    final BufferedReader br = new BufferedReader(new FileReader(filePath));
                    pathLastVisited = br.readLine();
                    br.close();
                } catch (FileNotFoundException e) {
                    L.error("Error reading settings file. This is most likely a bug.", e);
                } catch (IOException e) {
                    L.error("I/O Error while file was read. This is most likely a bug.", e);
                } 
            }
        } catch (IOException e) {
            L.error("I/O Error while file was create. This is most likely a bug.", e);
        }
        final DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle(RESOURCES.getString("select_folder_proj"));
        dirChooser.setInitialDirectory(new File(pathLastVisited.isEmpty() ? System.getProperty("user.home") : pathLastVisited));
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
                try {
                    final BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
                    bw.write(dir.getAbsolutePath());
                    bw.close();
                } catch (IOException e) {
                    L.error("I/O Error while file was write. This is most likely a bug.", e);
                }
                setView(dir);
            }
        }
    }

    /**
     * Save the project.
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
