package it.unibo.alchemist.boundary.projectview.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import it.unibo.alchemist.AlchemistRunner;
import it.unibo.alchemist.boundary.l10n.LocalizedResourceBundle;
import it.unibo.alchemist.loader.Loader;
import it.unibo.alchemist.loader.YamlLoader;
import it.unibo.alchemist.loader.variables.Variable;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * An entity which is able to produce a structure for an Alchemist project to go through a Json reader or writer.
 *
 */
public final class Project {

    private static final transient ResourceBundle RESOURCES = LocalizedResourceBundle.get("it.unibo.alchemist.l10n.ProjectViewUIStrings");

    private final transient File baseDir;
    private String simulation;
    private double endTime;
    private String effect;
    private Output output;
    private Batch batch;
    private List<String> classpath;

    /**
     * 
     * @param baseDir a base directory of project.
     */
    public Project(final File baseDir) {
        this.baseDir = Objects.requireNonNull(baseDir, "The base directory of project can't be null.");
        if (!this.baseDir.exists() || !this.baseDir.isDirectory()) {
            throw new IllegalArgumentException(baseDir + " is not a valid base directory for a project");
        }
    }

    /**
     * 
     * @return an entity of Batch mode.
     */
    public Batch getBatch() {
        return this.batch;
    }

    /**
     * 
     * @return a list of the libraries to add to the classpath.
     */
    public List<String> getClasspath() {
        return this.classpath;
    }

    /**
     * 
     * @return a end time of simulation.
     */
    public double getEndTime() {
        return this.endTime;
    }

    /**
     * 
     * @return a path of effect file.
     */
    public String getEffect() {
        return this.effect;
    }

    /**
     * 
     * @return an entity of the Output.
     */
    public Output getOutput() {
        return this.output;
    }

    /**
     * 
     * @return a path of simulation file.
     */
    public String getSimulation() {
        return this.simulation;
    }

    /**
     * 
     * @param batch a entity of Batch mode.
     */
    public void setBatch(final Batch batch) {
        this.batch = batch;
    }

    /**
     * 
     * @param classpath a list of libraries.
     */
    public void setClasspath(final List<String> classpath) {
        this.classpath = classpath;
    }

    /**
     * 
     * @param endTime an end time.
     */
    public void setEndTime(final double endTime) {
        this.endTime = endTime;
    }

    /**
     * 
     * @param eff a path of a effect file.
     */
    public void setEffect(final String eff) {
        this.effect = eff;
    }

    /**
     * 
     * @param out an entity of Output.
     */
    public void setOutput(final Output out) {
        this.output = out;
    }

    /**
     * 
     * @param sim a path of a simulation file.
     */
    public void setSimulation(final String sim) {
        this.simulation = sim;
    }

    /**
     * 
     * @return the base directory of project.
     */
    public File getBaseDirectory() {
        return baseDir;
    }

    /**
     * @throws FileNotFoundException 
     * 
     */
    public void filterVariables() throws FileNotFoundException {
        /* TODO
         * 1. Create a dry-run
         * 2. Get the variables from file
         * 3. populate the variables (keys from file, values from existing)
         */
        final Loader loader = new YamlLoader(new FileInputStream(getSimulationPath()));
        final AlchemistRunner runner = new AlchemistRunner.Builder(loader).build();
        final Map<String, Boolean> vars = Collections.unmodifiableMap(this.batch.getVariables());
        this.batch.setVariables(runner.getVariables().keySet().stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        key -> vars.getOrDefault(key, false))));
//        for (final Map.Entry<String, Variable> entryVarFile : keys.entrySet()) {
//            boolean find = false;
//            for (final Map.Entry<String, Boolean> entryVar : getBatch().getVariables().entrySet()) {
//                if (entryVarFile.getKey().equals(entryVar.getKey())) {
//                    this.variables.put(entryVarFile.getKey(), true);
//                    find = true;
//                }
//            }
//            if (!find) {
//                this.variables.put(entryVarFile.getKey(), false);
//            }
//        }
    }

    /**
     * 
     * @param isBatch A boolean that represent if it is in batch mode.
     * @throws FileNotFoundException When the selected files are not found.
     */
    public void runAlchemistSimulation(final boolean isBatch) throws FileNotFoundException {
        /* TODO
         * 1. Set the new classpath
         * 2. Configure the builder
         * 3. Extract the variables
         */

        /* TODO:
         * 1. Try to use resourceloader "/it/unibo/images/pluto.png" getResource() -- getResourceAsStream()
         * 2. If it fails, use file access
         */
        final AlchemistRunner runner = new AlchemistRunner.Builder(new YamlLoader(new FileInputStream(getSimulationPath())))
            .setEndTime(new DoubleTime(getEndTime()))
            .setEffects(getEffectPath())
            .setOutputFile(getFolderPath())
            .setInterval(getOutput().getSampleInterval())
            .setParallelism(getBatch().getThreadCount())
            .setHeadless(false)
            .build();

        final Map<String, Variable> keys = runner.getVariables();
        final Set<String> selectedVariables = isBatch 
                ? this.batch.getVariables().entrySet().stream().filter(Entry::getValue).map(Entry::getKey).collect(Collectors.toSet())
                : Collections.emptySet();
        if (keys.keySet().containsAll(selectedVariables)) {
            runner.launch(selectedVariables.toArray(new String[selectedVariables.size()]));
        } else {
            final Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle(RESOURCES.getString("var_key_error"));
            alert.setHeaderText(RESOURCES.getString("var_key_error_header"));
            alert.setContentText(RESOURCES.getString("var_key_error_content"));
            alert.showAndWait();
        }
//        if (!this.variables.isEmpty()) {
//            final List<String> list = new ArrayList<>();
//            for (final Map.Entry<String, Boolean> entry : this.variables.entrySet()) {
//                if (entry.getValue()) {
//                    list.add(entry.getKey());
//                }
//            }
//
//            if (!list.isEmpty()) {
//                String[] vars = new String[list.size()];
//                int i = 0;
//                for (final String s : list) {
//                    vars[i++] = s;
//                }
//                runner.launch(vars);
//            }
//        } else {
//            runner.launch();
//        }
    }

    //TODO: check if getSimulation returns null
    private String getSimulationPath() {
        return this.baseDir + File.separator + (getSimulation().replace("/", File.separator));
    }

    //TODO: check if getEffect returns null
    private String getEffectPath() {
        if (getEffect().equals("")) {
            return "";
        } else {
            return this.baseDir + File.separator + (getEffect().replace("/", File.separator));
        }
    }

    //TODO: check if getFolder() and getBaseName() return null
    private String getFolderPath() {
        if (this.output.isSelected()) {
            return this.baseDir + File.separator + getOutput().getFolder() + File.separator + getOutput().getBaseName();
        } else {
            return "";
        }
    }

}
