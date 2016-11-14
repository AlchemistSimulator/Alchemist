package it.unibo.alchemist.boundary.projectview.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import it.unibo.alchemist.AlchemistRunner;
import it.unibo.alchemist.boundary.l10n.LocalizedResourceBundle;
import it.unibo.alchemist.boundary.projectview.ProjectGUI;
import it.unibo.alchemist.boundary.projectview.model.Project;
import it.unibo.alchemist.loader.YamlLoader;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.StackPane;

/**
 * 
 *
 */
public class LeftLayoutController {

    private static final Logger L = LoggerFactory.getLogger(ProjectGUI.class);

    @FXML
    private Button run;
    @FXML
    private StackPane pane;
    @FXML
    private TreeView<String> treeView;

    private String pathFolder;
    private String selectedFile;

    /**
     * 
     */
    public void initialize() {
        this.run.setText(LocalizedResourceBundle.getString("run"));
    }

    /**
     * 
     * @return path of project folder
     */
    public String getPathFolder() {
        return this.pathFolder;
    }

    /**
     * 
     * @return path of selected file
     */
    public String getSelectedFilePath() {
        return this.selectedFile;
    }

    /**
     * 
     * @param dir Selected directory
     */
    public void setTreeView(final File dir) {
        this.pathFolder = dir.getAbsolutePath();

        final TreeItem<String> root = new TreeItem<>(dir.getName());
        root.setExpanded(true);
        this.treeView = new TreeView<>(root);

        displayProjectContent(dir, root);

        this.pane.getChildren().add(this.treeView);

        this.treeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<String>>() {

            @Override
            public void changed(final ObservableValue<? extends TreeItem<String>> observable, 
                    final TreeItem<String> oldVal,
                    final TreeItem<String> newVal) {
                final TreeItem<String> selectedItem = (TreeItem<String>) newVal;
                TreeItem<String> parent = selectedItem.getParent();
                String path = File.separator + selectedItem.getValue();
                while (parent != null)  {
                    if (parent.getParent() != null) {
                        path = File.separator + parent.getValue() + path;
                    }
                    parent = parent.getParent();
                }
                selectedFile = pathFolder + path;
            }

        });
    }

    /**
     * 
     */
    @FXML
    public void clickRun() {
        final Gson gson = new Gson();
        try {
            final BufferedReader br = new BufferedReader(new FileReader(this.pathFolder + File.separator + ".alchemist_project_descriptor.json"));
            final Project proj = gson.fromJson(br, Project.class);

            if (proj.getSimulation().isEmpty()) {
                setAlert(LocalizedResourceBundle.getString("sim_no_selected"), LocalizedResourceBundle.getString("sim_no_selected_header"), LocalizedResourceBundle.getString("sim_no_selected_content"));
            } else {
                final String effect;
                if (proj.getEffect().isEmpty()) {
                    effect = "";
                } else {
                    effect = proj.getEffect();
                }
                final String outputPath;
                if (!proj.getOutput().isSelect()) {
                    outputPath = "";
                } else {
                    outputPath = proj.getOutput().getFolder() + File.separator + proj.getOutput().getBaseName();
                }
                launchRunner(proj.getSimulation(), (double) proj.getEndTime(), effect, outputPath,
                        proj.getOutput().getSamplInterval(), proj.getBatch().getThread());
            }
        } catch (FileNotFoundException e) {
            L.error("Error reading the file. This is most likely a bug.", e);
        }
    }

    private void displayProjectContent(final File dir, final TreeItem<String> root) {
        final File[] files = dir.listFiles();
        for (final File file: files) {
            if (!file.getName().equals(".alchemist_project_descriptor.json")) {
                final TreeItem<String> singleFile = new TreeItem<>(file.getName());
                if (file.isDirectory()) {
                    displayProjectContent(file, singleFile);
                    root.getChildren().add(singleFile);
                } else {
                    root.getChildren().add(singleFile);
                }
                root.setExpanded(true);
            }
        }
    }

    private void launchRunner(final String sim, final double endTime, final String effect,
            final String outputFile, final double sampInt, final int paral) {
        try {
            AlchemistRunner r = new AlchemistRunner.Builder(new YamlLoader(new FileInputStream(sim)))
            .setEndTime(new DoubleTime(endTime))
            .setEffects(effect)
            .setOutputFile(outputFile)
            .setInterval(sampInt)
            .setParallelism(paral)
            .setHeadless(false)
            .build();
            r.launch();
        } catch (FileNotFoundException e) {
            L.error("Error reading the file YAML. This is most likely a bug.", e);
        }
    }

    private void setAlert(final String title, final String header, final String content) {
        final Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

}
