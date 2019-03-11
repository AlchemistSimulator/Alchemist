/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.projectview.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.jooq.lambda.Unchecked;
import org.kaikikm.threadresloader.ResourceLoader;

import it.unibo.alchemist.boundary.l10n.LocalizedResourceBundle;
import it.unibo.alchemist.boundary.projectview.ProjectGUI;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * 
 *
 */
public class NewProjLayoutSelectController {

    private static final ResourceBundle RESOURCES = LocalizedResourceBundle.get("it.unibo.alchemist.l10n.ProjectViewUIStrings");
    private static final boolean ON_WINDOWS = System.getProperty("os.name").toLowerCase(Locale.US).contains("windows");

    @FXML
    private Button backBtn;
    @FXML
    private Button finishBtn;
    @FXML
    private ChoiceBox<String> choiceTempl;
    @FXML
    private Label select;

    private ProjectGUI main;
    private String folderPath;
    private String selectedTemplate;
    private Stage stage;
    private static final ObservableList<String> TEMPLATES = FXCollections.observableList(
        resourcesFrom("templates", 1)
            .map(s -> s.substring(0,  s.length() - (s.endsWith("/") ? 1 : 0)))
            .collect(Collectors.toList()));

    /**
     * 
     */
    public void initialize() {
        this.backBtn.setText(RESOURCES.getString("back"));
        this.finishBtn.setText(RESOURCES.getString("finish"));
        this.finishBtn.setDisable(true);
        this.select.setText(RESOURCES.getString("select"));
        this.choiceTempl.setItems(TEMPLATES);
        this.choiceTempl.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectedTemplate = newValue;
            finishBtn.setDisable(false);
        });
}

    /**
     * 
     * @param main main
     */
    public void setMain(final ProjectGUI main) {
        this.main = main;
    }

    /**
     * 
     * @param stage Stage
     */
    public void setStage(final Stage stage) {
        this.stage = stage;
    }

    /**
     * 
     * @param path Folder path
     */
    public void setFolderPath(final String path) {
        this.folderPath = path;
    }

    private void copyRecursively(final String path) {
        resourcesFrom(path, Integer.MAX_VALUE)
            .sorted((s1, s2) -> Integer.compare(s2.length(), s1.length())) // Longest first
            .forEach(Unchecked.consumer(p -> {
                try (InputStream is = ResourceLoader.getResourceAsStream(path + '/' + p)) {
                    final File destination = new File(folderPath + '/' + p);
                    if (!destination.exists()) {
                        FileUtils.copyInputStreamToFile(is, destination);
                    }
                }
            }));
    }

    /**
     * 
     */
    @FXML
    public void clickFinish() {
        copyRecursively("templates/" + selectedTemplate);
        this.stage.close();
    }

    /**
     * @throws IOException
     *             if the XML can not get loaded
     */
    @FXML
    public void clickBack() throws IOException {
        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(ResourceLoader.getResource(ProjectGUI.RESOURCE_LOCATION + "/view/NewProjLayoutFolder.fxml"));
        final AnchorPane pane = (AnchorPane) loader.load();
        final Scene scene = new Scene(pane);
        this.stage.setScene(scene);
        final NewProjLayoutFolderController ctrl = loader.getController();
        ctrl.setMain(this.main);
        ctrl.setStage(this.stage);
        ctrl.setFolderPath(this.folderPath);
    }

    /*
     * Must return strings that represent RELATIVE paths starting by input path
     */
    private static Stream<String> resourcesFrom(final String path, final int depth) {
        try {
            final URI uri = ResourceLoader.getResource(path).toURI();
            Path myPath;
            if (uri.getScheme().equals("jar")) {
                FileSystem fileSystem;
                try {
                    fileSystem = FileSystems.getFileSystem(uri);
                } catch (FileSystemNotFoundException e) {
                    fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                }
                myPath = fileSystem.getPath(path);
            } else {
                myPath = Paths.get(uri);
            }
            Stream<String> resourcesStream = Files.walk(myPath, depth)
                    .skip(1)
                    .map(Path::toString)
                    .map(s -> s.replace(myPath.toString(), ""))
                    .map(s-> s.substring(1))
                    .sorted();
            if (ON_WINDOWS) {
                resourcesStream = resourcesStream.map(s -> s.replaceAll("\\\\", "/"));
            }
            return resourcesStream;
        } catch (URISyntaxException | IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
