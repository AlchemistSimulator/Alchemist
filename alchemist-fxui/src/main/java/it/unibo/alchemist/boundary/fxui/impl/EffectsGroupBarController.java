/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.impl;

import com.google.gson.JsonParseException;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDrawersStack;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.boundary.fxui.EffectGroup;
import it.unibo.alchemist.boundary.fxui.effects.EffectStack;
import it.unibo.alchemist.boundary.fxui.effects.serialization.EffectSerializer;
import it.unibo.alchemist.boundary.fxui.util.FXResourceLoader;
import it.unibo.alchemist.boundary.fxui.FXOutputMonitor;
import it.unibo.alchemist.model.Position2D;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

import static it.unibo.alchemist.boundary.fxui.effects.serialization.EffectSerializer.DEFAULT_EXTENSION;
import static it.unibo.alchemist.boundary.fxui.util.FXResourceLoader.getWhiteIcon;
import static it.unibo.alchemist.boundary.fxui.util.ResourceLoader.getStringRes;
import static jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons.ADD;
import static jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons.FOLDER_OPEN;
import static jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons.SAVE;

/**
 * This class models a JavaFX controller for EffectsGroupBar.fxml.
 *
 * @param <P> the position type
 */
@SuppressFBWarnings("EI_EXPOSE_REP")
public class EffectsGroupBarController<P extends Position2D<? extends P>> implements Initializable {
    /**
     * Layout path.
     */
    public static final String EFFECT_GROUP_BAR_LAYOUT = "EffectsGroupBar";
    /**
     * Default {@code Logger}.
     */
    private static final Logger L = LoggerFactory.getLogger(EffectsGroupBarController.class);
    private final JFXDrawersStack stack;
    @FXML
    @Nullable
    private JFXButton save; // Value injected by FXMLLoader
    @FXML
    @Nullable
    private JFXButton load; // Value injected by FXMLLoader
    @FXML
    @Nullable
    private JFXButton addGroup; // Value injected by FXMLLoader
    @FXML
    @Nullable
    private ListView<EffectGroup<P>> effectGroupsList; // Value injected by FXMLLoader
    private ObservableList<EffectGroup<P>> observableEffectsList;
    private Optional<String> lastPath;
    private Optional<FXOutputMonitor<?, ?>> displayMonitor = Optional.empty();

    /**
     * Default constructor.
     *
     * @param stack the stack where to open the effects lists
     */
    public EffectsGroupBarController(final JFXDrawersStack stack) {
        this.stack = stack;
        this.lastPath = Optional.empty();
    }

    /**
     * Constructor.
     *
     * @param displayMonitor the graphical {@link it.unibo.alchemist.boundary.OutputMonitor}
     * @param stack          the stack where to open the effects lists
     */
    public EffectsGroupBarController(
            final @Nullable FXOutputMonitor<?, ?> displayMonitor,
            final JFXDrawersStack stack
    ) {
        this(stack);
        setDisplayMonitor(displayMonitor);
    }

    /**
     * Getter method for the graphical {@link it.unibo.alchemist.boundary.OutputMonitor}.
     *
     * @return the graphical {@link it.unibo.alchemist.boundary.OutputMonitor}, if any
     */
    public final Optional<FXOutputMonitor<?, ?>> getDisplayMonitor() {
        return displayMonitor;
    }

