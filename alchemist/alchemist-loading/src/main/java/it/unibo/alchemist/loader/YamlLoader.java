/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.loader;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.ResourceBundle.getBundle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import it.unibo.alchemist.loader.variables.GroovyVariable;
import it.unibo.alchemist.loader.variables.ScriptVariable;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.danilopianini.jirf.Factory;
import org.danilopianini.jirf.FactoryBuilder;
import org.jetbrains.annotations.NotNull;
import org.kaikikm.threadresloader.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.google.common.base.Charsets;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.google.common.reflect.TypeToken;

import it.unibo.alchemist.SupportedIncarnations;
import it.unibo.alchemist.loader.displacements.Displacement;
import it.unibo.alchemist.loader.export.Extractor;
import it.unibo.alchemist.loader.export.FilteringPolicy;
import it.unibo.alchemist.loader.export.MoleculeReader;
import it.unibo.alchemist.loader.export.NumberOfNodes;
import it.unibo.alchemist.loader.export.filters.CommonFilters;
import it.unibo.alchemist.loader.shapes.Shape;
import it.unibo.alchemist.loader.variables.ArbitraryVariable;
import it.unibo.alchemist.loader.variables.DependentVariable;
import it.unibo.alchemist.loader.variables.JavascriptVariable;
import it.unibo.alchemist.loader.variables.LinearVariable;
import it.unibo.alchemist.loader.variables.NumericConstant;
import it.unibo.alchemist.loader.variables.ScalaVariable;
import it.unibo.alchemist.loader.variables.Variable;
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment;
import it.unibo.alchemist.model.implementations.linkingrules.NoLinks;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Action;
import it.unibo.alchemist.model.interfaces.Concentration;
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
@SuppressWarnings("UnstableApiUsage")
public final class YamlLoader implements Loader {

