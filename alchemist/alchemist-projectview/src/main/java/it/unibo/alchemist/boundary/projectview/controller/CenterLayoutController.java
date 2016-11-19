package it.unibo.alchemist.boundary.projectview.controller;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Stack;

import org.apache.commons.io.FilenameUtils;
import org.controlsfx.control.ToggleSwitch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.alchemist.boundary.l10n.LocalizedResourceBundle;
import it.unibo.alchemist.boundary.projectview.ProjectGUI;
import it.unibo.alchemist.boundary.projectview.model.Project;
import it.unibo.alchemist.loader.Loader;
import it.unibo.alchemist.loader.YamlLoader;
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
import sun.misc.URLClassPath;

/**
 * Controller of CenterLayout view.
 */
public class CenterLayoutController {

    private static final Logger L = LoggerFactory.getLogger(ProjectGUI.class);
    private static final ResourceBundle RESOURCES = LocalizedResourceBundle.get("it.unibo.alchemist.l10n.ProjectViewUIStrings");
    private static final double MIN = 0.01;
    private static final double MAX_SAM = 600;
    private static final double STEP = 0.01;
    private static final double VALUE_TIME = 60;
    private static final String EFF_EXT = RESOURCES.getString("eff_ext");
    private static final String YAML_EXT = RESOURCES.getString("yaml_ext");

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
    private GridPane grid;
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
    private Spinner<Double> spinTime;
    @FXML
    private Spinner<Double> spinOut;
    @FXML
    private TextField bnTextOut;

    private LeftLayoutController ctrlLeft;
    private ProjectGUI main;

    private Map<String, Boolean> variables = new HashMap<>();
    private ObservableList<String> data = FXCollections.observableArrayList();
    private final ToggleSwitch tsOut = new ToggleSwitch();
    private final ToggleSwitch tsVar = new ToggleSwitch();
    private final Image img = new Image(ProjectGUI.class.getResource("/icon/icon-delete.png").toExternalForm());
    private final ImageView imgViewYaml = new ImageView(img);
    private final ImageView imgViewEff = new ImageView(img);
    private final ImageView imgViewOut = new ImageView(img);