    /**
     * Setter method for the graphical {@link it.unibo.alchemist.boundary.OutputMonitor}.
     *
     * @param displayMonitor the graphical {@link it.unibo.alchemist.boundary.OutputMonitor} to set;
     *                       if null, it will be {@link Optional#empty() unset}
     */
    public final void setDisplayMonitor(final @Nullable FXOutputMonitor<?, ?> displayMonitor) {
        this.displayMonitor = Optional.ofNullable(displayMonitor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        Objects.requireNonNull(
                save,
                FXResourceLoader.getInjectionErrorMessage("save", EFFECT_GROUP_BAR_LAYOUT)
        );
        Objects.requireNonNull(
                load,
                FXResourceLoader.getInjectionErrorMessage("load", EFFECT_GROUP_BAR_LAYOUT)
        );
        Objects.requireNonNull(
                addGroup,
                FXResourceLoader.getInjectionErrorMessage("add", EFFECT_GROUP_BAR_LAYOUT)
        );
        Objects.requireNonNull(
                effectGroupsList,
                FXResourceLoader.getInjectionErrorMessage("effectGroupsList", EFFECT_GROUP_BAR_LAYOUT)
        );
        this.save.setText("");
        this.save.setGraphic(getWhiteIcon(SAVE));
        this.save.setOnAction(e -> this.saveToFile());
        this.load.setText("");
        this.load.setGraphic(getWhiteIcon(FOLDER_OPEN));
        this.load.setOnAction(e -> this.loadFromFile());
        this.addGroup.setText("");
        this.addGroup.setGraphic(getWhiteIcon(ADD));
        this.addGroup.setOnAction(e ->
                addGroupToList(
                        getStringRes("effect_group_default_name")
                                + " "
                                + (getObservableEffectsList().size() + 1)
                )
        );
    }

    /**
     * Adds a new {@link EffectGroup} to the {@link ListView}.
     *
     * @param name the name to give to the {@code EffectGroup}
     */
    private void addGroupToList(final String name) {
        final EffectGroup<P> newGroup = new EffectStack<>();
        newGroup.setName(name);
        this.getObservableEffectsList().add(newGroup);
        if (this.effectGroupsList != null) {
            this.effectGroupsList.refresh();
        }
    }

    /**
     * Getter method and lazy initializer for the internal
     * {@link ObservableList}.
     *
     * @return the {@code ObservableList} associated to the controlled
     * {@link ListView}
     */
    public ObservableList<EffectGroup<P>> getObservableEffectsList() {
        if (this.observableEffectsList == null) {
            this.observableEffectsList = FXCollections.observableArrayList();
            if (this.effectGroupsList != null) {
                this.effectGroupsList.setItems(observableEffectsList);
                this.effectGroupsList.setCellFactory(lv -> {
                    if (getDisplayMonitor().isPresent()) {
                        return new EffectGroupCell<>(getDisplayMonitor().get(), this.stack);
                    } else {
                        return new EffectGroupCell<>(this.stack);
                    }
                });
            }
        }
        return this.observableEffectsList;
    }

    /**
     * Saves the {@link EffectGroup}s to file using {@link com.google.gson.Gson}.
     */
    private void saveToFile() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(getStringRes("save_effect_groups_dialog_title"));
        final ExtensionFilter json = new ExtensionFilter(
                getStringRes("json_extension_filter_description"),
                "*" + DEFAULT_EXTENSION
        );
        fileChooser.getExtensionFilters().addAll(
                json,
                new ExtensionFilter(getStringRes("all_files_extension_filter_description"), "*.*")
        );
        lastPath.ifPresent(path -> {
            final File folder = new File(path);
            if (folder.isDirectory()) {
                fileChooser.setInitialDirectory(folder);
            }
        });
        fileChooser.setInitialFileName("Effects" + DEFAULT_EXTENSION);
        fileChooser.setSelectedExtensionFilter(json);
        Objects.requireNonNull(
                this.save,
                FXResourceLoader.getInjectionErrorMessage("save", EFFECT_GROUP_BAR_LAYOUT)
        );
        File selectedFile = fileChooser.showSaveDialog(this.save.getScene().getWindow());
        if (selectedFile != null) {
            if (FilenameUtils.getExtension(selectedFile.getAbsolutePath()).isEmpty()) {
                selectedFile = new File(selectedFile.getAbsolutePath() + DEFAULT_EXTENSION);
            }
            this.lastPath = Optional.ofNullable(selectedFile.getParent());
            try {
                // we need to keep the EffectFX parameterized, so we cannot use arrays
                EffectSerializer.effectGroupsToFile(
                        selectedFile,
                        new ArrayList<>(getObservableEffectsList())
                );
            } catch (final IOException | JsonParseException e) {
                L.error("Can't save Effect Groups to file: " + e.getMessage());
                this.errorDialog(
                        getStringRes(
                        "save_effect_groups_error_dialog_title"),
                        getStringRes("save_effect_groups_error_dialog_msg"),
                        e
                );
            }
        }
    }

    /**
     * Loads the {@link EffectGroup}s from file using {@link com.google.gson.Gson}.
     */
    private void loadFromFile() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(getStringRes("load_effect_groups_dialog_title"));
        lastPath.ifPresent(path -> {
            final File folder = new File(path);
            if (folder.isDirectory()) {
                fileChooser.setInitialDirectory(folder);
            }
        });
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter(
                        getStringRes("json_extension_filter_description"),
                        "*" + DEFAULT_EXTENSION
                ),
                new ExtensionFilter(getStringRes("all_files_extension_filter_description"), "*.*")
        );
        Objects.requireNonNull(
                this.load,
                FXResourceLoader.getInjectionErrorMessage("load", EFFECT_GROUP_BAR_LAYOUT)
        );
        final File selectedFile = fileChooser.showOpenDialog(this.load.getScene().getWindow());
        if (selectedFile != null) {
            this.lastPath = Optional.ofNullable(selectedFile.getParent());
            try {
                this.getObservableEffectsList().addAll(EffectSerializer.effectGroupsFromFile(selectedFile));
            } catch (final IOException | JsonParseException e) {
                L.error("Can't load Effect Groups from file: " + e.getMessage());
                this.errorDialog(
                        getStringRes("load_effect_groups_error_dialog_title"),
                        getStringRes("load_effect_groups_error_dialog_msg"),
                        e
                );
            }
        }
    }

    /**
     * Opens up a {@link javafx.scene.control.Dialog} showing the exception that caused it.
     *
     * @param title  the title of the {@code Dialog}
     * @param header the header of the {@code Dialog}
     * @param cause  the {@link Exception} that caused the error
     */
    private void errorDialog(final String title, final String header, final Exception cause) {
        final Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(cause.toString());
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        cause.printStackTrace(pw);
        final String exceptionText = sw.toString();
        final Label label = new Label(getStringRes("exception_error_dialog_msg"));
        final TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        final GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);
        alert.getDialogPane().setExpandableContent(expContent);
        alert.getDialogPane().autosize();
        alert.showAndWait();
    }
}
