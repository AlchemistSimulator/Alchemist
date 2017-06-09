package it.unibo.alchemist.boundary.gui.controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParseException;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDrawersStack;

import it.unibo.alchemist.boundary.gui.effects.EffectGroup;
import it.unibo.alchemist.boundary.gui.effects.EffectStack;
import it.unibo.alchemist.boundary.gui.effects.json.EffectSerializer;
import it.unibo.alchemist.boundary.gui.utility.FXResourceLoader;
import it.unibo.alchemist.boundary.gui.view.cells.EffectGroupCell;
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
import jiconfont.icons.GoogleMaterialDesignIcons;

/**
 * This class models a JavaFX controller for EffectsGroupBar.fxml.
 */
public class EffectsGroupBarController implements Initializable {
    /** Layout path. */
    public static final String EFFECT_GROUP_BAR_LAYOUT = "EffectsGroupBar";
    /** Default {@code Logger}. */
    private static final Logger L = LoggerFactory.getLogger(EffectsGroupBarController.class);

    @FXML
    private JFXButton save;
    @FXML
    private JFXButton load;
    @FXML
    private JFXButton addGroup;
    @FXML
    private ListView<EffectGroup> effectGroupsList;

    private ObservableList<EffectGroup> observableList;

    private final JFXDrawersStack stack;

    /**
     * Default constructor.
     * 
     * @param stack
     *            the stack where to open the effects lists
     */
    public EffectsGroupBarController(final JFXDrawersStack stack) {
        this.stack = stack;
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        assert save != null : FXResourceLoader.getInjectionErrorMessage("save", EFFECT_GROUP_BAR_LAYOUT);
        assert load != null : FXResourceLoader.getInjectionErrorMessage("load", EFFECT_GROUP_BAR_LAYOUT);
        assert addGroup != null : FXResourceLoader.getInjectionErrorMessage("add", EFFECT_GROUP_BAR_LAYOUT);
        assert effectGroupsList != null : FXResourceLoader.getInjectionErrorMessage("effectGroupsList", EFFECT_GROUP_BAR_LAYOUT);

        this.save.setText("");
        this.save.setGraphic(FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.SAVE));
        this.save.setOnAction(e -> this.saveToFile()); // TODO

        this.load.setText("");
        this.load.setGraphic(FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.FOLDER_OPEN));
        this.load.setOnAction(e -> this.loadFromFile()); // TODO

        this.addGroup.setText("");
        this.addGroup.setGraphic(FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.ADD));
        this.addGroup.setOnAction(e -> addGroupToList("Effect group " + (getObservableList().size() + 1)));
    }

    private void addGroupToList(final String name) {
        this.getObservableList().add(new EffectStack());
        this.getObservableList().get(this.getObservableList().size() - 1).setName(name);
        this.effectGroupsList.refresh();
    }

    private ObservableList<EffectGroup> getObservableList() {
        if (this.observableList == null) {
            this.observableList = FXCollections.observableArrayList();
            this.effectGroupsList.setItems(observableList);
            this.effectGroupsList.setCellFactory(lv -> new EffectGroupCell(this.stack));
            // TODO check
        }
        return this.observableList;
    }

    private void saveToFile() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save effect");
        final File selectedFile = fileChooser.showSaveDialog(this.save.getScene().getWindow());

        if (selectedFile != null) {
            try {
                EffectSerializer.effectGroupsToFile(selectedFile,
                        Arrays.asList(getObservableList().toArray(new EffectGroup[getObservableList().size()])));
            } catch (final IOException | JsonParseException e) {
                L.error("Can't save Effect Groups to file: " + e.getMessage());
                this.errorDialog("Exception during save", "Can't save Effect Groups to file", e);
            }
        }
    }

    private void loadFromFile() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load effect");
        fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Text Files", "*.txt", "*.json"),
                new ExtensionFilter("All Files", "*.*"));

        final File selectedFile = fileChooser.showOpenDialog(this.load.getScene().getWindow());

        if (selectedFile != null) {
            try {
                this.getObservableList().addAll(EffectSerializer.effectGroupsFromFile(selectedFile));
            } catch (final IOException | JsonParseException e) {
                L.error("Can't load Effect Groups from file: " + e.getMessage());
                this.errorDialog("Exception during load", "Can't load Effect Groups from file", e);
            }
        }
    }

    private void errorDialog(final String title, final String header, final Exception cause) {
        final Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(cause.toString());

        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        cause.printStackTrace(pw);
        final String exceptionText = sw.toString();

        final Label label = new Label("The exception stacktrace was: ");

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