    /**
     * 
     */
    public void initialize() {
        this.grid.setDisable(true);
        this.addClass.setText(RESOURCES.getString("add"));
        this.baseNameOut.setText(RESOURCES.getString("base_name"));
        this.batch.setText(RESOURCES.getString("batch_start"));
        this.batchMode.setText(RESOURCES.getString("batch_pane_title"));
        this.bnTextOut.setPromptText(RESOURCES.getString("enter_base_name"));
        this.bnTextOut.setText(RESOURCES.getString("base_name_text"));
        this.classpath.setText(RESOURCES.getString("classpath_pane_title"));
        this.removeClass.setText(RESOURCES.getString("remove"));
        this.editEff.setText(RESOURCES.getString("edit"));
        this.editYaml.setText(RESOURCES.getString("edit"));
        this.eff.setText(RESOURCES.getString("eff_pane_title"));
        this.endTime.setText(RESOURCES.getString("end_time"));
        this.intOut.setText(RESOURCES.getString("interval"));
        this.newEff.setText(RESOURCES.getString("new"));
        this.newYaml.setText(RESOURCES.getString("new"));
        this.output.setText(RESOURCES.getString("out_pane_title"));
        this.setEff.setText(RESOURCES.getString("set"));
        this.setOut.setText(RESOURCES.getString("set_folder"));
        this.setYaml.setText(RESOURCES.getString("set"));
        this.simConf.setText(RESOURCES.getString("sim_pane_title"));
        this.spinBatch.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 
                        Runtime.getRuntime().availableProcessors() + 1,
                        Runtime.getRuntime().availableProcessors() + 1, 
                        1));
        this.spinOut.setEditable(true);
        this.spinOut.setValueFactory(
                new SpinnerValueFactory.DoubleSpinnerValueFactory(MIN, MAX_SAM, 1, STEP));
        this.spinTime.setEditable(true);
        this.spinTime.setValueFactory(
                new SpinnerValueFactory.DoubleSpinnerValueFactory(MIN, Double.POSITIVE_INFINITY, VALUE_TIME, STEP));
        this.thread.setText(RESOURCES.getString("n_thread"));
        this.unitOut.setText(RESOURCES.getString("sec"));
        this.unitTime.setText(RESOURCES.getString("sec"));
        if (this.listClass.getItems().size() == 0) {
            this.removeClass.setDisable(true);
        }
    }

    /**
     * Sets the main class and adds toggle switch to view.
     * @param main main class.
     */
    public void setMain(final ProjectGUI main) {
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
            this.imgViewOut.setVisible(vis);
        } else {
            if (ts.isSelected() && this.pathYaml.getText().equals("")) {
                setAlert(RESOURCES.getString("file_no_selected"), RESOURCES.getString("yaml_no_selected_header"), RESOURCES.getString("yaml_no_selected_content"));
                setVisibilityBatch(false);
                ts.setSelected(false);
            } else {
                setVisibilityBatch(vis);
                if (ts.isSelected()) {
                    setVariablesList();
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
    public void setVariablesList() {
        Loader fileYaml = null;
        try {
            fileYaml = new YamlLoader(new FileReader(this.ctrlLeft.getPathFolder() + File.separator + this.pathYaml.getText().replace("/", File.separator)));
        } catch (FileNotFoundException e) {
            L.error("Error loading simulation file.", e);
        }
        if (fileYaml != null) {
            final ObservableList<String> vars = FXCollections.observableArrayList();
            vars.addAll(fileYaml.getVariables().keySet());
            if (this.variables.isEmpty()) {
                for (final String s: vars) {
                    this.variables.put(s, false);
                }
            }
            this.listYaml.setItems(vars);
            this.listYaml.setCellFactory(CheckBoxListCell.forListView(new Callback<String, ObservableValue<Boolean>>() {
                @Override
                public ObservableValue<Boolean> call(final String var) {
                    final BooleanProperty observable = new SimpleBooleanProperty();
                    if (variables.get(var)) {
                        observable.set(true);
                    }
                    observable.addListener((obs, wasSelected, isNowSelected) -> {
                            if (wasSelected && !isNowSelected) {
                                variables.put(var, false);
                            }
                            if (!wasSelected && isNowSelected) {
                                variables.put(var, true);
                            }
                        }
                    );
                    return observable;
                }
            }));
        }
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
            setAlert(RESOURCES.getString("folder_no_selected"), RESOURCES.getString("folder_no_selected_header"), RESOURCES.getString("folder_no_selected_content"));
        } else if (!FilenameUtils.getExtension(this.ctrlLeft.getSelectedFilePath()).isEmpty()) {
            setAlert(RESOURCES.getString("wrong_selection"), RESOURCES.getString("wrong_selection_header"), RESOURCES.getString("wrong_selection_content"));
        } else {
            File file = new File(this.ctrlLeft.getSelectedFilePath());
            while (!file.getName().equals(RESOURCES.getString("folder_output")) && !file.getName().equals(new File(this.ctrlLeft.getPathFolder()).getName())) {
                    file = file.getParentFile();
            }
            if (file.getName().equals(RESOURCES.getString("folder_output"))) {
                this.pathOut.setText(new File(this.ctrlLeft.getPathFolder()).toURI().relativize(new File(this.ctrlLeft.getSelectedFilePath()).toURI()).getPath());
                setDeleteIcon(this.gridOut, this.pathOut, this.imgViewOut, false);
            } else {
                setAlert(RESOURCES.getString("folder_wrong"), RESOURCES.getString("folder_wrong_header"), RESOURCES.getString("folder_wrong_content"));
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

        if (this.ctrlLeft.getSelectedFilePath() == null) {
            setAlert(RESOURCES.getString("file_no_selected"), RESOURCES.getString("file_no_selected_header"), RESOURCES.getString("file_no_selected_content"));
        } else { 
            if (!this.data.contains(this.ctrlLeft.getSelectedFilePath())) {
                if (addPath(this.ctrlLeft.getSelectedFilePath())) {
                    this.data.add(new File(this.ctrlLeft.getPathFolder()).toURI().relativize(new File(this.ctrlLeft.getSelectedFilePath()).toURI()).getPath());
                    this.listClass.setItems(data);
                } else {
                    final Alert alertCancel = new Alert(AlertType.ERROR);
                    alertCancel.setTitle(RESOURCES.getString("error_adding_classpath"));
                    alertCancel.setHeaderText(RESOURCES.getString("error_adding_classpath_header"));
                    alertCancel.setContentText(RESOURCES.getString("error_adding_classpath_content"));
                    alertCancel.showAndWait();
                }
            } else {
                setAlert(RESOURCES.getString("file_name_exists"), RESOURCES.getString("file_name_class_header"), RESOURCES.getString("file_name_class_content"));
            }
        }
    }

    /**
     * 
     */
    @FXML
    public void clickRemoveClass() {
        final String nameFile = this.listClass.getSelectionModel().getSelectedItem();
        if (removePath(this.ctrlLeft.getPathFolder() + File.separator + nameFile.replace("/", File.separator))) {
            this.listClass.getItems().remove(nameFile);
            if (this.listClass.getItems().size() == 0) {
                this.removeClass.setDisable(true);
            }
        } else {
            final Alert alertCancel = new Alert(AlertType.ERROR);
            alertCancel.setTitle(RESOURCES.getString("error_removing_classpath"));
            alertCancel.setHeaderText(RESOURCES.getString("error_removing_classpath_header"));
            alertCancel.setContentText(RESOURCES.getString("error_removing_classpath_content"));
            alertCancel.showAndWait();
        }
    }

    /**
     * 
     */
    @FXML
    public void clickBatch() {
        final Project project = ProjectIOUtils.loadFrom(this.ctrlLeft.getPathFolder());
        try {
            project.runAlchemistSimulation(true);
        } catch (FileNotFoundException e) {
            L.error("Error loading simulation file.", e);
        }
    }

    /**
     * 
     */
    public void setEnableGrid() {
        this.grid.setDisable(false);
    }

    /**
     * 
     * @return Selected simulation path.
     */
    public String getSimulationFilePath() {
        return this.pathYaml.getText();
    }

    /**
     * 
     * @param path The path of file simulation.
     */
    private void setSimulationFilePath(final String path) {
        this.pathYaml.setText(path);
        setDeleteIcon(this.gridYaml, this.pathYaml, this.imgViewYaml, true);
    }

    /**
     * 
     * @return Selected end time.
     */
    public double getEndTime() {
        return this.spinTime.getValueFactory().getValue();
    }

    /**
     * 
     * @param endT Selected end time.
     */
    private void setEndTime(final double endT) {
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
    private void setEffect(final String path) {
        this.pathEff.setText(path);
        setDeleteIcon(this.gridEff, this.pathEff, this.imgViewEff, false);
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
    private void setSwitchOutputSelected(final boolean select) {
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
    private void setOutputFolder(final String path) {
        this.pathOut.setText(path);
        setDeleteIcon(this.gridOut, this.pathOut, this.imgViewOut, false);
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
    private void setBaseName(final String name) {
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
    private void setSamplInterval(final double sampInt) {
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
    private void setSwitchBatchSelected(final boolean select) {
        this.tsVar.setSelected(select);
    }

    /**
     * 
     * @return a map of variables.
     */
    public Map<String, Boolean> getVariables() {
        return this.variables;
    }

    private void setVariables(final Map<String, Boolean> vars) {
        this.variables = vars;
    }

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
    private void setNumberThreads(final int threads) {
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
    private void setClasspath(final ObservableList<String> list) {
        final List<String> listLibError = new ArrayList<>(); 
        for (final String lib: list) {
            if (!addPath(this.ctrlLeft.getPathFolder() + File.separator + lib.replace("/", File.separator))) {
                listLibError.add(new File(lib).getName());
            } else {
                this.data.add(lib);
            }
        }
        this.listClass.setItems(this.data);
        if (this.listClass.getItems().size() != 0) {
            this.removeClass.setDisable(false);
        }
        if (!listLibError.isEmpty()) {
            String content = RESOURCES.getString("error_adding_classpath_content") + System.getProperty("line.separator");
            for (final String lib : listLibError) {
                content = content + System.getProperty("line.separator") + "- " + lib;
            }
            final Alert alertCancel = new Alert(AlertType.ERROR);
            alertCancel.setTitle(RESOURCES.getString("error_adding_classpath"));
            alertCancel.setHeaderText(RESOURCES.getString("error_adding_classpath_header"));
            alertCancel.setContentText(content);
            alertCancel.showAndWait();
        }
    }

    /**
     * 
     * @return The entity project.
     */
    public Project setField() {
        Project project = ProjectIOUtils.loadFrom(this.ctrlLeft.getPathFolder());
        if (project != null) {
            if (project.getBatch() != null && project.getBatch().getVariables() != null) {
                project.filterVariables();
            }
            if (project.getSimulation() != null && !project.getSimulation().isEmpty()) {
                if (!new File(project.getBaseDirectory() + File.separator + project.getSimulation()).exists()) {
                    setAlert(RESOURCES.getString("file_not_found"), RESOURCES.getString("file_yaml_not_found_header"), RESOURCES.getString("file_not_found_content"));
                } else {
                    setSimulationFilePath(project.getSimulation());
                }
            }
            if (project.getEndTime() != 0) {
                setEndTime(project.getEndTime());
            } else {
                setAlert(RESOURCES.getString("end_time_not_found"), RESOURCES.getString("end_time_not_found_header"), RESOURCES.getString("end_time_not_found_content"));
                setEndTime(VALUE_TIME);
            }
            if (project.getEffect() != null && !project.getEffect().isEmpty()) {
                if (!new File(project.getBaseDirectory() + File.separator + project.getEffect()).exists()) {
                    setAlert(RESOURCES.getString("file_not_found"), RESOURCES.getString("file_json_not_found_header"), RESOURCES.getString("file_not_found_content"));
                } else {
                    setEffect(project.getEffect());
                }
            }
            if (project.getOutput() != null) {
                setSwitchOutputSelected(project.getOutput().isSelected());
                if (project.getOutput().getFolder() != null && !project.getOutput().getFolder().isEmpty()) {
                    if (!new File(project.getBaseDirectory() + File.separator + project.getOutput().getFolder()).exists()) {
                        if (isSwitchOutputSelected()) {
                            setAlert(RESOURCES.getString("folder_not_found"), RESOURCES.getString("folder_out_not_found_header"), RESOURCES.getString("folder_out_not_found_content"));
                        }
                        setSwitchOutputSelected(false);
                    } else {
                        setOutputFolder(project.getOutput().getFolder());
                    }
                }
                if (project.getOutput().getBaseName() != null && !project.getOutput().getBaseName().isEmpty()) {
                    setBaseName(project.getOutput().getBaseName());
                } else {
                    if (isSwitchOutputSelected()) {
                        setAlert(RESOURCES.getString("base_name_not_found"), RESOURCES.getString("base_name_not_found_header"), RESOURCES.getString("base_name_not_found_content"));
                    }
                    setBaseName(RESOURCES.getString("base_name_text"));
                }
                if (project.getOutput().getSampleInterval() == 0) {
                    if (isSwitchOutputSelected()) {
                        setAlert(RESOURCES.getString("samp_interval_not_found"), RESOURCES.getString("samp_interval_not_found_header"), RESOURCES.getString("samp_interval_not_found_content"));
                    }
                    setSamplInterval(1);
                } else {
                    setSamplInterval(project.getOutput().getSampleInterval());
                }
            }
            if (project.getBatch() != null) {
                setSwitchBatchSelected(project.getBatch().isSelected());
                if (project.getBatch().getVariables() != null) {
                    setVariables(project.getBatch().getVariables());
                } else {
                    setSwitchBatchSelected(false);
                    setAlert(RESOURCES.getString("var_not_found"), RESOURCES.getString("var_not_found_header"), RESOURCES.getString("var_not_found_content"));
                }
                if (project.getBatch().getThreadCount() == 0) {
                    if (isSwitchBatchSelected()) {
                        setAlert(RESOURCES.getString("n_thread_not_found"), RESOURCES.getString("n_thread_not_found_header"), RESOURCES.getString("n_thread_not_found_content"));
                    }
                    setNumberThreads(Runtime.getRuntime().availableProcessors() + 1);
                } else {
                    setNumberThreads(project.getBatch().getThreadCount());
                }
            }
            if (project.getClasspath() != null && !project.getClasspath().isEmpty()) {
                    final ObservableList<String> list = FXCollections.observableArrayList();
                    for (final String lib : project.getClasspath()) {
                        if (!new File(project.getBaseDirectory() + File.separator + lib).exists()) {
                            setAlert(RESOURCES.getString("library_not_found"), lib + " " + RESOURCES.getString("library_not_found_header"), RESOURCES.getString("library_not_found_content"));
                        } else {
                            list.add(lib);
                        }
                    }
                    setClasspath(list);
            }
        } else {
            project = new Project(new File(this.ctrlLeft.getPathFolder()));
        }
        if (this.grid.isDisable()) {
            setEnableGrid();
        }
        this.ctrlLeft.setEnableRun();
        return project;
    }

    /**
     * 
     * @param path A path of output folder
     */
    public void setFolderAfterDelete(final Path path) {
        if (!getOutputFolder().isEmpty() && getOutputFolder().equals(new File(this.ctrlLeft.getPathFolder()).toURI().relativize(path.toUri()).getPath() + "/")) {
            setAlert(RESOURCES.getString("folder_not_found"), RESOURCES.getString("folder_out_not_found_header"), RESOURCES.getString("folder_out_not_found_content"));
            setSwitchOutputSelected(false);
            this.gridOut.getChildren().remove(this.imgViewOut);
            this.pathOut.setText("");
        } else if (!getClasspath().isEmpty()) {
            final String folder = findItemInList(path);
            if (!folder.isEmpty()) {
                setAlert(RESOURCES.getString("library_not_found"), folder + " " + RESOURCES.getString("library_not_found_header"), RESOURCES.getString("library_not_found_content"));
                this.listClass.getItems().remove(folder);
            }
        }
    }

    /**
     * 
     * @param path A path of file
     */
    public void setFileAfterDelete(final Path path) {
        if (!getSimulationFilePath().isEmpty() && getSimulationFilePath().equals(new File(this.ctrlLeft.getPathFolder()).toURI().relativize(path.toUri()).getPath())) {
            setAlert(RESOURCES.getString("file_not_found"), RESOURCES.getString("file_yaml_not_found_header"), RESOURCES.getString("file_not_found_content"));
            this.pathYaml.setText("");
            this.gridYaml.getChildren().remove(this.imgViewYaml);
        } else if (!getEffect().isEmpty() && getEffect().equals(new File(this.ctrlLeft.getPathFolder()).toURI().relativize(path.toUri()).getPath())) {
            setAlert(RESOURCES.getString("file_not_found"), RESOURCES.getString("file_json_not_found_header"), RESOURCES.getString("file_not_found_content"));
            this.pathEff.setText("");
            this.gridEff.getChildren().remove(this.imgViewEff);
        } else if (!getClasspath().isEmpty()) {
            final String folder = findItemInList(path);
            if (!folder.isEmpty()) {
                setAlert(RESOURCES.getString("library_not_found"), folder + " " + RESOURCES.getString("library_not_found_header"), RESOURCES.getString("library_not_found_content"));
                this.listClass.getItems().remove(folder);
            }
        }
    }

    private String findItemInList(final Path path) {
        for (final String s : getClasspath()) {
            if (s.equals(new File(this.ctrlLeft.getPathFolder()).toURI().relativize(path.toUri()).getPath())) {
                return s;
            }
        }
        return "";
    }

    private void manageFile(final String extension, final boolean edit) {
        if (this.ctrlLeft.getSelectedFilePath() == null) {
            setAlert(RESOURCES.getString("file_no_selected"), RESOURCES.getString("file_no_selected_header"), RESOURCES.getString("file_no_selected_content"));
        } else if (this.ctrlLeft.getSelectedFilePath().endsWith(extension)) {
            if (extension.equals(YAML_EXT) && !edit) {
                this.pathYaml.setText(new File(this.ctrlLeft.getPathFolder()).toURI().relativize(new File(this.ctrlLeft.getSelectedFilePath()).toURI()).getPath());
                setDeleteIcon(this.gridYaml, this.pathYaml, this.imgViewYaml, true);
            } else if (extension.equals(EFF_EXT) && !edit) {
                this.pathEff.setText(new File(this.ctrlLeft.getPathFolder()).toURI().relativize(new File(this.ctrlLeft.getSelectedFilePath()).toURI()).getPath());
                setDeleteIcon(this.gridEff, this.pathEff, this.imgViewEff, false);
            } else {
                editFile();
            }
        } else {
            if (extension.equals(YAML_EXT)) {
                setAlert(RESOURCES.getString("file_wrong"), RESOURCES.getString("file_wrong_yaml_header"), RESOURCES.getString("file_wrong_content"));
            } else {
                setAlert(RESOURCES.getString("file_wrong"), RESOURCES.getString("file_wrong_effect_header"), RESOURCES.getString("file_wrong_content"));
            }
        }
    }

    private void newFile(final String extension) {
        try {
            final FXMLLoader loader = new FXMLLoader();
            loader.setLocation(ProjectGUI.class.getResource("view/FileNameDialog.fxml"));
            final AnchorPane pane = (AnchorPane) loader.load();

            final Stage stage = new Stage();
            stage.setTitle(RESOURCES.getString("file_name_title"));
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

    private void setDeleteIcon(final GridPane grid, final Label label, final ImageView imgView, final boolean isYaml) {
        //final ImageView imgView = new ImageView(new Image(ProjectGUI.class.getResource("/icon/icon-delete.png").toExternalForm()));
        final Tooltip tooltip = new Tooltip();
        tooltip.setText(RESOURCES.getString("delete"));
        Tooltip.install(imgView, tooltip);
        grid.getChildren().remove(imgView);
        grid.add(imgView, 0, 2);
        imgView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent event) {
                label.setText(""); 
                grid.getChildren().remove(imgView);
                if (isYaml) {
                    setSwitchBatchSelected(false);
                }
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

    private boolean addPath(final String path) {
        URL url = null;
        try {
            url = new File(path).toURI().toURL();
        } catch (MalformedURLException e) {
            L.error("Error during the construction of the URL.", e);
        }
        if (url != null) {
            final ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
            if (systemClassLoader instanceof URLClassLoader) {
                final URLClassLoader urlClassLoader = (URLClassLoader) systemClassLoader;
                final Class<?> urlClass = URLClassLoader.class;
                final Method method;
                try {
                    method = urlClass.getDeclaredMethod("addURL", new Class[]{URL.class});
                    method.setAccessible(true);
                    try {
                        method.invoke(urlClassLoader, new Object[]{url});
                        return true;
                    } catch (IllegalAccessException e) {
                        L.error("Error because the method is inaccessible.", e);
                    } catch (IllegalArgumentException e) {
                        L.error("Error because the objects are not an instance of the method.", e);
                    } catch (InvocationTargetException e) {
                        L.error("Error during invoke of method.", e);
                    }
                } catch (NoSuchMethodException e) {
                    L.error("Error because no methods matching with \"addURL\".", e);
                } catch (SecurityException e) {
                    L.error("Error because there is a security manager.", e);
                }
            }
        }
        return false;
    }

    private boolean removePath(final String path) {
        URL url = null;
        try {
            url = new File(path).toURI().toURL();
        } catch (MalformedURLException e) {
            L.error("Error during the construction of the URL.", e);
        }
        if (url != null) {
        final ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
            if (systemClassLoader instanceof URLClassLoader) {
                final URLClassLoader urlClassLoader = (URLClassLoader) systemClassLoader;
                final Class<?> urlClass = URLClassLoader.class;
                final Field ucpField;
                try {
                    ucpField = urlClass.getDeclaredField("ucp");
                    ucpField.setAccessible(true);
                    try {
                        URLClassPath field = (URLClassPath) ucpField.get(urlClassLoader);
                        if (field instanceof URLClassPath) {
                            try {
                                final URLClassPath ucp = field;
                                final Class<?> ucpClass = URLClassPath.class;
                                final Field urlsField = ucpClass.getDeclaredField("urls");
                                urlsField.setAccessible(true);
                                final Stack<?> fieldStack = (Stack<?>) urlsField.get(ucp);
                                if (fieldStack instanceof Stack) {
                                    final Stack<?> urls = fieldStack;
                                    urls.remove(url);
                                    return true;
                                }
                            } catch (IllegalArgumentException e) {
                                L.error("Error because the objects are not an instance of the field.", e);
                            } catch (IllegalAccessException e) {
                                L.error("Error because the method is inaccessible.", e);
                            }
                        }
                    } catch (IllegalArgumentException e1) {
                        L.error("Error because the objects are not an instance of the field.", e1);
                    } catch (IllegalAccessException e1) {
                        L.error("Error because the field is inaccessible.", e1);
                    }
                } catch (NoSuchFieldException e) {
                    L.error("Error because no fields matching with \"ucp\".", e);
                } catch (SecurityException e) {
                    L.error("Error because there is a security manager.", e);
                }
            }
        }
        return false;
    }

}
