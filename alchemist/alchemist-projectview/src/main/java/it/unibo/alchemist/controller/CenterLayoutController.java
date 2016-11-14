package it.unibo.alchemist.controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.controlsfx.control.ToggleSwitch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.alchemist.Main;
import it.unibo.alchemist.boundary.l10n.R;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * Controller of CenterLayout view.
 */
public class CenterLayoutController {

    private static final Logger L = LoggerFactory.getLogger(Main.class);
    private static final double MIN_SAM = 0.01;
    private static final double MAX_SAM = 600;
    private static final double STEP_SAM = 0.01;
    private static final int MAX_TIME = 18000;
    private static final int VALUE_TIME = 60;
    private static final String EFF_EXT = R.getString("eff_ext");
    private static final String YAML_EXT = R.getString("yaml_ext");

    @FXML
    private Button addClass;
    @FXML
    private Button batch;
    @FXML
    private Button removeClass;
    @FXML
    private Button editEff;
    @FXML
    private Button editYaml;
    @FXML
    private Button newEff;
    @FXML
    private Button newYaml;
    @FXML
    private Button setEff;
    @FXML
    private Button setOut;
    @FXML
    private Button setYaml;
    @FXML
    private GridPane gridEff;
    @FXML
    private GridPane gridOut;
    @FXML
    private GridPane gridVar;
    @FXML
    private GridPane gridYaml;
    @FXML
    private Label baseNameOut;
    @FXML
    private Label batchMode;
    @FXML
    private Label classpath;
    @FXML
    private Label eff;
    @FXML
    private Label endTime;
    @FXML
    private Label intOut;
    @FXML
    private Label output;
    @FXML
    private Label pathEff;
    @FXML
    private Label pathOut;
    @FXML
    private Label pathYaml;
    @FXML
    private Label simConf;
    @FXML
    private Label thread;
    @FXML
    private Label unitOut;
    @FXML
    private Label unitTime;
    @FXML
    private ListView<String> listClass = new ListView<>();
    @FXML
    private ListView<String> listYaml = new ListView<>();;
    @FXML
    private Spinner<Integer> spinBatch;
    @FXML
    private Spinner<Integer> spinTime;
    @FXML
    private Spinner<Double> spinOut;
    @FXML
    private TextField bnTextOut;

    private LeftLayoutController ctrlLeft;
    private Main main;

    private final ObservableList<String> data = FXCollections.observableArrayList();
    private final ToggleSwitch tsOut = new ToggleSwitch();
    private final ToggleSwitch tsVar = new ToggleSwitch();

