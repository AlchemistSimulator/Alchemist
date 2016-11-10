package it.unibo.alchemist.controller;

import java.io.File;
//import java.io.IOException;
import java.io.IOException;

import it.unibo.alchemist.Main;
import it.unibo.alchemist.boundary.l10n.R;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * 
 *
 */
public class TopLayoutController {

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

    private Main main;

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
            //L.error("Error loading the graphical interface. This is most likely a bug.", e);
            System.exit(1);
        }
    }

    /**
     * 
     */
    @FXML
    public void clickImport() {
        final DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Import project folder");
        dirChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        final File dir = dirChooser.showDialog(this.main.getStage());  // eccezione se non si seleziona nulla!
        //System.out.println("base: " + dir.getName());
        displayDirectoryContent(dir);
    }

    private void displayDirectoryContent(final File dir) {
        //try {
            final File[] files = dir.listFiles();
            for (final File file: files) {
                if (file.isDirectory()) {
                    //System.out.println("   directory: " + file.getName());
                    displayDirectoryContent(file);
                } else {
                    System.out.println("      file: " + file.getName());
                }
            }
        /*} catch (IOException e) {
            e.printStackTrace();
        }*/
    }

}
