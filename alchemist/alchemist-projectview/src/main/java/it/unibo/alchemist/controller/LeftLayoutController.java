package it.unibo.alchemist.controller;

import java.io.File;

import it.unibo.alchemist.boundary.l10n.R;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;

/**
 * 
 *
 */
public class LeftLayoutController {

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
        this.run.setText(R.getString("run"));
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

}
