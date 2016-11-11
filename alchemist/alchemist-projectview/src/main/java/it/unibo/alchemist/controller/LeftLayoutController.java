package it.unibo.alchemist.controller;

import java.io.File;

import it.unibo.alchemist.boundary.l10n.R;
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

    /**
     * 
     */
    public void initialize() {
        this.run.setText(R.getString("run"));
    }

    /**
     * 
     * @param dir Selected directory
     */
    public void setTreeView(final File dir) {
        final TreeItem<String> root = new TreeItem<>(dir.getName());
        root.setExpanded(true);
        this.treeView = new TreeView<>(root);

        displayProjectContent(dir, root);

        this.pane.getChildren().add(this.treeView);
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
