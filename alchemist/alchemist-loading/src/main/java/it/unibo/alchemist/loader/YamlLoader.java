package it.unibo.alchemist.loader;

import static java.util.ResourceBundle.getBundle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.danilopianini.lang.PrimitiveUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;


import it.unibo.alchemist.SupportedIncarnations;
import it.unibo.alchemist.loader.displacements.Displacement;
import it.unibo.alchemist.loader.export.Extractor;
import it.unibo.alchemist.loader.export.FilteringPolicy;
import it.unibo.alchemist.loader.export.MoleculeReader;
import it.unibo.alchemist.loader.export.NumberOfNodes;
import it.unibo.alchemist.loader.export.filters.CommonFilters;
import it.unibo.alchemist.loader.shapes.Shape;
import it.unibo.alchemist.loader.variables.ArbitraryVariable;
import it.unibo.alchemist.loader.variables.DependentScriptVariable;
import it.unibo.alchemist.loader.variables.DependentVariable;
import it.unibo.alchemist.loader.variables.LinearVariable;
import it.unibo.alchemist.loader.variables.Variable;
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment;
import it.unibo.alchemist.model.implementations.linkingrules.NoLinks;
import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Action;
import it.unibo.alchemist.model.interfaces.Condition;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Incarnation;
import it.unibo.alchemist.model.interfaces.Layer;
import it.unibo.alchemist.model.interfaces.LinkingRule;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;
import it.unibo.alchemist.model.interfaces.TimeDistribution;

/**
 * Loads a properly formatted YAML file and provides method for instancing a batch of scenarios.
 */