    private static final long serialVersionUID = 1L;
    private static final Logger L = LoggerFactory.getLogger(YamlLoader.class);
    private static final ResourceBundle SYNTAX = getBundle(YamlLoader.class.getPackage().getName() + ".YamlSyntax", Locale.US);
    private static final String ACTIONS = SYNTAX.getString("actions");
    private static final String AGGREGATORS = SYNTAX.getString("aggregators");
    private static final String ALCHEMIST_PACKAGE_ROOT = "it.unibo.alchemist.";
    private static final String CONCENTRATION = SYNTAX.getString("concentration");
    private static final String CONDITIONS = SYNTAX.getString("conditions");
    private static final String CONTENTS = SYNTAX.getString("contents");
    private static final String DEFAULT = SYNTAX.getString("default");
    private static final String DISPLACEMENTS = SYNTAX.getString("displacements");
    private static final String ENVIRONMENT = SYNTAX.getString("environment");
    private static final String EXPORT = SYNTAX.getString("export");
    private static final String FORMULA = SYNTAX.getString("formula");
    private static final String IN = SYNTAX.getString("in");
    private static final String INCARNATION = SYNTAX.getString("incarnation");
    private static final String LANGUAGE = SYNTAX.getString("language");
    private static final String LAYERS = SYNTAX.getString("layers");
    private static final String LINKING_RULE = SYNTAX.getString("linking-rule");
    private static final String MAX = SYNTAX.getString("max");
    private static final String MIN = SYNTAX.getString("min");
    private static final String MODEL_PACKAGE_ROOT = ALCHEMIST_PACKAGE_ROOT + "model.implementations.";
    private static final String NAME = SYNTAX.getString("name");
    private static final String MOLECULE = SYNTAX.getString("molecule");
    private static final String NODE = SYNTAX.getString("node");
    private static final String NODES = SYNTAX.getString("nodes");
    private static final String PARAMS = SYNTAX.getString("parameters");
    private static final String PARAMETER = SYNTAX.getString("parameter");
    private static final String PROGRAMS = SYNTAX.getString("programs");
    private static final String PROPERTY = SYNTAX.getString("property");
    private static final String REACTION = SYNTAX.getString("reaction");
    private static final String REMOTE_DEPENDENCIES = SYNTAX.getString("remote-dependencies");
    private static final String SCENARIO_SEED = SYNTAX.getString("scenario-seed");
    private static final String SEEDS = SYNTAX.getString("seeds");
    private static final String SIMULATION_SEED = SYNTAX.getString("simulation-seed");
    private static final String STEP = SYNTAX.getString("step");
    private static final String TERMINATORS = SYNTAX.getString("terminators");
    private static final String TIME = SYNTAX.getString("time");
    private static final String TIMEDISTRIBUTION = SYNTAX.getString("time-distribution");
    private static final String TYPE = SYNTAX.getString("type");
    private static final String UNCHECKED = "unchecked";
    private static final String VALUE_FILTER = SYNTAX.getString("value-filter");
    private static final String VALUES = SYNTAX.getString("values");
    private static final String VARIABLES = SYNTAX.getString("variables");
    @SuppressWarnings("deprecation")
    private static final Map<String, Function<String, ScriptVariable<?>>> SUPPORTED_LANGUAGES = ImmutableMap.of(
            "groovy", GroovyVariable::new,
            "javascript", JavascriptVariable::new,
            "scala", ScalaVariable::new
    );
    private static final Map<Class<?>, Map<String, Class<?>>> DEFAULT_MANDATORY_PARAMETERS = ImmutableMap.<Class<?>, Map<String, Class<?>>>builder()
            .put(Layer.class, ImmutableMap.of(TYPE, CharSequence.class, MOLECULE, CharSequence.class))
            .build();
    private static final Map<Class<?>, Map<String, Class<?>>> DEFAULT_OPTIONAL_PARAMETERS = ImmutableMap.<Class<?>, Map<String, Class<?>>>builder()
            .put(Variable.class, ImmutableMap.of(PARAMS, List.class, NAME, CharSequence.class))
            .put(DependentVariable.class, ImmutableMap.of(PARAMS, List.class, NAME, CharSequence.class))
            .put(Reaction.class, ImmutableMap.of(PARAMS, List.class, TIMEDISTRIBUTION, Object.class, ACTIONS, List.class, CONDITIONS, List.class))
            .build();
    private static final Set<BuilderConfiguration<DependentVariable<?>>> DEPENDENT_VAR_CONFIG = ImmutableSet.of(
            new BuilderConfiguration<>(
                ImmutableMap.of(FORMULA, CharSequence.class),
                ImmutableMap.of(NAME, CharSequence.class, LANGUAGE, CharSequence.class),
                makeBaseFactory(),
                m -> {
                    final Object formula = m.get(FORMULA);
                    return formula instanceof Number
                        ? new NumericConstant((Number) formula)
                        : SUPPORTED_LANGUAGES.get(m.getOrDefault(LANGUAGE, "groovy").toString().toLowerCase(Locale.ENGLISH))
                            .apply(formula.toString());
                }));
    private static final BuilderConfiguration<FilteringPolicy> FILTERING_CONFIG = new BuilderConfiguration<>(
            ImmutableMap.of(NAME, CharSequence.class), ImmutableMap.of(), makeBaseFactory(), m -> CommonFilters.fromString(m.get(NAME).toString()));
    private static final TypeToken<List<Number>> LIST_NUMBER = new TypeToken<List<Number>>() {
        private static final long serialVersionUID = 1L;
    };
    private static final TypeToken<Map<String, Object>> MAP_STRING_OBJECT = new TypeToken<Map<String, Object>>() {
        private static final long serialVersionUID = 1L;
    };
    private static final BuilderConfiguration<Extractor> NAMED_EXTRACTOR_CONFIG = new BuilderConfiguration<>(
            ImmutableMap.of(NAME, CharSequence.class), ImmutableMap.of(), makeBaseFactory(), m -> {
                final String name = m.get(NAME).toString();
                if (TIME.equalsIgnoreCase(name)) {
                    return new it.unibo.alchemist.loader.export.Time();
                }
                if (NODES.equalsIgnoreCase(name)) {
                    return new NumberOfNodes();
                }
                throw new IllegalAlchemistYAMLException("Invalid named " + EXPORT + ' ' + name);
            });
    private static final Map<Class<?>, String> PACKAGE_ROOTS = ImmutableMap.<Class<?>, String>builder()
            .put(Action.class, MODEL_PACKAGE_ROOT + "actions.")
            .put(Concentration.class, MODEL_PACKAGE_ROOT + "concentrations.")
            .put(Condition.class, MODEL_PACKAGE_ROOT + "conditions.")
            .put(DependentVariable.class, ALCHEMIST_PACKAGE_ROOT + "loader.variables.")
            .put(Displacement.class, ALCHEMIST_PACKAGE_ROOT + "loader.displacements.")
            .put(Environment.class, MODEL_PACKAGE_ROOT + "environments.")
            .put(Extractor.class, ALCHEMIST_PACKAGE_ROOT + "loader.export.")
            .put(FilteringPolicy.class, ALCHEMIST_PACKAGE_ROOT + "loader.export.filters")
            .put(Layer.class, MODEL_PACKAGE_ROOT + "layers.")
            .put(LinkingRule.class, MODEL_PACKAGE_ROOT + "linkingrules.")
            .put(Molecule.class, MODEL_PACKAGE_ROOT + "molecules.")
            .put(Node.class, MODEL_PACKAGE_ROOT + "nodes.")
            .put(Predicate.class, MODEL_PACKAGE_ROOT + "terminators.")
            .put(Reaction.class, MODEL_PACKAGE_ROOT + "reactions.")
            .put(Shape.class, ALCHEMIST_PACKAGE_ROOT + "loader.shapes.")
            .put(TimeDistribution.class, MODEL_PACKAGE_ROOT + "timedistributions.")
            .put(Variable.class, ALCHEMIST_PACKAGE_ROOT + "loader.variables.")
            .build();
    private final ImmutableMap<String, Object> constants;
    private final ImmutableMap<String, Object> contents;
    private final ImmutableMap<String, DependentVariable<?>> depVariables;
    private final List<Extractor> extractors;
    private transient Incarnation<?, ?> incarnation;
    private final ImmutableMap<Map<String, Object>, String> reverseLookupTable;
    private final ImmutableMap<String, Variable<?>> variables;
    private final ImmutableList<String> dependencies;

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
        final Map<String, Object> rawContents = Collections.unmodifiableMap((Map<String, Object>) yamlObj);
        /*
         * Incarnation
         */
        final Object incObj = rawContents.get(INCARNATION);
        if (incObj == null) {
            throw new IllegalAlchemistYAMLException("You must specify an incarnation.",
                    new IllegalStateException("No incarnation specified in YAML simulation file"));
        }
        incarnation = SupportedIncarnations.get(incObj.toString())
                .orElseThrow(() -> new IllegalStateException(incObj
                        + " is not a valid incarnation. Supported incarnations are: "
                        + SupportedIncarnations.getAvailableIncarnations()));
        /*
         * Extract variables
         */
        final Object varObj = rawContents.get(VARIABLES);
        if (varObj != null && !(varObj instanceof Map)) {
            throw new IllegalAlchemistYAMLException("The " + VARIABLES + " section has an invalid format.");
        }
        final Map<String, Map<String, Object>> originalVars = Optional.ofNullable((Map<String, Map<String, Object>>) varObj).orElse(emptyMap());
        for (final Entry<String, Map<String, Object>> varEntry : originalVars.entrySet()) {
            if (varEntry.getValue() == null) {
                throw new IllegalAlchemistYAMLException("The " + VARIABLES + " section has an invalid format."
                        + " Likely, the error is in " + varEntry.getKey() + ", and due to its members not being"
                        + " correctly indented.");
            }
            varEntry.getValue().put(NAME, varEntry.getKey());
        }
        L.debug("Variables: {}", originalVars);
        final Map<Map<String, Object>, String> reverseLookupTable = originalVars.entrySet().stream().collect(Collectors.toMap(Entry::getValue, Entry::getKey));
        L.debug("Reverse lookup table: {}", reverseLookupTable);
        /*
         * Compute constants and dependent variables
         */
        final Map<String, Object> constants = Maps.newLinkedHashMapWithExpectedSize(originalVars.size());
        final Map<String, DependentVariable<?>> depVariables = Maps.newLinkedHashMapWithExpectedSize(originalVars.size());
        final Factory factory = makeBaseFactory(incarnation);
        final Builder<DependentVariable<?>> depVarBuilder = new Builder<>(DependentVariable.class, DEPENDENT_VAR_CONFIG, factory);
        int previousConstants, previousDepVars;
        final Map<String, Map<String, Object>> originalClone = new LinkedHashMap<>(originalVars);
        do {
            previousConstants = constants.size();
            previousDepVars = depVariables.size();
            final Iterator<Entry<String, Map<String, Object>>> iter = originalClone.entrySet().iterator();
            while (iter.hasNext()) {
                final Entry<String, Map<String, Object>> entry = iter.next();
                final String name = entry.getKey();
                try {
                    final DependentVariable<?> dv = Objects.requireNonNull(depVarBuilder.build(entry.getValue()));
                    try {
                        final Object value = dv.getWith(constants);
                        iter.remove();
                        constants.put(name, value);
                        depVariables.remove(name);
                    } catch (IllegalStateException e) {
                        L.debug("{} value could not be computed: maybe it depends on another, not yet initialized variable.\nReason: {}", name, e);
                        depVariables.put(name, dv);
                    }
                    L.debug("Constant intialized in {}", constants);
                } catch (IllegalAlchemistYAMLException e) {
                    L.debug("{} could not be created: its constructor may be requiring an uninitialized variable.\nReason: {}", name, e);
                }
            }
        } while (previousConstants != constants.size() || previousDepVars != depVariables.size());
        assert constants.size() + depVariables.size() <= originalVars.size();
        this.constants = ImmutableMap.copyOf(constants);
        this.depVariables = ImmutableMap.copyOf(depVariables);
        this.contents = ImmutableMap.copyOf((Map<String, Object>) recursivelyResolveVariables(rawContents, reverseLookupTable, this.constants));
        final Iterator<String> varNames = reverseLookupTable.values().iterator();
        while (varNames.hasNext() && !constants.isEmpty()) {
            final String var = varNames.next();
            if (constants.containsKey(var)) {
                varNames.remove();
            }
        }
        this.reverseLookupTable = ImmutableMap.copyOf(reverseLookupTable);
        /*
         * Compute variables
         */
        final BiFunction<Map<String, ?>, String, Double> toDouble =
                (m, s) -> factory.convertOrFail(Double.class, m.get(s));
        final BuilderConfiguration<Variable<?>> arbitraryVarConfig = new BuilderConfiguration<>(
                ImmutableMap.of(VALUES, List.class, DEFAULT, Number.class), ImmutableMap.of(NAME, CharSequence.class), factory,
                m -> new ArbitraryVariable(toDouble.apply(m, DEFAULT), factory.convertOrFail(List.class, m.get(VALUES))));
        final BuilderConfiguration<Variable<?>> linearVarConfig = new BuilderConfiguration<>(
                ImmutableMap.of(DEFAULT, Number.class, MIN, Number.class, MAX, Number.class, STEP, Number.class), ImmutableMap.of(NAME, CharSequence.class), factory,
                m -> new LinearVariable(toDouble.apply(m, DEFAULT), toDouble.apply(m, MIN), toDouble.apply(m, MAX), toDouble.apply(m, STEP)));
        final Builder<Variable<?>> varBuilder = new Builder<>(Variable.class, ImmutableSet.of(arbitraryVarConfig, linearVarConfig), factory);
        variables = originalVars.entrySet().stream()
                .filter(e -> e != null && !e.getValue().containsKey(FORMULA))
                .collect(ImmutableMap.toImmutableMap(Entry::getKey, e -> varBuilder.build(e.getValue())));
        L.debug("Lookup table: {}", variables);
        assert depVariables.size() + variables.size() == reverseLookupTable.size();
        /*
         * Extractors
         */
        final Object extrObj = contents.get(EXPORT);
        if (extrObj == null) {
            extractors = emptyList();
        } else if (extrObj instanceof List) {
            final Builder<FilteringPolicy> filterBuilder = new Builder<>(FilteringPolicy.class, FILTERING_CONFIG, factory);
            final Builder<Extractor> extractorBuilder = new Builder<>(Extractor.class,
                    ImmutableSet.of(
                            NAMED_EXTRACTOR_CONFIG,
                            new BuilderConfiguration<>(
                                ImmutableMap.of(MOLECULE, CharSequence.class),
                                ImmutableMap.of(PROPERTY, CharSequence.class, AGGREGATORS, List.class, VALUE_FILTER, Object.class),
                                factory,
                                m -> {
                                    final Object filterObj = m.getOrDefault(VALUE_FILTER, "NoFilter");
                                    final FilteringPolicy filter = filterBuilder.build(filterObj instanceof CharSequence
                                            ? ImmutableMap.of(NAME, filterObj.toString()) : filterObj);
                                    final List<String> aggregators = listCast(factory, m.get(AGGREGATORS), "aggregators").stream()
                                            .map(Object::toString)
                                            .collect(Collectors.toList());
                                    return new MoleculeReader<>(m.get(MOLECULE).toString(), m.getOrDefault(PROPERTY, "").toString(), incarnation, filter, aggregators);
                                })
                    ), factory);
            extractors = Collections.unmodifiableList(((List<?>) extrObj).stream()
                    .map(obj -> extractorBuilder.build(obj instanceof CharSequence ? ImmutableMap.of(NAME, obj.toString()) : obj))
                    .collect(Collectors.toList()));
        } else {
            throw new IllegalAlchemistYAMLException("Exports must be a YAML map.");
        }
        final Object dependencies = rawContents.get(REMOTE_DEPENDENCIES);
        if (dependencies == null) {
            this.dependencies = ImmutableList.of();
        } else if (dependencies instanceof List) {
            final List<?> dependencyList = (List<?>) dependencies;
            if (dependencyList.stream().allMatch(it -> it instanceof  String)) {
                this.dependencies = ImmutableList.copyOf((List<String>) dependencies);
            } else {
                throw new IllegalStateException("Dependencies are declared,"
                        + " but some of them are not strings: " + dependencies);
            }
        } else {
            throw new IllegalStateException("Dependencies are expected to be declared as list. Found a "
                + dependencies.getClass().getSimpleName() + ": " + dependencies);
        }
    }

    /**
     * @param yaml
     *            the YAML file content in {@link String} format
     */
    public YamlLoader(final String yaml) {
        this(new StringReader(yaml));
    }

    @Override
    public List<Extractor> getDataExtractors() {
        return Collections.unmodifiableList(extractors);
    }

    @Override
    public <T, P extends Position<P>> Environment<T, P> getDefault() {
        return getWith(emptyMap());
    }

    @Override
    public Map<String, Variable<?>> getVariables() {
        return Collections.unmodifiableMap(variables);
    }

    @Override
    public List<String> getDependencies() {
        return this.dependencies;
    }

    @Override
    public <T, P extends Position<P>> Environment<T, P> getWith(final Map<String, ?> values) {
        if (values.size() > variables.size()) {
            throw new IllegalArgumentException("Some variables do not exist in the environment, or are not overridable: " + Maps.difference(values, variables).entriesOnlyOnLeft());
        }
        final int expectedSize = constants.size() + variables.size() + depVariables.size();
        final Map<String, Object> actualVars = Maps.newLinkedHashMapWithExpectedSize(expectedSize);
        actualVars.putAll(values);
        actualVars.putAll(constants);
        for (final Entry<String, Variable<?>> entry: variables.entrySet()) {
            final String var = entry.getKey();
            final Object varVal = values.get(var);
            actualVars.put(var, varVal == null ? entry.getValue().getDefault() : varVal); 
        }
        /*
         * Initialize the remaining dependent variables, and add them to the actual Vars
         */
        final Map<String, DependentVariable<?>> depClone = new LinkedHashMap<>(depVariables);
        final List<Exception> issues = Lists.newArrayListWithCapacity(depClone.size());
        int previousSize;
        do {
            issues.clear();
            previousSize = depClone.size();
            final Iterator<Entry<String, DependentVariable<?>>> iter = depClone.entrySet().iterator();
            while (iter.hasNext()) {
                final Entry<String, DependentVariable<?>> entry = iter.next();
                final String name = entry.getKey();
                final DependentVariable<?> dv = entry.getValue();
                try {
                    final Object value = dv.getWith(actualVars);
                    iter.remove();
                    actualVars.put(name, value);
                } catch (IllegalStateException e) {
                    issues.add(e);
                    L.debug("{} value could not be computed: maybe it depends on another, not yet initialized variable.\nReason: {}", name, e);
                }
            }
        } while (previousSize != depClone.size());
        if (!depClone.isEmpty()) {
            final RuntimeException ex = new IllegalAlchemistYAMLException("One or more variables could not be initialized: " + depClone);
            if (issues.size() == 1) {
                ex.initCause(issues.get(0));
            } else {
                issues.forEach(ex::addSuppressed);
            }
            throw ex;
        }
        L.debug("Variable bindings: {}", actualVars);
        assert actualVars.size() == constants.size() + reverseLookupTable.size();
        @SuppressWarnings(UNCHECKED)
        final Map<String, Object> contents = (Map<String, Object>) recursivelyResolveVariables(this.contents, reverseLookupTable, actualVars);
        /*
         * Factory
         */
        final Factory factory = makeBaseFactory(incarnation);
        @SuppressWarnings(UNCHECKED)
        final Incarnation<T, P> incarnation = (Incarnation<T, P>) this.incarnation;
        /*
         * RNG
         */
        final Object seedObj = contents.get(SEEDS);
        final RandomGenerator scenarioRng = rngBuilder(factory, SCENARIO_SEED).build(seedObj);
        final RandomGenerator simRng = rngBuilder(factory, SIMULATION_SEED).build(seedObj);
        /*
         * Environment
         */
        @SuppressWarnings("unchecked")
        final BuilderConfiguration<Environment<T, P>> envDefaultConfig = emptyConfig(factory, () -> (Environment<T, P>) new Continuous2DEnvironment<>());
        final Builder<Environment<T, P>> envBuilder = new Builder<>(Environment.class, ImmutableSet.of(envDefaultConfig), factory);
        factory.registerSingleton(RandomGenerator.class, simRng);
        final Environment<T, P> env = envBuilder.build(contents.get(ENVIRONMENT));
        env.setIncarnation(incarnation);
        factory.registerSingleton(Environment.class, env);
        factory.registerImplicit(List.class, Position.class, l -> env.makePosition(cast(factory, LIST_NUMBER, l, "position coordinates").toArray(new Number[l.size()])));
        factory.registerImplicit(Number[].class, Position.class, env::makePosition);
        final Builder<Molecule> molBuilder = new Builder<>(Molecule.class, singleParamConfig(factory, p -> incarnation.createMolecule(p.toString())), factory);
        /*
         * Layers
         */
        final List<?> layers = listCast(factory, contents.get(LAYERS), "layers");
        final Builder<Layer<T, P>> layerBuilder = new Builder<>(Layer.class, emptySet(), factory);
        layers.forEach(o -> {
            final Map<String, Object> layerMap = cast(factory, MAP_STRING_OBJECT, o, "layer");
            final Layer<T, P> layer = layerBuilder.build(layerMap);
            final Molecule molecule = molBuilder.build(layerMap.get(MOLECULE));
            env.addLayer(molecule, layer);
        });
        /*
         * Linking rule
         */
        final BuilderConfiguration<LinkingRule<T, P>> linkingRuleConfig = emptyConfig(factory, NoLinks::new);
        final Builder<LinkingRule<T, P>> linkingBuilder = new Builder<>(LinkingRule.class, ImmutableSet.of(linkingRuleConfig), factory);
        final LinkingRule<T, P> linkingRule = linkingBuilder.build(contents.get(LINKING_RULE));
        env.setLinkingRule(linkingRule);
        factory.registerSingleton(LinkingRule.class, linkingRule);
        /*
         * Termination conditions
         */
        final Builder<Predicate<Environment<T, P>>> terminatorBuilder = new Builder<>(Predicate.class, emptySet(), factory);
        final Object terminatorsDesc = contents.get(TERMINATORS);
        for (final Object terminatorDescriptor: listCast(factory, terminatorsDesc, "terminator")) {
            env.addTerminator(terminatorBuilder.build(terminatorDescriptor));
        }
        /*
         * Displacements
         */
        final List<?> dispList = listCast(factory, contents.get(DISPLACEMENTS), "displacements");
        if (dispList.isEmpty()) {
            L.warn("Your {} section is empty. No nodes will be placed in this scenario, making it pretty useless.", DISPLACEMENTS);
        } else {
            final Builder<Displacement<P>> displacementBuilder = new Builder<>(Displacement.class, emptySet(), factory);
            final Builder<Node<T>> nodeBuilder = new Builder<>(Node.class, ImmutableSet.of(
                    emptyConfig(factory, () -> incarnation.createNode(simRng, env, null)),
                    singleParamConfig(factory, o -> incarnation.createNode(simRng, env, o.toString()))),
                    factory);
            final Builder<Shape<P>> shapeBuilder = new Builder<>(Shape.class, emptyConfig(factory, () -> p -> true), factory);
            for (final Object dispObj: dispList) {
                final Map<String, Object> dispMap = cast(factory, MAP_STRING_OBJECT, dispObj, "displacement");
                factory.registerSingleton(RandomGenerator.class,  scenarioRng);
                final Displacement<P> displacement = displacementBuilder.build(dispMap.get(IN));
                factory.registerSingleton(RandomGenerator.class,  simRng);
                factory.registerSingleton(Displacement.class, displacement);
                /*
                 * Contents
                 */
                final List<?> contentsList = listCast(factory, dispMap.get(CONTENTS), "contents");
                final Table<Shape<P>, Molecule, String> shapes = HashBasedTable.create(contentsList.size(), contentsList.size());
                for (final Object contentObj: contentsList) {
                    final Map<String, Object> contentMap = cast(factory, MAP_STRING_OBJECT, contentObj, "content");
                    final Shape<P> shape = shapeBuilder.build(contentMap.get(IN));
                    final Molecule molecule = molBuilder.build(contentMap.get(MOLECULE));
                    final Object concObj = contentMap.get(CONCENTRATION);
                    shapes.put(shape, molecule, concObj == null ? "" : concObj.toString());
                }
                /*
                 * Nodes
                 */
                factory.registerSingleton(RandomGenerator.class, simRng);
                for (@NotNull final P position: displacement) {
                    final Node<T> node = nodeBuilder.build(dispMap.get(NODE));
                    factory.registerSingleton(Node.class, node);
                    /*
                     * Node contents
                     */
                    for (final Cell<Shape<P>, Molecule, String> entry: shapes.cellSet()) {
                        final Shape<P> shape = entry.getRowKey();
                        if (shape == null) {
                            throw new IllegalStateException("Illegal null shape in " + shapes);
                        }
                        if (shape.contains(position)) {
                            final Molecule mol = entry.getColumnKey();
                            final String concentration = entry.getValue();
                            node.setConcentration(mol, incarnation.createConcentration(concentration));
                        }
                    }
                    /*
                     * Reactions
                     */
                    final List<?> poolsList = listCast(factory, dispMap.get(PROGRAMS), "program pools");
                    final Builder<TimeDistribution<T>> tdBuilder = new Builder<>(TimeDistribution.class, ImmutableSet.of(
                            emptyConfig(factory, () -> incarnation.createTimeDistribution(simRng, env, node, null)),
                            singleParamConfig(factory, o -> incarnation.createTimeDistribution(simRng, env, node, o.toString()))),
                            factory);
                    for (final Object programsObj: poolsList) {
                        final List<?> programs = listCast(factory, programsObj, "programs");
                        for (final Object programObj: programs) {
                            final Map<String, Object> program = cast(factory, MAP_STRING_OBJECT, programObj, "program");
                            final TimeDistribution<T> td = tdBuilder.build(program.get(TIMEDISTRIBUTION));
                            factory.registerSingleton(TimeDistribution.class, td);
                            final Builder<Reaction<T>> reactionBuilder = new Builder<>(Reaction.class,
                                    new BuilderConfiguration<>(
                                            ImmutableMap.of(REACTION, CharSequence.class),
                                            ImmutableMap.of(TIMEDISTRIBUTION, Object.class, ACTIONS, List.class, CONDITIONS, List.class),
                                            factory, m -> incarnation.createReaction(simRng, env, node, td, m.get(REACTION).toString())),
                                    factory);
                            final Reaction<T> reaction = reactionBuilder.build(program);
                            factory.registerSingleton(Reaction.class, reaction);
                            /*
                             * Actions and conditions
                             */
                            final List<?> actionsList = listCast(factory, program.get(ACTIONS), "actions list");
                            if (!actionsList.isEmpty()) {
                                final Builder<Action<T>> actionBuilder = new Builder<>(Action.class, 
                                        singleParamConfig(factory, o -> incarnation.createAction(simRng, env, node, td, reaction, o.toString())), factory);
                                reaction.setActions(Stream.concat(
                                        actionsList.stream().map(actionBuilder::build),
                                        reaction.getActions().stream())
                                    .collect(Collectors.toList()));
                            }
                            /*
                             * Conditions
                             */
                            final List<?> conditionsList = listCast(factory, program.get(CONDITIONS), "conditions list");
                            if (!conditionsList.isEmpty()) {
                                final Builder<Condition<T>> conditionBuilder = new Builder<>(Condition.class, 
                                        singleParamConfig(factory, o -> incarnation.createCondition(simRng, env, node, td, reaction, o.toString())), factory);
                                reaction.setConditions(Stream.concat(
                                        conditionsList.stream().map(conditionBuilder::build),
                                        reaction.getConditions().stream())
                                    .collect(Collectors.toList()));
                            }
                            node.addReaction(reaction);
                            if (!(factory.deregisterSingleton(reaction) && factory.deregisterSingleton(td))) {
                                throw new IllegalStateException("This is a bug in " + getClass() + ": singletons are not correctly cleared.");
                            }
                        }
                    }
                    if (!factory.deregisterSingleton(node)) {
                        throw new IllegalStateException("This is a bug in " + getClass() + ": singletons are not correctly cleared.");
                    }
                    env.addNode(node, position);
                }
                if (!factory.deregisterSingleton(displacement)) {
                    throw new IllegalStateException("This is a bug in " + getClass() + ": singletons are not correctly cleared.");
                }
            }
        }
        return env;
    }

    private void readObject(final ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        final String incarnationName = ois.readObject().toString();
        incarnation = SupportedIncarnations.get(incarnationName).
                orElseThrow(() -> new IllegalStateException(incarnationName + " is not a valid incarnation."));
    }

    private Builder<RandomGenerator> rngBuilder(final Factory factory, final String seed) {
        final BuilderConfiguration<RandomGenerator> config = new BuilderConfiguration<>(
                emptyMap(),
                ImmutableMap.of(SCENARIO_SEED, Number.class, SIMULATION_SEED, Number.class),
                factory, rngMaker(factory, seed));
        return new Builder<>(RandomGenerator.class, ImmutableSet.of(config), factory);
    }

    private void writeObject(final ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeObject(incarnation.toString());
    }

    @SuppressWarnings(UNCHECKED)
    private static <T> T cast(final Factory factory, final Class<? super T> clazz, final Object target, final String what) {
        assert factory != null;
        assert clazz != null;
        assert target != null;
        assert what != null;
        return (T) factory.convert(clazz, target).orElseThrow(() -> new IllegalAlchemistYAMLException(target + " is not a valid " + what + " descriptor"));
    }

    private static <T> T cast(final Factory factory, final TypeToken<T> clazz, final Object target, final String message) {
        return cast(factory, clazz.getRawType(), target, message);
    }

    private static <X> BuilderConfiguration<X> emptyConfig(final Factory factory, final Supplier<X> supplier) {
        return new BuilderConfiguration<>(emptyMap(), emptyMap(), factory, m -> supplier.get());
    }

    private static List<?> listCast(final Factory factory, final Object target, final String what) {
        assert factory != null;
        assert what != null;
        return target == null ? emptyList() : cast(factory, List.class, target, what + " list");
    }

    private static Factory makeBaseFactory() {
        final Factory factory = new FactoryBuilder()
                .withNarrowingConversions()
                .withArrayBooleanIntConversions()
                .withArrayListConversions(String[].class, Number[].class)
                .withArrayNarrowingConversions()
                .build();
        factory.registerImplicit(Number.class, CharSequence.class, Number::toString);
        factory.registerImplicit(double.class, Time.class, DoubleTime::new);
        factory.registerImplicit(List.class, Number[].class, l -> ((List<?>) l).stream().map(e -> factory.convertOrFail(Number.class, e)).toArray(Number[]::new));
        factory.registerImplicit(CharSequence.class, FilteringPolicy.class, s -> CommonFilters.fromString(s.toString()));
        factory.registerImplicit(Number.class, double.class, Number::doubleValue);
        factory.registerImplicit(double.class, BigDecimal.class, BigDecimal::new);
        factory.registerImplicit(long.class, BigInteger.class, BigInteger::valueOf);
        return factory;
    }

    private static Factory makeBaseFactory(@Nonnull final Incarnation<?, ?> incarnation) {
        final Factory factory = makeBaseFactory();
        factory.registerSingleton(Incarnation.class, incarnation);
        factory.registerImplicit(CharSequence.class, Molecule.class, s -> incarnation.createMolecule(s.toString()));
        return factory;
    }

    private static Object recursivelyResolveVariables(final Object o, final Map<Map<String, Object>, String> reverseLookupTable, final Map<String, Object> variables) {
        if (reverseLookupTable.isEmpty() || variables.isEmpty()) {
            return o;
        }
        if (o instanceof Collection) {
            final Collection<?> collection = (Collection<?>) o;
            final int s = collection.size();
            return collection.stream()
                .map(e -> recursivelyResolveVariables(e, reverseLookupTable, variables))
                .collect(Collectors.toCollection(() -> collection instanceof Set
                        ? Sets.newLinkedHashSetWithExpectedSize(s)
                        : Lists.newArrayListWithCapacity(s)));
        }
        if (o instanceof Map) {
            final String varName = reverseLookupTable.get(o);
            if (varName != null) {
                final Object value = variables.get(varName);
                if (value != null) {
                    return value;
                }
            } else {
                final Map<?, ?> oMap = (Map<?, ?>) o;
                return oMap.entrySet().stream()
                        .collect(() -> Maps.newLinkedHashMapWithExpectedSize(oMap.size()),
                            (m, p) -> m.put(p.getKey(), recursivelyResolveVariables(p.getValue(), reverseLookupTable, variables)),
                            (m1, m2) -> {
                                throw new IllegalStateException();
                            }
                        );
            }
        }
        return o;
    }
    private static Function<Map<String, Object>, RandomGenerator> rngMaker(final Factory factory, final String seed) {
        return m -> Optional.ofNullable(m.get(seed))
                .map(o -> factory.build(MersenneTwister.class, o))
                .orElse(new MersenneTwister(0));
    }

    private static <T> BuilderConfiguration<T> singleParamConfig(final Factory factory, final Function<Object, T> supplier) {
        return new BuilderConfiguration<>(ImmutableMap.of(PARAMETER, Object.class), emptyMap(), factory, m -> supplier.apply(m.get(PARAMETER)));
    }

    private class Builder<T> {
        private final @Nonnull Class<? super T> clazz;
        private final @Nonnull Set<BuilderConfiguration<T>> supportedConfigs;
        Builder(@Nonnull final Class<? super T> clazz, @Nonnull final BuilderConfiguration<T> supportedConfig, final Factory factory) {
            this(clazz, ImmutableSet.of(supportedConfig), factory);
        }

        @SuppressWarnings(UNCHECKED)
        Builder(@Nonnull final Class<? super T> clazz, @Nonnull final Set<BuilderConfiguration<T>> supportedConfigs, final Factory factory) {
            this.clazz = clazz;
            final String packageRoot = PACKAGE_ROOTS.getOrDefault(clazz, "");
            this.supportedConfigs = Sets.newLinkedHashSet(supportedConfigs);
            this.supportedConfigs.add(new BuilderConfiguration<>(
                    DEFAULT_MANDATORY_PARAMETERS.getOrDefault(clazz, ImmutableMap.of(TYPE, CharSequence.class)),
                    DEFAULT_OPTIONAL_PARAMETERS.getOrDefault(clazz, ImmutableMap.of(PARAMS, List.class)),
                    factory,
                    m -> {
                        String type = m.get(TYPE).toString();
                        assert type != null;
                        type = (type.contains(".") ? "" : packageRoot) + type;
                        try {
                            final Class<?> actualClass = ResourceLoader.classForName(type);
                            if (clazz.isAssignableFrom(actualClass)) {
                                final Optional<Object> rawParams = Optional.ofNullable(m.get(PARAMS));
                                rawParams.ifPresent(l -> {
                                    if (!(l instanceof List)) {
                                        throw new IllegalAlchemistYAMLException(l + " is not a valid list of parameters");
                                    }
                                });
                                final List<?> parameters = rawParams.map(l -> (List<?>) l).orElse(emptyList());
                                return factory.build((Class<T>) actualClass, parameters);
                            } else {
                                throw new IllegalAlchemistYAMLException(type + "is not a subclass of " + clazz);
                            }
                        } catch (ClassNotFoundException e) {
                            throw new IllegalAlchemistYAMLException(type + " is not a valid Java class", e);
                        }
                    }));
        }
        public T build(final Object o) {
            final Object realObj = o == null ? emptyMap() : o;
            final Collection<BuilderConfiguration<T>> configs = supportedConfigs.stream()
                    .filter(c -> c.matches(realObj)).collect(Collectors.toList());
            if (configs.isEmpty()) {
                throw new IllegalAlchemistYAMLException("No configuration among " + supportedConfigs + " is applicable for building a " + clazz.getName() + " with " + o);
            }
            if (configs.size() > 1) {
                throw new IllegalAlchemistYAMLException("Ambiguous specification " + o + " matches all those in " + configs + " for " + clazz.getName());
            }
            return configs.iterator().next().build(realObj);
        }
    }

    private static class BuilderConfiguration<T> {
        private final @Nonnull Function<Map<String, Object>, T> buildFunction;
        private final @Nonnull Factory factory;
        private final @Nonnull Map<String, Class<?>> mandatoryFields;
        private final @Nonnull Map<String, Class<?>> optionalFields;
        BuilderConfiguration(@Nonnull final Map<String, Class<?>> mandatory,
                @Nonnull final Map<String, Class<?>> optional,
                @Nonnull final Factory factory,
                @Nonnull final Function<Map<String, Object>, T> converter) {
            this.mandatoryFields = mandatory;
            this.optionalFields = optional;
            this.buildFunction = converter;
            this.factory = factory;
        }
        public T build(@Nonnull final Object o) {
            assert matches(o);
            return ifMap(o, m -> {
                    final Map<String, Object> args = Maps.newLinkedHashMapWithExpectedSize(mandatoryFields.size() + optionalFields.size());
                    for (final Entry<String, Class<?>> arg: mandatoryFields.entrySet()) {
                        final String varName = arg.getKey();
                        assert m.containsKey(varName);
                        args.put(varName, factory.convertOrFail(arg.getValue(), m.get(varName)));
                    }
                    for (final Entry<String, Class<?>> arg: optionalFields.entrySet()) {
                        final String varName = arg.getKey();
                        final Object target = m.get(varName);
                        if (target != null) {
                            args.put(varName, factory.convertOrFail(arg.getValue(), m.get(varName)));
                        }
                    }
                    return buildFunction.apply(args);
                },
                () -> {
                    throw new IllegalAlchemistYAMLException(o + " is not a valid Alchemist object descriptor.");
                });
        }
        private <R> R ifMap(final Object o, final Function<Map<?, ?>, R> todo, final Supplier<R> otherwise) {
            if (o instanceof Map) {
                return todo.apply((Map<?, ?>) o);
            } else if (o != null) {
                return todo.apply(ImmutableMap.of(PARAMETER, o));
            }
            return otherwise.get();
        }
        private boolean matches(final Object o) {
            return ifMap(o, m -> 
                m.keySet().containsAll(mandatoryFields.keySet())
                && Sets.union(mandatoryFields.keySet(), optionalFields.keySet()).containsAll(m.keySet()),
                () -> false
            );
        }
        @Override
        public String toString() {
            return "{mandatory=" + toString(mandatoryFields) + ", optional=" + toString(optionalFields) + "}";
        }
        private static String toString(final Map<String, Class<?>> m) {
            return '[' + (m.isEmpty() ? "" : m.entrySet().stream()
                    .map(e -> e.getKey() + ':' + e.getValue().getName()).collect(Collectors.joining(", "))) + ']';
        }
    }
}
