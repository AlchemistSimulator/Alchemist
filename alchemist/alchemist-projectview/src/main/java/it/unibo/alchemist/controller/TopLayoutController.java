package it.unibo.alchemist.controller;

import java.io.File;
//import java.io.IOException;

import it.unibo.alchemist.Main;
import javafx.fxml.FXML;
import javafx.stage.DirectoryChooser;

/**
 * 
 *
 */
public class TopLayoutController {

    private Main main;

    /**
     * Sets the main class.
     * @param main main class
     */
    public void setMain(final Main main) {
        this.main = main;
    }

    @FXML
    private void clickImport() {
        final DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Import project folder");
        dirChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File dir = dirChooser.showDialog(this.main.getStage());  // eccezione se non si seleziona nulla!
        System.out.println("base: " + dir.getName());
        displayDirectoryContent(dir);
    }

    private void displayDirectoryContent(final File dir) {
        //try {
            File[] files = dir.listFiles();
            for (File file: files) {
                if (file.isDirectory()) {
                    System.out.println("   directory: " + file.getName());
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