public class YamlLoader implements Loader, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -8503453282930319680L;
    private static final String ALCHEMIST_PACKAGE_ROOT = "it.unibo.alchemist.";
    private static final String SYNTAX_NAME = "YamlSyntax";
    private static final ResourceBundle SYNTAX = getBundle(YamlLoader.class.getPackage().getName() + '.' + SYNTAX_NAME, Locale.US);
    private static final String ACTIONS = SYNTAX.getString("actions");
    private static final String ACTIONS_PACKAGE_ROOT = ALCHEMIST_PACKAGE_ROOT + "model.implementations.actions.";
    private static final String AGGREGATORS = SYNTAX.getString("aggregators");
    private static final String CONCENTRATION = SYNTAX.getString("concentration");
    private static final String CONCENTRATIONS_PACKAGE_ROOT = ALCHEMIST_PACKAGE_ROOT + "model.implementations.concentrations.";
    private static final String CONDITIONS = SYNTAX.getString("conditions");
    private static final String CONDITIONS_PACKAGE_ROOT = ALCHEMIST_PACKAGE_ROOT + "model.implementations.conditions.";
    private static final String CONTENTS = SYNTAX.getString("contents");
    private static final String DEFAULT = SYNTAX.getString("default");
    private static final String DISPLACEMENTS = SYNTAX.getString("displacements");
    private static final String DISPLACEMENTS_PACKAGE_ROOT = ALCHEMIST_PACKAGE_ROOT + "loader.displacements.";
    private static final String ENV_PACKAGE_ROOT = ALCHEMIST_PACKAGE_ROOT + "model.implementations.environments.";
    private static final String ENVIRONMENT = SYNTAX.getString("environment");
    private static final String EXPORT = SYNTAX.getString("export");
    private static final String FORMULA = SYNTAX.getString("formula");
    private static final String IN = SYNTAX.getString("in");
    private static final String INCARNATION = SYNTAX.getString("incarnation");
    private static final String LAYERS = SYNTAX.getString("layers");
    private static final String LAYERS_PACKAGE_ROOT = ALCHEMIST_PACKAGE_ROOT + "model.implementations.layers.";
    private static final String LINKING_PACKAGE_ROOT = ALCHEMIST_PACKAGE_ROOT + "model.implementations.linkingrules.";
    private static final String LINKING_RULE = SYNTAX.getString("linking-rule");
    private static final String MIN = SYNTAX.getString("min");
    private static final String MAX = SYNTAX.getString("max");
    private static final String MOLECULE = SYNTAX.getString("molecule");
    private static final String MOLECULES_PACKAGE_ROOT = ALCHEMIST_PACKAGE_ROOT + "model.implementations.molecules.";
    private static final String NODE = SYNTAX.getString("node");
    private static final String NODES = SYNTAX.getString("nodes");
    private static final String NODES_PACKAGE_ROOT = ALCHEMIST_PACKAGE_ROOT + "model.implementations.nodes.";
    private static final String PARAMS = SYNTAX.getString("parameters");
    private static final String POSITIONS = SYNTAX.getString("positions");
    private static final String POSITIONS_PACKAGE_ROOT = ALCHEMIST_PACKAGE_ROOT + "model.implementations.positions.";
    private static final String PROGRAMS = SYNTAX.getString("programs");
    private static final String PROPERTY = SYNTAX.getString("property");
    private static final String REACTION = SYNTAX.getString("reaction");
    private static final String REACTIONS_PACKAGE_ROOT = ALCHEMIST_PACKAGE_ROOT + "model.implementations.reactions.";
    private static final String SCENARIO_SEED = SYNTAX.getString("scenario-seed");
    private static final String SEEDS = SYNTAX.getString("seeds");
    private static final String SHAPES_PACKAGE_ROOT = ALCHEMIST_PACKAGE_ROOT + "loader.shapes.";
    private static final String SIMULATION_SEED = SYNTAX.getString("simulation-seed");
    private static final String STEP = SYNTAX.getString("step");
    private static final String TIME = SYNTAX.getString("time");
    private static final String TIMEDISTRIBUTION = SYNTAX.getString("time-distribution");
    private static final String TIMEDISTRIBUTIONS_PACKAGE_ROOT = ALCHEMIST_PACKAGE_ROOT + "model.implementations.timedistributions.";
    private static final String TYPE = SYNTAX.getString("type");
    private static final String VALUES = SYNTAX.getString("values");
    private static final String VALUE_FILTER = SYNTAX.getString("value-filter");
    private static final String VALUE_FILTER_PACKAGE_ROOT = ALCHEMIST_PACKAGE_ROOT + "loader.export.filters";
    private static final String VARIABLES = SYNTAX.getString("variables");
    private static final String VARIABLE = "is a variable";

    private static final Logger L = LoggerFactory.getLogger(YamlLoader.class);
    private static final String UNCHECKED = "unchecked";

    @SuppressWarnings({ UNCHECKED, "rawtypes" })
    private static final Class<? extends Environment<?>> DEFAULT_ENVIRONMENT_CLASS =
        (Class<? extends Environment<?>>) (Class<? extends Environment>) Continuous2DEnvironment.class;
    @SuppressWarnings({ UNCHECKED, "rawtypes" })
    private static final Class<? extends LinkingRule<?>> DEFAULT_LINKING_CLASS =
        (Class<? extends LinkingRule<?>>) (Class<? extends LinkingRule>) NoLinks.class;
    private static final Class<? extends Position> DEFAULT_POSITION_CLASS = Continuous2DEuclidean.class;
    private static final Shape IN_ALL = (p) -> true;

    private Object simulationSeed;
    private Object scenarioSeed;
    private final Map<Long, String> reverseLookupTable;
    private final Map<String, Variable> lookupTable;
    private final Map<String, DependentVariable> computableVariables;
    private final List<Extractor> extractors;
    private final Class<? extends Environment<?>> envClass;
    private final Class<? extends Position> posClass;
    private final Class<? extends LinkingRule<?>> linkingClass;
    private final List<?> envArgs;
    private final List<?> linkingArgs;
    private List<?> layersList = new ArrayList<>();

    private final List<Map<String, Object>> displacements;

    private transient Incarnation<?> incarnation;

    private class PlaceHolder {
        private final String str;

        PlaceHolder(final String str) {
            this.str = str;
        }

        private String get() {
            return str;
        }

        @Override
        public String toString() {
            return str + ":" + lookupTable.get(str);
        }
    }

    /**
     * @param source
     *            the YAML file
     */
    public YamlLoader(final InputStream source) {
        this(new BufferedReader(new InputStreamReader(source, Charsets.UTF_8)));
    }

    /**
     * @param source
     *            the YAML file
     */
    @SuppressWarnings(UNCHECKED)
    public YamlLoader(final Reader source) {
        final Yaml yaml = new Yaml();
        final Object yamlObj = yaml.load(source);
        L.debug("Parsed yaml: {}", yamlObj);
        if (!(yamlObj instanceof Map)) {
            throw new IllegalArgumentException("Not a valid Alchemist YAML file.");
        }
        final Map<String, Object> contents = (Map<String, Object>) yamlObj;
        /*
         * Extract variables
         */
        final Object varObj = contents.get(VARIABLES);
        if (varObj != null && !(varObj instanceof Map)) {
            throw new IllegalAlchemistYAMLException(
                    "Variables configuration is wrong. A YAML map was expected, instead of " + varObj);
        }
        final Map<String, Map<String, Object>> originalVars = Optional
                .ofNullable((Map<String, Map<String, Object>>) varObj)
                .orElse(Collections.emptyMap());
        L.debug("Variables: {}", originalVars);
        reverseLookupTable = originalVars.entrySet().stream()
                .collect(Collectors.toMap(e -> univoqueId(e.getValue()), Entry::getKey));
        L.debug("Reverse lookup table: {}", reverseLookupTable);
        lookupTable = originalVars.entrySet().stream()
                .filter(e -> e.getValue() instanceof Map && !((Map<?, ?>) e.getValue()).containsKey(FORMULA))
                .collect(Collectors.toMap(Entry::getKey, e -> makeVar(e.getValue())));
        L.debug("Lookup table: {}", lookupTable);
        computableVariables = originalVars.entrySet().stream()
                .filter(e -> e.getValue() instanceof Map && ((Map<?, ?>) e.getValue()).containsKey(FORMULA))
                .collect(Collectors.toMap(Entry::getKey, e -> makeDepVar(e.getValue())));
        /*
         * Incarnation
         */
        final Object incObj = contents.get(INCARNATION);
        if (incObj == null) {
            throw new IllegalAlchemistYAMLException("You must specify an incarnation.",
                    new IllegalStateException("No incarnation specified in YAML simulation file"));
        }
        incarnation = SupportedIncarnations.get(incObj.toString())
                .orElseThrow(() -> new IllegalStateException(incObj
                        + " is not a valid incarnation. Supported incarnations are: "
                        + SupportedIncarnations.getAvailableIncarnations()));
        /*
         * RNG
         */
        final Object seedMapObj = contents.get(SEEDS);
        if (seedMapObj instanceof Map) {
            final Map<String, Object> seedMap = (Map<String, Object>) seedMapObj;
            simulationSeed = Optional.ofNullable(makePlaceHolderIfNeeded(seedMap.get(SIMULATION_SEED))).orElse(0);
            scenarioSeed = Optional.ofNullable(makePlaceHolderIfNeeded(seedMap.get(SCENARIO_SEED))).orElse(0);
        } else {
            missingPart(SEEDS, "0 for all random seeds");
            simulationSeed = 0;
            scenarioSeed = 0;
        }
        L.debug("Simulation seed: {}", simulationSeed);
        L.debug("Scenario seed: {}", scenarioSeed);
        /*
         * Environment
         */
        final Object envObj = contents.get(ENVIRONMENT);
        if (envObj instanceof Map) {
            final Map<String, Object> envYaml = (Map<String, Object>) envObj;
            envClass = extractClass(envYaml, ENV_PACKAGE_ROOT, DEFAULT_ENVIRONMENT_CLASS);
            envArgs = extractParams(envYaml);
            L.trace("Environment parameters: {}", envArgs);
            // if envYaml contains LAYERS key 
            if (envYaml.containsKey(LAYERS)) {
                // get its value;
                final Object layersObj = envYaml.get(LAYERS);
                // if layersObj is a List
                if (layersObj instanceof List) {
                    // extract classes and parameter from the list and put them inside layersMap.
                    layersList = (List<?>) envYaml.get(LAYERS);
                } else {
                    throw new IllegalAlchemistYAMLException(layersObj + " is not a valid layer list");
                }
            } else {
                L.info("\"" + LAYERS + "\" key not found in environment. The " + envClass + " won't contain any layer.");
            }
        } else {
            missingPart(ENVIRONMENT, DEFAULT_ENVIRONMENT_CLASS.getName());
            envClass = DEFAULT_ENVIRONMENT_CLASS;
            envArgs = Collections.emptyList();
        }
        /*
         * Linking rule
         */
        final Object linkingObj = contents.get(LINKING_RULE);
        if (linkingObj instanceof Map) {
            final Map<String, Object> linkingYaml = (Map<String, Object>) linkingObj;
            L.trace("Linking rule: {}", linkingYaml);
            linkingClass = extractClass(linkingYaml, LINKING_PACKAGE_ROOT, DEFAULT_LINKING_CLASS);
            linkingArgs = extractParams(linkingYaml);
            L.trace("Linking rule args: {}", linkingArgs);
        } else {
            missingPart(LINKING_RULE, DEFAULT_LINKING_CLASS.getName());
            linkingClass = DEFAULT_LINKING_CLASS;
            linkingArgs = Collections.emptyList();
        }
        /*
         * Positions
         */
        final Object posObj = contents.get(POSITIONS);
        if (posObj instanceof Map) {
            posClass = extractClass((Map<String, Object>) posObj, POSITIONS_PACKAGE_ROOT, DEFAULT_POSITION_CLASS);
        } else {
            missingPart(POSITIONS, DEFAULT_POSITION_CLASS.getName());
            posClass = DEFAULT_POSITION_CLASS;
        }
        /*
         * Displacements
         */
        final Object dispObj = contents.get(DISPLACEMENTS);
        List<Map<String, Object>> tmpDisp = Collections.emptyList();
        if (dispObj instanceof List) {
            final List<?> dispList = (List<?>) dispObj;
            if (!dispList.isEmpty()) {
                if (dispList.get(0) instanceof Map) {
                    tmpDisp = (List<Map<String, Object>>) contents.get(DISPLACEMENTS);
                } else {
                    missingPart(DISPLACEMENTS + " inner", "an empty List");
                }
            }
        } else {
            missingPart(DISPLACEMENTS, "an empty list.");
        }
        displacements = tmpDisp;
        if (displacements.isEmpty()) {
            L.warn("Your {} section is empty. No nodes will be placed in this scenario, making it pretty useless.", DISPLACEMENTS);
        }
        L.debug("Displacements: " + displacements);
        /*
         * Extractors
         */
        final Object extrObj = contents.get(EXPORT);
        if (extrObj instanceof List) {
            extractors = Collections.unmodifiableList(((List<?>) extrObj).parallelStream()
                .map(obj -> {
                    if (obj instanceof String) {
                        final String strDesc = (String) obj;
                        if (TIME.equalsIgnoreCase(strDesc)) {
                            return new it.unibo.alchemist.loader.export.Time();
                        }
                        if (NODES.equalsIgnoreCase(strDesc)) {
                            return new NumberOfNodes();
                        }
                    }
                    if (obj instanceof Map) {
                        final Map<String, Object> mapObj = (Map<String, Object>) obj;
                        if (mapObj.containsKey(MOLECULE)) {
                            final Object aggregatorObj = mapObj.getOrDefault(AGGREGATORS, Collections.emptyList());
                            final List<String> aggregators = aggregatorObj instanceof List ? (List<String>) aggregatorObj : Collections.emptyList();
                            final Object filterObj = mapObj.getOrDefault(VALUE_FILTER, "NoFilter");
                            final FilteringPolicy filter = filterObj instanceof Map
                                    ? extractClassIfDeclared((Map<String, Object>) filterObj, VALUE_FILTER_PACKAGE_ROOT)
                                        .map(clazz -> create(clazz, extractParams((Map<String, Object>) filterObj)))
                                        .map(f -> (FilteringPolicy) f)
                                        .orElseThrow(() -> new IllegalAlchemistYAMLException(filterObj + " is not a valid value filter."))
                                    : CommonFilters.fromString(filterObj.toString());
                            return new MoleculeReader<>(
                                    mapObj.get(MOLECULE).toString(),
                                    mapObj.getOrDefault(PROPERTY, "").toString(),
                                    incarnation,
                                    filter,
                                    aggregators);
                        }
                        final Optional<Class<Extractor>> extrClass = extractClassIfDeclared(mapObj, "it.unibo.alchemist.loader.export.");
                        if (extrClass.isPresent()) {
                            final List<?> params = extractParams(mapObj);
                            return create(extrClass.get(), params, incarnation);
                        }
                    }
                    throw new IllegalAlchemistYAMLException("Could not create an exporter with " + obj);
                })
                .filter(e -> e != null)
                .collect(Collectors.toList())
            );
        } else {
            extractors = Collections.emptyList();
            L.info("{} is not a valid list of exports. No data will be exported.", extrObj);
        }
        try {
            source.close();
        } catch (IOException e1) {
            L.error("Could not close {}", source);
        }
    }

    /**
     * @param yaml
     *            the YAML file content in {@link String} format
     */
    public YamlLoader(final String yaml) {
        this(new StringReader(yaml));
    }

    @SuppressWarnings(UNCHECKED)
    private Map<String, Object> castOrEmpty(final Object target) {
        return target instanceof Map ? (Map<String, Object>) target : Collections.emptyMap();
    }

    private List<?> extractParams(final Map<String, Object> yaml) {
        return Optional.ofNullable((List<?>) yaml.get(PARAMS))
                .orElse(Collections.emptyList())
                .parallelStream()
                .map(this::makePlaceHolderIfNeeded)
                .collect(Collectors.toList());
    }

    @Override
    public <T> Environment<T> getDefault() {
        return getWith(Collections.emptyMap());
    }

    @Override
    public Map<String, Variable> getVariables() {
        return Collections.unmodifiableMap(lookupTable);
    }

    @SuppressWarnings(UNCHECKED)
    @Override
    public <T> Environment<T> getWith(final Map<String, Double> values) {
        final Map<String, Double> actualVars = lookupTable.entrySet().stream().collect(Collectors.toMap(
            Entry::getKey,
            entry -> Optional.ofNullable(values.get(entry.getKey())).orElse(entry.getValue().getDefault())
        ));
        int previousSize = -1;
        final Map<String, DependentVariable> computableCopy = new LinkedHashMap<>(computableVariables);
        while (previousSize != actualVars.size()) {
            previousSize = actualVars.size();
            final Iterator<Entry<String, DependentVariable>> it = computableCopy.entrySet().iterator();
            while (it.hasNext()) {
                final Entry<String, DependentVariable> entry = it.next();
                final DependentVariable depvar = entry.getValue();
                final String name = entry.getKey();
                try {
                    final double value = depvar.getWith(actualVars);
                    actualVars.putIfAbsent(name, value);
                    it.remove();
                } catch (IllegalStateException e) {
                    L.debug("{} could not be initialized: maybe it depends on another, not yet initialized variable.\nReason: {}", name, e);
                }
            }
        }
        if (!computableCopy.isEmpty()) {
            throw new IllegalStateException("Could not initialize some variables: " + computableCopy);
        }
        L.debug("Variable bindings: {}", actualVars);
        final RandomGenerator simRandom = create(MersenneTwister.class, Lists.newArrayList(simulationSeed), incarnation, actualVars);
        L.debug("Simulation random engine has been initialized: {}.", simRandom);
        final RandomGenerator scenarioRandom = create(MersenneTwister.class, Lists.newArrayList(simulationSeed), incarnation, actualVars);
        L.debug("Scenario random engine has been initialized: {}.", scenarioRandom);
        /*
         * Environment
         */
        final Environment<T> env = (Environment<T>) create(envClass, envArgs, incarnation, actualVars);
        L.debug("Created environment: {}", Objects.requireNonNull(env, "Could not initialize the requested environment."));
        final LinkingRule<T> linking = (LinkingRule<T>) create(linkingClass, linkingArgs, incarnation, actualVars, scenarioRandom, env);
        L.debug("Linking rule is: {}", linking);
        env.setLinkingRule(Objects.requireNonNull(linking, "The linking rule can not be null."));
        /*
         * Layers.
         */
        if (!layersList.isEmpty()) {
            for (final Object layerObj: layersList) {
                if (layerObj instanceof Map) {
                    final Map<String, Object> layer = (Map<String, Object>) layerObj;
                    if (layer.containsKey(MOLECULE) && layer.get(MOLECULE) instanceof String) {
                        final Optional<Class<Layer<T>>> layerDescriptor = extractClassIfDeclared(layer, LAYERS_PACKAGE_ROOT);
                        if (layerDescriptor.isPresent()) {
                            final Class<Layer<T>> layerClass = layerDescriptor.get();
                            final List<?> layArgs = extractParams(layer);
                            final Molecule molecule = incarnation.createMolecule((String) layer.get(MOLECULE));
                            env.addLayer(molecule, create(layerClass, layArgs));
                        } else {
                            throw new IllegalAlchemistYAMLException(layerObj + " is not a valid layer description: layer class missing or invald layer class name");
                        }
                    } else {
                        throw new IllegalAlchemistYAMLException(layerObj + " is not a valid layer description: molecule missing or invald molecule name");
                    }
                } else {
                    throw new IllegalAlchemistYAMLException(layerObj + " is not a valid layer description");
                }
            }
        }
        final PositionMaker pmaker = new PositionMaker(posClass);
        final Incarnation<T> currIncarnation = (Incarnation<T>) incarnation;
        for (final Map<String, Object> displacement : displacements) {
            final Map<String, Object> displacementShapeMap = (Map<String, Object>) displacement.get(IN);
            final Class<Displacement> displacementClass = extractClass(displacementShapeMap, DISPLACEMENTS_PACKAGE_ROOT, null);
            if (displacementClass != null) {
                final List<?> parameters = extractParams(displacementShapeMap);
                final Displacement displ = create(displacementClass, parameters, incarnation, actualVars, scenarioRandom, env, pmaker);
                L.debug("Displacement initialized: {}", displ);
                final List<Map<String, Object>> contents = Optional
                        .ofNullable((List<Map<String, Object>>) displacement.get(CONTENTS))
                        .orElse(Collections.emptyList());
                for (final Map<String, Object> content: contents) {
                    final Object shapeObj = content.get(IN);
                    if (shapeObj instanceof Map) {
                        final Map<String, Object> shapeMap = (Map<String, Object>) shapeObj;
                        content.put(IN, makeSupplier(
                                () -> IN_ALL,
                                shapeMap, SHAPES_PACKAGE_ROOT, actualVars, simRandom, env, pmaker)
                            .get());
                    } else if (!(shapeObj instanceof Shape)) {
                        content.put(IN, IN_ALL);
                    }
                }
                L.debug("Modified contents: {}", contents);
                Object programsObj = displacement.get(PROGRAMS);
                if (!(programsObj instanceof List)) {
                    missingPart(PROGRAMS, "empty list");
                    programsObj = Collections.emptyList();
                }
                final List<?> programsList = (List<?>) programsObj;
                if (!programsList.isEmpty()) {
                    final Object programObj = programsList.get(0);
                    if (programObj instanceof List) {
                        final List<?> programList = (List<?>) programObj;
                        if (!programList.isEmpty()) {
                            final Object reactionObj = programList.get(0);
                            if (!(reactionObj instanceof Map)) {
                                throw new IllegalAlchemistYAMLException("The reaction should be a YAML map. I got a "
                                        + reactionObj.getClass().getSimpleName() + " instead.");
                            }
                        }
                    } else {
                        throw new IllegalAlchemistYAMLException("The program should be a list of reactions. I got a "
                                + programObj.getClass().getSimpleName() + " instead.");
                    }
                }
                final List<Map<String, Object>> programs = ((List<List<Map<String, Object>>>) programsObj)
                        .parallelStream()
                        .flatMap(pool -> pool.stream())
                        .collect(Collectors.toList());
                final Object nodeDescriptor = displacement.get(NODE);
                final Supplier<Node<T>> nodeSupplier = makeSupplier(
                        () -> currIncarnation.createNode(simRandom, env, stringOrNull(nodeDescriptor)),
                        nodeDescriptor instanceof Map ? (Map<String, Object>) nodeDescriptor : Collections.emptyMap(),
                        NODES_PACKAGE_ROOT, actualVars, simRandom, env);
                for (final Position pos: displ) {
                    final Node<T> node = nodeSupplier.get();
                    for (final Map<String, Object> content: contents) {
                        final Shape shape = (Shape) content.get(IN);
                        if (shape.contains(pos)) {
                            final Molecule mol = makeMolecule(content, actualVars, simRandom, currIncarnation, env, node);
                            node.setConcentration(mol, makeConcentration(content, actualVars, simRandom, currIncarnation, env, node));
                        }
                    }
                    for (final Map<String, Object> program: programs) {
                        final TimeDistribution<T> td = makeTimeDistribution(program, actualVars, simRandom, currIncarnation, env, node);
                        L.trace("{}", td);
                        final Reaction<T> reaction = makeSupplier(
                                () -> currIncarnation.createReaction(simRandom, env, node, td, stringOrNull(program.get(REACTION))),
                                program, REACTIONS_PACKAGE_ROOT, actualVars, simRandom, env, node, td)
                            .get();
                        node.addReaction(reaction);
                        populateConditions(actualVars, program, simRandom, currIncarnation, env, pmaker, node, td, reaction);
                        populateActions(actualVars, program, simRandom, currIncarnation, env, pmaker, node, td, reaction);
                        L.trace("{}", reaction);
                    }
                    env.addNode(node, pos);
                }
            } else {
                L.error("Cannot instance the required displacement: {}", displacement);
            }
        }
        return env;
    }

    private  <T> T makeConcentration(final Map<String, Object> content,
            final Map<String, Double> actualVars,
            final RandomGenerator rand,
            final Incarnation<T> incarnation,
            final Environment<T> env,
            final Node<T> node) {
        final Object concSource = resolveVariable(content.get(CONCENTRATION), actualVars);
        return makeSupplier(
                () -> incarnation.createConcentration(concSource == null ? null : concSource.toString()),
                castOrEmpty(concSource), CONCENTRATIONS_PACKAGE_ROOT, actualVars, rand, env, node)
            .get();
    }

    private <T> Molecule makeMolecule(final Map<String, Object> content, final Map<String, Double> actualVars, final RandomGenerator rand, final Incarnation<T> incarnation, final Environment<T> env, final Node<T> node) {
        final Object molSource = content.get(MOLECULE);
        return makeSupplier(
                () -> incarnation.createMolecule(molSource == null ? null : molSource.toString()),
                castOrEmpty(molSource), MOLECULES_PACKAGE_ROOT, actualVars, rand, env, node)
            .get();
    }

    private Object makePlaceHolderIfNeeded(final Object in) {
        final Optional<Map<?, Double>> varopt = aVariable(in);
        if (varopt.isPresent()) {
            return new PlaceHolder(reverseLookupTable.get(univoqueId(in)));
        }
        return in;
    }

    private <O> Supplier<O> makeSupplier(
            final Supplier<O> orElse,
            final Map<String, Object> target,
            final String pkg,
            final Map<String, Double> actualVars,
            final RandomGenerator rand,
            final Environment<?> env) {
        assert target != null;
        assert pkg != null;
        return makeSupplier(orElse, target, pkg, actualVars, rand, env, null, null);
    }

    private <O> Supplier<O> makeSupplier(
            final Supplier<O> orElse,
            final Map<String, Object> target,
            final String pkg,
            final Map<String, Double> actualVars,
            final RandomGenerator rand,
            final Environment<?> env,
            final Node<?> node) {
        return makeSupplier(orElse, target, pkg, actualVars, rand, env, node, null);
    }

    private <O> Supplier<O> makeSupplier(
            final Supplier<O> orElse,
            final Map<String, Object> target,
            final String pkg,
            final Map<String, Double> actualVars,
            final RandomGenerator rand,
            final Environment<?> env,
            final Node<?> node,
            final TimeDistribution<?> timedist) {
        return makeSupplier(orElse, target, pkg, actualVars, rand, env, null, node, timedist, null);
    }

    private <O> Supplier<O> makeSupplier(
            final Supplier<O> orElse,
            final Map<String, Object> target,
            final String pkg,
            final Map<String, Double> actualVars,
            final RandomGenerator rand,
            final Environment<?> env,
            final PositionMaker posMaker) {
        assert target != null;
        return makeSupplier(orElse, target, pkg, actualVars, rand, env, posMaker, null, null, null);
    }

    private <O> Supplier<O> makeSupplier(
            final Supplier<O> orElse,
            final Map<String, Object> target,
            final String pkg,
            final Map<String, Double> actualVars,
            final RandomGenerator rand,
            final Environment<?> env,
            final PositionMaker posMaker,
            final Node<?> node,
            final TimeDistribution<?> timedist,
            final Reaction<?> reaction) {
        assert target != null;
        assert pkg != null;
        final Optional<Class<O>> clazz = extractClassIfDeclared(target, pkg);
        return clazz.isPresent()
                ? () -> create(clazz.get(), extractParams(target), incarnation, actualVars, rand, env, posMaker, node, timedist, reaction)
                : orElse;
    }

    private <T> TimeDistribution<T> makeTimeDistribution(final Map<String, Object> content, final Map<String, Double> actualVars, final RandomGenerator rand, final Incarnation<T> incarnation, final Environment<T> env, final Node<T> node) {
        final Object tdSource = resolveVariable(content.get(TIMEDISTRIBUTION), actualVars);
        return makeSupplier(
                () -> incarnation.createTimeDistribution(rand, env, node, stringOrNull(tdSource)),
                castOrEmpty(tdSource), TIMEDISTRIBUTIONS_PACKAGE_ROOT, actualVars, rand, env, node)
            .get();
    }

    private <T> void populateActions(
            final Map<String, Double> actualVars,
            final Map<String, Object> program,
            final RandomGenerator rand,
            final Incarnation<T> incarnation,
            final Environment<T> env,
            final PositionMaker pMaker,
            final Node<T> node,
            final TimeDistribution<T> td,
            final Reaction<T> reaction) {
        final List<?> actList = (List<?>) program.get(ACTIONS);
        if (actList != null) {
            /*
             * Merge existing actions and those listed.
             */
            final List<Action<T>> actions = Stream.concat(reaction.getActions().stream(), 
                actList.stream()
                    .map(actObj -> Optional.ofNullable(
                        makeSupplier(
                                () -> incarnation.createAction(rand, env, node, td, reaction, stringOrNull(actObj)),
                                castOrEmpty(actObj), ACTIONS_PACKAGE_ROOT, actualVars, rand, env, pMaker, node, td, reaction).get())
                    .orElseThrow(() -> new IllegalAlchemistYAMLException("Can not build an action with " + actObj))))
                .collect(Collectors.toList());
            L.trace("actions: {}", actions);
            reaction.setActions(actions);
        }
    }

    private <T> void populateConditions(
            final Map<String, Double> actualVars,
            final Map<String, Object> program,
            final RandomGenerator rand,
            final Incarnation<T> incarnation,
            final Environment<T> env,
            final PositionMaker pMaker,
            final Node<T> node,
            final TimeDistribution<T> td,
            final Reaction<T> reaction) {
        final List<?> condList = (List<?>) program.get(CONDITIONS);
        if (condList != null) {
            /*
             * Merge existing conditions and those listed.
             */
            final List<Condition<T>> conditions = Stream.concat(reaction.getConditions().stream(), 
                condList.stream()
                    .map(condObj -> Optional.ofNullable(
                        makeSupplier(
                            () -> incarnation.createCondition(rand, env, node, td, reaction, stringOrNull(condObj)),
                            castOrEmpty(condObj), CONDITIONS_PACKAGE_ROOT, actualVars, rand, env, pMaker, node, td, reaction).get())
                    .orElseThrow(() -> new IllegalAlchemistYAMLException("Can not build a condition with " + condObj))))
                .collect(Collectors.toList());
            L.trace("conditions: {}", conditions);
            reaction.setConditions(conditions);
        }
    }

    private Object resolveVariable(final Object target, final Map<String, Double> variables) {
        final Object extracted = makePlaceHolderIfNeeded(target);
        return extracted instanceof PlaceHolder ? resolvePlaceHolder(variables, extracted) : extracted;
    }

    private void writeObject(final ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeObject(incarnation.toString());
    }

    private void readObject(final ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        incarnation = SupportedIncarnations.get((String) ois.readObject()).get();
    }

    @SuppressWarnings(UNCHECKED)
    private static Optional<Map<?, Double>> aVariable(final Object o) {
        if (o instanceof Map<?, ?>) {
            final Map<?, ?> var = (Map<?, ?>) o;
            final Object isVar = var.get(VARIABLE);
            if (isVar instanceof Boolean) {
                final boolean isAVar = (boolean) isVar;
                if (isAVar) {
                    return Optional.of((Map<?, Double>) var);
                }
            }
        }
        return Optional.empty();
    }

    private static <O> O create(
            final Class<O> clazz,
            final List<?> newArrayList) {
        return create(clazz, newArrayList, null);
    }

    private static <O> O create(
            final Class<O> clazz,
            final List<?> newArrayList,
            final Incarnation<?> incarnation) {
        return create(clazz, newArrayList, incarnation, null);
    }
    private static <O> O create(
            final Class<O> clazz,
            final List<?> newArrayList,
            final Incarnation<?> incarnation,
            final Map<String, Double> actualVars) {
        return create(clazz, newArrayList, incarnation, actualVars, null, null, null, null, null, null);
    }
    private static <O> O create(
            final Class<O> clazz,
            final List<?> newArrayList,
            final Incarnation<?> incarnation,
            final Map<String, Double> actualVars,
            final RandomGenerator random,
            final Environment<?> env) {
        return create(clazz, newArrayList, incarnation, actualVars, random, env, null);
    }
    private static <O> O create(
            final Class<O> clazz,
            final List<?> newArrayList,
            final Incarnation<?> incarnation,
            final Map<String, Double> actualVars,
            final RandomGenerator random,
            final Environment<?> env,
            final PositionMaker pmaker) {
        return create(clazz, newArrayList, incarnation, actualVars, random, env, pmaker, null, null, null);
    }
    private static <O> O create(
            final Class<O> clazz,
            final List<?> params,
            final Incarnation<?> incarnation,
            final Map<String, Double> variables,
            final RandomGenerator rand,
            final Environment<?> env,
            final PositionMaker posMaker,
            final Node<?> node,
            final TimeDistribution<?> timedist,
            final Reaction<?> reaction) {
        @SuppressWarnings(UNCHECKED)
        final Optional<O> result = Arrays.stream(clazz.getConstructors())
            .sorted((c1, c2) -> {
                final int n1 = c1.getParameterCount();
                final int n2 = c2.getParameterCount();
                if (n1 == n2) {
                    /*
                     * Sort using types.
                     */
                    final Class<?>[] paramTypes1 = c1.getParameterTypes();
                    final Class<?>[] paramTypes2 = c2.getParameterTypes();
                    for (int i = 0; i < n1; i++) {
                        final Class<?> p1 = paramTypes1[i];
                        final Class<?> p2 = paramTypes2[i];
                        if (!p1.equals(p2)) {
                            if (Double.class.isAssignableFrom(p1) || double.class.isAssignableFrom(p1)) {
                                return -1;
                            }
                            if (Double.class.isAssignableFrom(p2) || double.class.isAssignableFrom(p2)) {
                                return 1;
                            }
                            if (Long.class.isAssignableFrom(p1) || long.class.isAssignableFrom(p1)) {
                                return -1;
                            }
                            if (Long.class.isAssignableFrom(p2) || long.class.isAssignableFrom(p2)) {
                                return 1;
                            }
                            if (Integer.class.isAssignableFrom(p1) || int.class.isAssignableFrom(p1)) {
                                return -1;
                            }
                            if (Integer.class.isAssignableFrom(p2) || int.class.isAssignableFrom(p2)) {
                                return 1;
                            }
                            L.trace("Fall back to lexicographic comparison for {} and {}", p1, p2);
                            if (p1.getSimpleName().equals(p1.getSimpleName())) {
                                return p1.toString().compareTo(p2.toString());
                            }
                            return p1.getSimpleName().compareTo(p2.getSimpleName());
                        }
                    }
                    L.warn("There are apparently two identical constructors.");
                    return 0;
                }
                final int target = params.size();
                return n1 == target ? -1
                        : n2 == target ? 1
                        : n1 < target ? n2 - n1
                        : n2 < target ? -1
                        : n1 - n2;
            })
            .map(c -> (Constructor<O>) c)
            .map(c -> createBestEffort(c, params, incarnation, variables, rand, env, posMaker, node, timedist, reaction))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
        if (result.isPresent()) {
            return result.get();
        }
        L.error("Unable to create a {} with {}", clazz.getSimpleName(), params);
        return null;
    }
    private static <O> Optional<O> createBestEffort(
            final Constructor<O> constructor,
            final List<?> params,
            final Incarnation<?> incarnation,
            final Map<String, Double> variables,
            final RandomGenerator rand,
            final Environment<?> env,
            final PositionMaker posMaker,
            final Node<?> node,
            final TimeDistribution<?> timedist,
            final Reaction<?> reaction) {
        final Deque<?> paramsLeft = Lists.newLinkedList(params);
        final Object[] actualArgs = Arrays.stream(constructor.getParameterTypes()).map(expectedClass -> {
            if (Incarnation.class.isAssignableFrom(expectedClass)) {
                return incarnation;
            }
            if (RandomGenerator.class.isAssignableFrom(expectedClass)) {
                return rand;
            }
            if (Environment.class.isAssignableFrom(expectedClass)) {
                return env;
            }
            if (PositionMaker.class.isAssignableFrom(expectedClass)) {
                return posMaker;
            }
            if (Node.class.isAssignableFrom(expectedClass)) {
                return node;
            }
            if (TimeDistribution.class.isAssignableFrom(expectedClass)) {
                return timedist;
            }
            if (Reaction.class.isAssignableFrom(expectedClass)) {
                return reaction;
            }
            while (!paramsLeft.isEmpty()) {
                Object param = paramsLeft.pop();
                if (param instanceof PlaceHolder) {
                    param = resolvePlaceHolder(variables, param);
                }
                if (param == null) {
                    return null;
                }
                if (expectedClass.isAssignableFrom(param.getClass())) {
                    return param;
                }
                if (PrimitiveUtils.classIsNumber(expectedClass) && param instanceof Number) {
                    final Optional<Number> attempt = optional2Optional(PrimitiveUtils.castIfNeeded(expectedClass, (Number) param));
                    if (attempt.isPresent()) {
                        return attempt.get();
                    }
                }
                if (PrimitiveUtils.classIsNumber(expectedClass) && param instanceof String) {
                    try {
                        final double d = Double.parseDouble((String) param);
                        final Optional<Number> attempt = optional2Optional(PrimitiveUtils.castIfNeeded(expectedClass, d));
                        if (attempt.isPresent()) {
                            return attempt.get();
                        }
                    } catch (final NumberFormatException e) {
                        return null;
                    }
                }
                if (Boolean.class.isAssignableFrom(expectedClass) || boolean.class.isAssignableFrom(expectedClass)) {
                    if (param instanceof Boolean) {
                        return param;
                    }
                    if (param instanceof String) {
                        try {
                            return Boolean.parseBoolean((String) param);
                        } catch (final NumberFormatException e) {
                            return null;
                        }
                    }
                }
                if (CharSequence.class.isAssignableFrom(expectedClass)) {
                    return param.toString();
                }
                if (Time.class.isAssignableFrom(expectedClass)) {
                    if (param instanceof Number) {
                        return new DoubleTime(((Number) param).doubleValue());
                    }
                    return new DoubleTime();
                }
                if (Molecule.class.isAssignableFrom(expectedClass) && param instanceof String) {
                    return incarnation.createMolecule(param.toString());
                }
                if (Position.class.isAssignableFrom(expectedClass) && param instanceof List) {
                    final List<?> coordList = ((List<?>) param);
                    if (coordList.stream().allMatch(n -> n instanceof Number)) {
                        return posMaker.makePosition(coordList.stream().map(v -> (Number) v).toArray(i -> new Number[i]));
                    }
                    return null;
                }
            }
            return null;
        }).toArray();
        try {
            final O result = constructor.newInstance(actualArgs);
            L.debug("{} produced {} with arguments {}", constructor, result, actualArgs);
            return Optional.of(result);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            L.debug("No luck with {} and arguments {}", constructor, actualArgs);
        }
        return Optional.empty();
    }

    @SuppressWarnings(UNCHECKED)
    private static <T> Class<T> extractClass(final Map<String, Object> yaml, final String root, final Class<T> defaultClass) {
        assert root != null;
        final Object clazz = yaml.get(TYPE);
        if (clazz != null) {
            final String className = clazz.toString();
            try {
                final String prefix = className.contains(".") ? "" : root;
                return (Class<T>) Class.forName(prefix + className);
            } catch (ClassNotFoundException e) {
                L.debug("Cannot instance {}", className);
            }
        }
        return defaultClass;
    }

    private static <O> Optional<Class<O>> extractClassIfDeclared(final Map<String, Object> target, final String pkg) {
        assert target != null;
        assert pkg != null;
        return Optional.ofNullable(extractClass(target, pkg, null));
    }

    @SuppressWarnings(UNCHECKED)
    private static Variable makeVar(final Object varObj) {
        final Map<String, Object> var = (Map<String, Object>) varObj;
        var.put(VARIABLE, true);
        final Optional<Class<Variable>> clazz = extractClassIfDeclared(var, "it.unibo.alchemist.loader.variables.");
        if (clazz.isPresent()) {
            return create(clazz.get(), Optional.ofNullable((List<?>) var.get(PARAMS)).orElse(Collections.emptyList()));
        }
        final Object vals = var.get(VALUES);
        if (vals != null) {
            if (!(vals instanceof List)) {
                throw new IllegalArgumentException(
                        VALUES + " is " + vals + ", but it must be a List of Numbers in " + varObj);
            }
            return new ArbitraryVariable(((Number) var.get(DEFAULT)).doubleValue(), (List<? extends Number>) vals);
        }
        final double def = ((Number) var.get(DEFAULT)).doubleValue();
        final double min = ((Number) var.getOrDefault(MIN, def)).doubleValue();
        final double max = ((Number) var.getOrDefault(MAX, def)).doubleValue();
        final double step = ((Number) var.getOrDefault(STEP, 1)).doubleValue();
        return new LinearVariable(def, min, max, step);
    }

    private static DependentVariable makeDepVar(final Object varObj) {
        @SuppressWarnings(UNCHECKED)
        final Map<String, Object> var = (Map<String, Object>) varObj;
        final Object formula = var.get(FORMULA);
        var.put(VARIABLE, true);
        return new DependentScriptVariable(formula.toString());
    }

    private static void missingPart(final String what, final String fallback) {
        L.warn("No {} section provided, or wrong type. Falling back to {}", what, fallback);
    }

    private static Double resolvePlaceHolder(final Map<String, Double> variables, final Object param) {
        return variables.get(((PlaceHolder) param).get());
    }

    private static String stringOrNull(final Object target) {
        return target == null ? null : target.toString();
    }

    private static long univoqueId(final Object obj) {
        return System.identityHashCode(obj);
    }

    @Override
    public List<Extractor> getDataExtractors() {
        return extractors;
    }

    private static <T> Optional<T> optional2Optional(final java8.util.Optional<T> in) {
        if (in.isPresent()) {
            return Optional.of(in.get());
        }
        return Optional.empty();
    }

}
