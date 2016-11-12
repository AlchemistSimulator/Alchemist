package it.unibo.alchemist.controller;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.stream.JsonWriter;

import it.unibo.alchemist.Main;
import it.unibo.alchemist.boundary.l10n.R;
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

    private static final Logger L = LoggerFactory.getLogger(Main.class);

    @FXML
    private Button btnNew;
    @FXML
    private Button btnOpen;
    @FXML
    private Button btnImport;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnSaveAs;

    private CenterLayoutController ctrlCenter;
    private Main main;
    private LeftLayoutController ctrlLeft;
    private String pathFolder;

    /**
     * 
     */
    public void initialize() {
        this.btnNew.setText(R.getString("new"));
        this.btnOpen.setText(R.getString("open"));
        this.btnImport.setText(R.getString("import"));
        this.btnSave.setText(R.getString("save"));
        this.btnSaveAs.setText(R.getString("save_as"));
    }

    /**
     * Sets the main class.
     * @param main main class
     */
    public void setMain(final Main main) {
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
        loader.setLocation(Main.class.getResource("view/NewProjLayoutFolder.fxml"));
        try {
            final AnchorPane pane = (AnchorPane) loader.load();

            final Stage stage = new Stage();
            stage.setTitle(R.getString("new_proj"));
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
    public void clickOpen() { //TODO: read .alchemist_project_descriptor.json and set field
        final DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle(R.getString("select_folder_proj"));
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
                alert.setTitle(R.getString("folder_wrong"));
                alert.setHeaderText(R.getString("folder_wrong_header"));
                alert.setContentText(R.getString("folder_wrong_content"));
                alert.showAndWait();
            } else {
                this.pathFolder = dir.getAbsolutePath();
                this.ctrlLeft.setTreeView(dir);
            }
        }
    }

    /**
     * 
     */
    @FXML
    public void clickSave() {
        JsonWriter json = null;
        try {
            json = new JsonWriter(new FileWriter(this.pathFolder + File.separator + ".alchemist_project_descriptor.json"));
            json.beginObject();
            json.name("Simulation");
            json.value(this.ctrlCenter.getSimulation());
            json.name("EndTime");
            json.value(this.ctrlCenter.getEndTime());
            json.name("Effect");
            json.value(this.ctrlCenter.getEffect());
            json.name("Output");
            json.beginArray();
            json.beginObject();
            json.name("Select");
            json.value(this.ctrlCenter.isSwitchOutputSelected());
            json.name("Folder");
            json.value(!this.ctrlCenter.isSwitchOutputSelected() ? "" : this.ctrlCenter.getOutputFolder());
            json.name("Base name");
            json.value(!this.ctrlCenter.isSwitchOutputSelected() ? "" : this.ctrlCenter.getBaseName());
            json.name("Interval");
            json.value(!this.ctrlCenter.isSwitchOutputSelected() ? -1 : this.ctrlCenter.getSamplInterval());
            json.endObject();
            json.endArray();
            json.name("Batch");
            json.beginArray();
            json.beginObject();
            json.name("Select");
            json.value(this.ctrlCenter.isSwitchBatchSelected());
            json.name("Variables");
            json.beginArray();
            if (!this.ctrlCenter.isSwitchBatchSelected()) {
                json.value("");
            } else {
                json.value(false); //TODO:change
            }
            json.endArray();
            json.name("Threads");
            json.value(!this.ctrlCenter.isSwitchBatchSelected() ? -1 : this.ctrlCenter.getNumberThreads());
            json.endObject();
            json.endArray();
            json.name("Classpath");
            json.beginArray();
            for (final String s: this.ctrlCenter.getClasspath()) {
                json.value(s);
            }
            json.endArray();
            json.endObject();
            json.close();
        } catch (IOException e) {
            L.error("Error writing the file. This is most likely a bug.", e);
        }

    }

    /**
     * 
     */
    @FXML
    public void clickImport() {
        /*final DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Import project folder");
        dirChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        final File dir = dirChooser.showDialog(this.main.getStage());  // eccezione se non si seleziona nulla!
        System.out.println("base: " + dir.getName());
        displayDirectoryContent(dir);*/
    }

    /*private void displayDirectoryContent(final File dir) {
        try {
            final File[] files = dir.listFiles();
            for (final File file: files) {
                if (file.isDirectory()) {
                    System.out.println("   directory: " + file.getName());
                    displayDirectoryContent(file);
                } else {
                    System.out.println("      file: " + file.getName());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

}
