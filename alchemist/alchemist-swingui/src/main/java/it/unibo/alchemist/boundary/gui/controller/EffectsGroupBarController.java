package it.unibo.alchemist.boundary.gui.controller;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDrawersStack;
import it.unibo.alchemist.boundary.gui.effects.EffectGroup;
import it.unibo.alchemist.boundary.gui.effects.EffectStack;
import it.unibo.alchemist.boundary.gui.effects.json.EffectSerializer;
import it.unibo.alchemist.boundary.gui.utility.FXResourceLoader;
import it.unibo.alchemist.boundary.gui.view.cells.EffectGroupCell;
import it.unibo.alchemist.boundary.interfaces.FXOutputMonitor;
import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static it.unibo.alchemist.boundary.gui.effects.json.EffectSerializer.DEFAULT_EXTENSION;
import static it.unibo.alchemist.boundary.gui.utility.FXResourceLoader.getWhiteIcon;
import static it.unibo.alchemist.boundary.gui.utility.ResourceLoader.getStringRes;
import static jiconfont.icons.GoogleMaterialDesignIcons.ADD;
import static jiconfont.icons.GoogleMaterialDesignIcons.FOLDER_OPEN;
import static jiconfont.icons.GoogleMaterialDesignIcons.SAVE;

/**
 * This class models a JavaFX controller for EffectsGroupBar.fxml.
 */
public class EffectsGroupBarController implements Initializable {
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
    private ListView<EffectGroup> effectGroupsList; // Value injected by FXMLLoader
    private ObservableList<EffectGroup> observableEffectsList;
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
     * @param displayMonitor the graphical {@link OutputMonitor}
     * @param stack          the stack where to open the effects lists
     */
    public EffectsGroupBarController(final @Nullable FXOutputMonitor<?, ?> displayMonitor, final JFXDrawersStack stack) {
        this(stack);
        setDisplayMonitor(displayMonitor);
    }

    /**
     * Getter method for the graphical {@link OutputMonitor}.
     *
     * @return the graphical {@link OutputMonitor}, if any
     */
    public final Optional<FXOutputMonitor<?, ?>> getDisplayMonitor() {
        return displayMonitor;
    }

    /**
     * Setter method for the graphical {@link OutputMonitor}.
     *
     * @param displayMonitor the graphical {@link OutputMonitor} to set; if null, it will be {@link Optional#empty() unset}
     */
    public final void setDisplayMonitor(final @Nullable FXOutputMonitor<?, ?> displayMonitor) {
        this.displayMonitor = Optional.ofNullable(displayMonitor);
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        assert save != null : FXResourceLoader.getInjectionErrorMessage("save", EFFECT_GROUP_BAR_LAYOUT);
        assert load != null : FXResourceLoader.getInjectionErrorMessage("load", EFFECT_GROUP_BAR_LAYOUT);
        assert addGroup != null : FXResourceLoader.getInjectionErrorMessage("add", EFFECT_GROUP_BAR_LAYOUT);
        assert effectGroupsList != null : FXResourceLoader.getInjectionErrorMessage("effectGroupsList", EFFECT_GROUP_BAR_LAYOUT);

        this.save.setText("");
        this.save.setGraphic(getWhiteIcon(SAVE));
        this.save.setOnAction(e -> this.saveToFile());

        this.load.setText("");
        this.load.setGraphic(getWhiteIcon(FOLDER_OPEN));
        this.load.setOnAction(e -> this.loadFromFile());

        this.addGroup.setText("");
        this.addGroup.setGraphic(getWhiteIcon(ADD));
        this.addGroup.setOnAction(e -> addGroupToList(getStringRes("effect_group_default_name") + " " + (getObservableEffectsList().size() + 1)));
    }

    /**
     * Adds a new {@link EffectGroup} to the {@link ListView}.
     *
     * @param name the name to give to the {@code EffectGroup}
     */
    private void addGroupToList(final String name) {
        final EffectGroup newGroup = new EffectStack();
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
    public ObservableList<EffectGroup> getObservableEffectsList() {
        if (this.observableEffectsList == null) {
            this.observableEffectsList = FXCollections.observableArrayList();
            if (this.effectGroupsList != null) {
                this.effectGroupsList.setItems(observableEffectsList);
                this.effectGroupsList.setCellFactory(lv -> {
                    if (getDisplayMonitor().isPresent()) {
                        return new EffectGroupCell(getDisplayMonitor().get(), this.stack);
                    } else {
                        return new EffectGroupCell(this.stack);
                    }
                });
            }
        }
        return this.observableEffectsList;
    }

    /**
     * Saves the {@link EffectGroup}s to file using {@link Gson}.
     */
    private void saveToFile() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(getStringRes("save_effect_groups_dialog_title"));
        final ExtensionFilter json = new ExtensionFilter(getStringRes("json_extension_filter_description"), "*" + DEFAULT_EXTENSION);
        fileChooser.getExtensionFilters().addAll(
                json,
                new ExtensionFilter(getStringRes("all_files_extension_filter_description"), "*.*"));
        lastPath.ifPresent(path -> {
            final File folder = new File(path);
            if (folder.isDirectory()) {
                fileChooser.setInitialDirectory(folder);
            }
        });
        fileChooser.setInitialFileName("Effects" + DEFAULT_EXTENSION);
        fileChooser.setSelectedExtensionFilter(json);

        assert this.save != null;

        File selectedFile = fileChooser.showSaveDialog(this.save.getScene().getWindow());

        if (selectedFile != null) {
            if (FilenameUtils.getExtension(selectedFile.getAbsolutePath()).equals("")) {
                selectedFile = new File(selectedFile.getAbsolutePath() + DEFAULT_EXTENSION);
            }
            this.lastPath = Optional.ofNullable(selectedFile.getParent());

            try {
                EffectSerializer.effectGroupsToFile(selectedFile,
                        Arrays.asList(getObservableEffectsList().toArray(new EffectGroup[getObservableEffectsList().size()])));
            } catch (final IOException | JsonParseException e) {
                L.error("Can't save Effect Groups to file: " + e.getMessage());
                this.errorDialog(getStringRes("save_effect_groups_error_dialog_title"), getStringRes("save_effect_groups_error_dialog_msg"), e);
            }
        }
    }

    /**
     * Loads the {@link EffectGroup}s from file using {@link Gson}.
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
                new ExtensionFilter(getStringRes("json_extension_filter_description"), "*" + DEFAULT_EXTENSION),
                new ExtensionFilter(getStringRes("all_files_extension_filter_description"), "*.*"));

        assert this.load != null;

        final File selectedFile = fileChooser.showOpenDialog(this.load.getScene().getWindow());

        if (selectedFile != null) {
            this.lastPath = Optional.ofNullable(selectedFile.getParent());

            try {
                this.getObservableEffectsList().addAll(EffectSerializer.effectGroupsFromFile(selectedFile));
            } catch (final IOException | JsonParseException e) {
                L.error("Can't load Effect Groups from file: " + e.getMessage());
                this.errorDialog(getStringRes("load_effect_groups_error_dialog_title"), getStringRes("load_effect_groups_error_dialog_msg"), e);
            }
        }
    }

    /**
     * Opens up a {@link Dialog} showing the exception that caused it
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