    /**
     * 
     */
    public void initialize() {
        this.addClass.setText(R.getString("add"));
        this.baseNameOut.setText(R.getString("base_name"));
        this.batch.setText(R.getString("batch_start"));
        this.batchMode.setText(R.getString("batch_pane_title"));
        this.bnTextOut.setPromptText(R.getString("enter_base_name"));
        this.bnTextOut.setText(R.getString("base_name_text"));
        this.classpath.setText(R.getString("classpath_pane_title"));
        this.removeClass.setText(R.getString("remove"));
        this.editEff.setText(R.getString("edit"));
        this.editYaml.setText(R.getString("edit"));
        this.eff.setText(R.getString("eff_pane_title"));
        this.endTime.setText(R.getString("end_time"));
        this.intOut.setText(R.getString("interval"));
        this.newEff.setText(R.getString("new"));
        this.newYaml.setText(R.getString("new"));
        this.output.setText(R.getString("out_pane_title"));
        this.setEff.setText(R.getString("set"));
        this.setOut.setText(R.getString("set_folder"));
        this.setYaml.setText(R.getString("set"));
        this.simConf.setText(R.getString("sim_pane_title"));
        this.spinBatch.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 
                        Runtime.getRuntime().availableProcessors() + 1,
                        Runtime.getRuntime().availableProcessors() + 1, 
                        1));
        this.spinOut.setEditable(true);
        this.spinOut.setValueFactory(
                new SpinnerValueFactory.DoubleSpinnerValueFactory(MIN_SAM, MAX_SAM, 1, STEP_SAM));
        this.spinTime.setEditable(true);
        this.spinTime.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, MAX_TIME, VALUE_TIME, 1));
        this.thread.setText(R.getString("n_thread"));
        this.unitOut.setText(R.getString("sec"));
        this.unitTime.setText(R.getString("sec"));
        if (this.listClass.getItems().size() == 0) {
            this.removeClass.setDisable(true);
        }
    }

    /**
     * Sets the main class and adds toggle switch to view.
     * @param main main class.
     */
    public void setMain(final Main main) {
        this.main = main;

        this.gridOut.add(this.tsOut, 0, 0);
        controlSwitch(this.tsOut);
        this.gridVar.add(this.tsVar, 0, 0);
        controlSwitch(this.tsVar);
    }

    /**
     * 
     * @param controller LeftLayout controller
     */
    public void setCtrlLeft(final LeftLayoutController controller) {
        this.ctrlLeft = controller;
    }

    private void controlSwitch(final ToggleSwitch ts) {
        if (ts.isSelected()) {
            setComponentVisible(ts, true);
        } else {
            setComponentVisible(ts, false);
        }

        ts.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(final ObservableValue<? extends Boolean> ov, 
                    final Boolean t1, final Boolean t2) {
                if (ts.isSelected()) {
                    setComponentVisible(ts, true);
                } else {
                    setComponentVisible(ts, false);
                }
            }
        });
    }

    private void setComponentVisible(final ToggleSwitch ts, final boolean vis) {
        if (ts.equals(this.tsOut)) {
            this.setOut.setVisible(vis);
            this.pathOut.setVisible(vis);
            this.baseNameOut.setVisible(vis);
            this.bnTextOut.setVisible(vis);
            this.intOut.setVisible(vis);
            this.unitOut.setVisible(vis);
            this.spinOut.setVisible(vis);
        } else {
            if (ts.isSelected() && this.pathYaml.getText().equals("")) {
                setAlert(R.getString("file_no_selected"), R.getString("yaml_no_selected_header"), R.getString("yaml_no_selected_content"));
                setVisibilityBatch(false);
                ts.setSelected(false);
            } else {
                setVisibilityBatch(vis);
                if (ts.isSelected()) {
                    //TODO: read file YAML and get variables
                    /*try {
                        final Loader fileYaml = new YamlLoader(new FileReader(this.pathYaml.getText()));
                    } catch (FileNotFoundException e) {
                    }*/
                    final ObservableList<String> vars = FXCollections.observableArrayList();
                    vars.addAll("var1", "var2", "var3", "var4", "var5", "var6", "var7", "var8", "var9", "var10"); //TODO: change with obtained variables
                    this.listYaml.setItems(vars);
                    this.listYaml.setCellFactory(CheckBoxListCell.forListView(new Callback<String, ObservableValue<Boolean>>() {
                        @Override
                        public ObservableValue<Boolean> call(final String var) {
                            final BooleanProperty observable = new SimpleBooleanProperty();
                            observable.addListener((obs, wasSelected, isNowSelected) -> 
                                System.out.println("Check box for " + var + " changed from " + wasSelected + " to " + isNowSelected) //TODO: add or remove variables selected into list
                            );
                            return observable;
                        }
                    }));
                }
            }
        }
    }

    private void setVisibilityBatch(final boolean visibility) {
        this.batch.setVisible(visibility);
        this.listYaml.setVisible(visibility);
        this.spinBatch.setVisible(visibility);
        this.thread.setVisible(visibility);
    }

    /**
     * 
     */
    @FXML
    public void clickSetYaml() {
        manageFile(YAML_EXT, false);
    }

    /**
     * Show dialog to create new YAML file.
     */
    @FXML
    public void clickNewYaml() {
        newFile(YAML_EXT);
    }

    /**
     * 
     */
    @FXML
    public void clickEditYaml() {
        manageFile(YAML_EXT, true);
    }

    /**
     * 
     */
    @FXML
    public void clickSetEffect() {
        manageFile(EFF_EXT, false);
    }

    /**
     * Show dialog to create new effect file.
     */
    @FXML
    public void clickNewEffect() {
        newFile(EFF_EXT);
    }

    /**
     * 
     */
    @FXML
    public void clickEditEffect() {
        manageFile(EFF_EXT, true);
    }

    /**
     * 
     */
    @FXML
    public void clickSetFolderOut() {
        if (this.ctrlLeft.getSelectedFilePath() == null) {
            setAlert(R.getString("folder_no_selected"), R.getString("folder_no_selected_header"), R.getString("folder_no_selected_content"));
        } else if (!FilenameUtils.getExtension(this.ctrlLeft.getSelectedFilePath()).isEmpty()) {
            setAlert(R.getString("wrong_selection"), R.getString("wrong_selection_header"), R.getString("wrong_selection_content"));
        } else {
            File file = new File(this.ctrlLeft.getSelectedFilePath());
            while (!file.getName().equals(R.getString("folder_output")) && !file.getName().equals(new File(this.ctrlLeft.getPathFolder()).getName())) {
                    file = file.getParentFile();
            }
            if (file.getName().equals(R.getString("folder_output"))) {
                this.pathOut.setText(this.ctrlLeft.getSelectedFilePath());
                setDeleteIcon(this.gridOut, this.pathOut);
            } else {
                setAlert(R.getString("folder_wrong"), R.getString("folder_wrong_header"), R.getString("folder_wrong_content"));
            }
        }
    }

    /**
     * 
     */
    @FXML
    public void clickAddClass() {
        if (this.listClass.getItems().size() == 0) {
            this.removeClass.setDisable(false);
        }
        manageFile(R.getString("jar_ext"), false);
    }

    /**
     * 
     */
    @FXML
    public void clickRemoveClass() {
        final String nameFile = this.listClass.getSelectionModel().getSelectedItem();
        this.listClass.getItems().remove(nameFile);
        if (this.listClass.getItems().size() == 0) {
            this.removeClass.setDisable(true);
        }
    }

    /**
     * 
     * @return Selected simulation path.
     */
    public String getSimulation() {
        return this.pathYaml.getText();
    }

    /**
     * 
     * @param path The path of file simulation.
     */
    public void setSimulation(final String path) {
        this.pathYaml.setText(path);
        setDeleteIcon(this.gridYaml, this.pathYaml);
    }

    /**
     * 
     * @return Selected end time.
     */
    public int getEndTime() {
        return this.spinTime.getValueFactory().getValue();
    }

    /**
     * 
     * @param endT Selected end time.
     */
    public void setEndTime(final int endT) {
        this.spinTime.getValueFactory().setValue(endT);
    }

    /**
     * 
     * @return Selected effect path
     */
    public String getEffect() {
        return this.pathEff.getText();
    }

    /**
     * 
     * @param path The path of file effect.
     */
    public void setEffect(final String path) {
        this.pathEff.setText(path);
        setDeleteIcon(this.gridEff, this.pathEff);
    }

    /**
     * 
     * @return true if the output switch is selected.
     */
    public boolean isSwitchOutputSelected() {
        return this.tsOut.isSelected();
    }

    /**
     * 
     * @param select true if the output switch is selected.
     */
    public void setSwitchOutputSelected(final boolean select) {
        this.tsOut.setSelected(select);
    }

    /**
     * 
     * @return Selected output folder
     */
    public String getOutputFolder() {
        return this.pathOut.getText();
    }

    /**
     * 
     * @param path The path of output folder.
     */
    public void setOutputFolder(final String path) {
        this.pathOut.setText(path);
        setDeleteIcon(this.gridOut, this.pathOut);
    }

    /**
     * 
     * @return Base name typed
     */
    public String getBaseName() {
        return this.bnTextOut.getText();
    }

    /**
     * 
     * @param name The name of output file.
     */
    public void setBaseName(final String name) {
        this.bnTextOut.setText(name);
    }

    /**
     * 
     * @return Selected sampling interval.
     */
    public double getSamplInterval() {
        return this.spinOut.getValueFactory().getValue();
    }

    /**
     * 
     * @param sampInt Selected sampling interval.
     */
    public void setSamplInterval(final double sampInt) {
        this.spinOut.getValueFactory().setValue(sampInt);
    }

    /**
     * 
     * @return True if the batch mode switch is selected.
     */
    public boolean isSwitchBatchSelected() {
        return this.tsVar.isSelected();
    }

    /**
     * 
     * @param select True if the batch mode switch is selected.
     */
    public void setSwitchBatchSelected(final boolean select) {
        this.tsVar.setSelected(select);
    }

    //TODO: get and set list of variable selected

    /**
     * 
     * @return Selected number of threads.
     */
    public int getNumberThreads() {
        return this.spinBatch.getValueFactory().getValue();
    }

    /**
     * 
     * @param threads Selected number of threads.
     */
    public void setNumberThreads(final int threads) {
        this.spinBatch.getValueFactory().setValue(threads);
    }

    /**
     * 
     * @return The libraries to add to the classpath.
     */
    public ObservableList<String> getClasspath() {
        return this.listClass.getItems();
    }

    /**
     * 
     * @param list The libraries to add to the classpath.
     */
    public void setClasspath(final ObservableList<String> list) {
        this.listClass.setItems(list);
    }

    private void manageFile(final String extension, final boolean edit) {
        if (this.ctrlLeft.getSelectedFilePath() == null) {
            setAlert(R.getString("file_no_selected"), R.getString("file_no_selected_header"), R.getString("file_no_selected_content"));
        } else if (this.ctrlLeft.getSelectedFilePath().endsWith(extension)) {
            if (extension.equals(YAML_EXT) && !edit) {
                this.pathYaml.setText(this.ctrlLeft.getSelectedFilePath());
                setDeleteIcon(this.gridYaml, this.pathYaml);
            } else if (extension.equals(EFF_EXT) && !edit) {
                this.pathEff.setText(this.ctrlLeft.getSelectedFilePath());
                setDeleteIcon(this.gridEff, this.pathEff);
            } else if (extension.equals(EFF_EXT) && edit) {
                editFile();
            } else {
                if (!this.data.contains(this.ctrlLeft.getSelectedFilePath())) {
                    this.data.add(this.ctrlLeft.getSelectedFilePath());
                    this.listClass.setItems(data);
                } else {
                    setAlert(R.getString("file_name_exists"), R.getString("file_name_jar_header"), R.getString("file_name_jar_content"));
                }
            }
        } else {
            if (extension.equals(YAML_EXT)) {
                setAlert(R.getString("file_wrong"), R.getString("file_wrong_yaml_header"), R.getString("file_wrong_content"));
            } else if (extension.equals(EFF_EXT)) {
                setAlert(R.getString("file_wrong"), R.getString("file_wrong_effect_header"), R.getString("file_wrong_content"));
            } else {
                setAlert(R.getString("file_wrong"), R.getString("file_wrong_jar_header"), R.getString("file_wrong_content"));
            }
        }
    }

    private void newFile(final String extension) {
        try {
            final FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("view/FileNameDialog.fxml"));
            final AnchorPane pane = (AnchorPane) loader.load();

            final Stage stage = new Stage();
            stage.setTitle(R.getString("file_name_title"));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(this.main.getStage());
            stage.setResizable(false);
            final Scene scene = new Scene(pane);
            stage.setScene(scene);

            final FileNameDialogController controller = loader.getController();
            controller.setDialogStage(stage);
            controller.setExtension(extension);
            controller.setCtrlLeftLayout(this.ctrlLeft);

            stage.showAndWait();
        } catch (IOException e) {
            L.error("Error loading the graphical interface. This is most likely a bug.", e);
            System.exit(1);
        }
    }

    private void editFile() {
        final Desktop desk = Desktop.getDesktop();
        try {
            desk.open(new File(this.ctrlLeft.getSelectedFilePath()));
        } catch (IOException e) {
            L.error("Error opening file.", e);
            System.exit(1);
        }
    }

    private void setDeleteIcon(final GridPane grid, final Label label) {
        final ImageView imgView = new ImageView(new Image(Main.class.getResource("/icon/icon-delete.png").toExternalForm()));
        final Tooltip tooltip = new Tooltip();
        tooltip.setText(R.getString("delete"));
        Tooltip.install(imgView, tooltip);
        grid.add(imgView, 0, 2);
        imgView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent event) {
                label.setText(""); 
                grid.getChildren().remove(imgView);
            }
        });
    }

    private void setAlert(final String title, final String header, final String content) {
        final Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

}
