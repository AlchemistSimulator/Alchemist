package it.unibo.alchemist.boundary.projectview.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.unibo.alchemist.boundary.l10n.LocalizedResourceBundle;
import it.unibo.alchemist.boundary.projectview.ProjectGUI;
import it.unibo.alchemist.boundary.projectview.model.BatchImpl;
import it.unibo.alchemist.boundary.projectview.model.OutputImpl;
import it.unibo.alchemist.boundary.projectview.model.ProjectImpl;
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
                final Gson gson = new Gson();
                try {
                    final BufferedReader br = new BufferedReader(new FileReader(this.pathFolder + File.separator + ".alchemist_project_descriptor.json"));
                    try {
                        if (br.ready()) {
                            final ProjectImpl proj = gson.fromJson(br, ProjectImpl.class);
                            if (!proj.getSimulation().isEmpty()) {
                                this.ctrlCenter.setSimulation(proj.getSimulation());
                            }
                            this.ctrlCenter.setEndTime(proj.getEndTime());
                            if (!proj.getEffect().isEmpty()) {
                                this.ctrlCenter.setEffect(proj.getEffect());
                            }
                            this.ctrlCenter.setSwitchOutputSelected(proj.getOutput().isSelect());
                            if (proj.getOutput().isSelect()) {
                                this.ctrlCenter.setOutputFolder(proj.getOutput().getFolder());
                                this.ctrlCenter.setBaseName(proj.getOutput().getBaseName());
                                this.ctrlCenter.setSamplInterval(proj.getOutput().getSamplInterval());
                            }
                            this.ctrlCenter.setSwitchBatchSelected(proj.getBatch().isSelected());
                            if (proj.getBatch().isSelected()) {
                                //TODO: set variables selected and all variables of yaml file.
                                this.ctrlCenter.setNumberThreads(proj.getBatch().getThreadCount());
                            }
                            if (!proj.getClasspath().isEmpty()) {
                                final ObservableList<String> list = FXCollections.observableArrayList();
                                for (final String lib : proj.getClasspath()) {
                                    list.add(lib);
                                }
                                this.ctrlCenter.setClasspath(list);
                            }
                        }
                    } catch (IOException e) {
                        L.error("I/O error. This is most likely a bug.", e);
                    }
                } catch (FileNotFoundException e) {
                    L.error("Error reading the file. This is most likely a bug.", e);
                }
            }
        }
    }

    /**
     * 
     */
    @FXML
    public void clickSave() {
        final OutputImpl out = new OutputImpl();
        out.setSelect(this.ctrlCenter.isSwitchOutputSelected());
        out.setFolder(this.ctrlCenter.getOutputFolder());
        out.setBaseName(this.ctrlCenter.getBaseName());
        out.setSamplInterval(this.ctrlCenter.getSamplInterval());

        final BatchImpl batch = new BatchImpl();
        batch.setSelect(this.ctrlCenter.isSwitchBatchSelected());
        batch.setVariables(new ArrayList<String>()); // TODO: change
        batch.setThreadCount(this.ctrlCenter.getNumberThreads());

        final List<String> classpathList = new ArrayList<>();
        for (final String s: this.ctrlCenter.getClasspath()) {
            classpathList.add(s);
        }

        final ProjectImpl proj = new ProjectImpl();
        proj.setSimulation(this.ctrlCenter.getSimulationFilePath());
        proj.setEndTime(this.ctrlCenter.getEndTime());
        proj.setEffect(this.ctrlCenter.getEffect());
        proj.setOutput(out);
        proj.setBatch(batch);
        proj.setClasspath(classpathList);

        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final String json = gson.toJson(proj);
        try {
            final FileWriter writer = new FileWriter(this.pathFolder + File.separator + ".alchemist_project_descriptor.json");
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            L.error("Error writing the file. This is most likely a bug.", e);
        }
    }

}
