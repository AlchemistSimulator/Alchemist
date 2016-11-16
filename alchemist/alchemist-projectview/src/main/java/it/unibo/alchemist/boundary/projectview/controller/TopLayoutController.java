package it.unibo.alchemist.boundary.projectview.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.alchemist.boundary.l10n.LocalizedResourceBundle;
import it.unibo.alchemist.boundary.projectview.ProjectGUI;
import it.unibo.alchemist.boundary.projectview.model.Batch;
import it.unibo.alchemist.boundary.projectview.model.Output;
import it.unibo.alchemist.boundary.projectview.model.Project;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    private String pathFolder;
    private Project project;

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
     * 
     */
    @FXML
    public void clickNew() {
        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(ProjectGUI.class.getResource("view/NewProjLayoutFolder.fxml"));
        try {
            final AnchorPane pane = (AnchorPane) loader.load();

            final Stage stage = new Stage();
            stage.setTitle(RESOURCES.getString("new_proj"));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(this.main.getStage());
            final Scene scene = new Scene(pane);
            stage.setScene(scene);

            final NewProjLayoutFolderController ctrl = loader.getController();
            ctrl.setMain(this.main);
            ctrl.setStage(stage);

            stage.showAndWait();
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
                this.pathFolder = dir.getAbsolutePath();
                this.ctrlLeft.setTreeView(dir);
                this.btnSave.setDisable(false);
                this.project = ProjectIOUtils.loadFrom(this.pathFolder);
                try {
                    project.filterVariables();
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (!this.project.getSimulation().isEmpty()) {
                    if (!new File(this.project.getBaseDirectory() + File.separator + this.project.getSimulation()).exists()) {
                        setAlert(RESOURCES.getString("file_not_found"), RESOURCES.getString("file_yaml_not_found_header"), RESOURCES.getString("file_not_found_content"));
                    } else {
                        this.ctrlCenter.setSimulationFilePath(this.project.getSimulation());
                    }
                }
                this.ctrlCenter.setEndTime(this.project.getEndTime());
                if (!this.project.getEffect().isEmpty()) {
                    if (!new File(this.project.getBaseDirectory() + File.separator + this.project.getEffect()).exists()) {
                        setAlert(RESOURCES.getString("file_not_found"), RESOURCES.getString("file_json_not_found_header"), RESOURCES.getString("file_not_found_content"));
                    } else {
                        this.ctrlCenter.setEffect(this.project.getEffect());
                    }
                }
                this.ctrlCenter.setSwitchOutputSelected(this.project.getOutput().isSelected());
                if (this.project.getOutput().isSelected()) {
                    if (!this.project.getOutput().getFolder().isEmpty()) {
                        if (!new File(this.project.getBaseDirectory() + File.separator + this.project.getOutput().getFolder()).exists()) {
                            setAlert(RESOURCES.getString("folder_not_found"), RESOURCES.getString("folder_not_found_header"), RESOURCES.getString("folder_not_found_content"));
                            this.ctrlCenter.setSwitchOutputSelected(false);
                        } else {
                            this.ctrlCenter.setOutputFolder(this.project.getOutput().getFolder());
                        }
                    }
                    this.ctrlCenter.setBaseName(this.project.getOutput().getBaseName());
                    this.ctrlCenter.setSamplInterval(this.project.getOutput().getSampleInterval());
                }
                this.ctrlCenter.setSwitchBatchSelected(this.project.getBatch().isSelected());
                if (this.project.getBatch().isSelected()) {
                    //TODO: set variables selected and all variables of yaml file.
                    this.ctrlCenter.setNumberThreads(this.project.getBatch().getThreadCount());
                }
                if (!this.project.getClasspath().isEmpty()) {
                    final ObservableList<String> list = FXCollections.observableArrayList();
                    for (final String lib : this.project.getClasspath()) {
                        if (!new File(this.project.getBaseDirectory() + File.separator + lib).exists()) {
                            setAlert(RESOURCES.getString("library_not_found"), lib + " " + RESOURCES.getString("library_not_found_header"), RESOURCES.getString("library_not_found_content"));
                        } else {
                            list.add(lib);
                        }
                    }
                    this.ctrlCenter.setClasspath(list);
                }
                this.ctrlCenter.setEnableGrid();
                this.ctrlLeft.setEnableRun();

                this.main.getWatcher().registerPath(this.pathFolder);
                new Thread(this.main.getWatcher(), "WatcherProjectView").start();
            }
        }
    }

    /**
     * 
     */
    @FXML
    public void clickSave() {
        final Output out = new Output();
        out.setSelected(this.ctrlCenter.isSwitchOutputSelected());
        out.setFolder(this.ctrlCenter.getOutputFolder());
        out.setBaseName(this.ctrlCenter.getBaseName());
        out.setSampleInterval(this.ctrlCenter.getSamplInterval());
        this.project.setOutput(out);

        final Batch batch = new Batch();
        batch.setSelected(this.ctrlCenter.isSwitchBatchSelected());
        batch.setVariables(new HashMap<String, Boolean>()); // TODO: change
        batch.setThreadCount(this.ctrlCenter.getNumberThreads());
        this.project.setBatch(batch);

        this.project.setSimulation(this.ctrlCenter.getSimulationFilePath());
        this.project.setEffect(this.ctrlCenter.getEffect());
        this.project.setEndTime(this.ctrlCenter.getEndTime());
        final List<String> classpathList = Collections.unmodifiableList(ctrlCenter.getClasspath());
        this.project.setClasspath(classpathList);

        ProjectIOUtils.saveTo(project, pathFolder);
    }

    private void setAlert(final String title, final String header, final String content) {
        final Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

}
