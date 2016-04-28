/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.language;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java8.util.Optional;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.danilopianini.lang.PrimitiveUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.apache.commons.math3.random.RandomGenerator;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Action;
import it.unibo.alchemist.model.interfaces.Concentration;
import it.unibo.alchemist.model.interfaces.Condition;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.LinkingRule;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;
import it.unibo.alchemist.model.interfaces.TimeDistribution;

/**
 * @param <T>
 *            concentration type
 */
public final class EnvironmentBuilder<T> {

    private static final Logger L = LoggerFactory.getLogger(EnvironmentBuilder.class);
    private static final String DEFAULT_PACKAGE = "it.unibo.alchemist.";
    private static final String LINKINGRULES_DEFAULT_PACKAGE = DEFAULT_PACKAGE + "model.implementations.linkingrules.";
    private static final String NAME = "name";
    private static final String REACTIONS_DEFAULT_PACKAGE = DEFAULT_PACKAGE + "model.implementations.reactions.";
    private static final String TEXT = "#text";
    private static final String TYPE = "type";
    private static final Class<?>[] TYPES = new Class<?>[] { List.class, Integer.TYPE, Double.TYPE, Boolean.TYPE, Character.TYPE, Byte.TYPE, Short.TYPE, Long.TYPE, Float.TYPE };

    private Class<?> concentrationClass;
    private final Random internalRandom = new Random();
    private Class<?> positionClass;
    private RandomGenerator random;
    private int seed;
    private Environment<T> result;
    private final InputStream xmlFile;

    /**
     * Builds a new XML interpreter.
     * 
     * @param xmlStream
     *            the input stream to interpret
     */
    private EnvironmentBuilder(final InputStream xmlStream) {
        super();
        xmlFile = xmlStream;
    }

    private Action<T> buildAction(final Node son, final Map<String, Object> env) throws InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        return buildK(son, env, "it.unibo.alchemist.model.implementations.actions.");
    }

    private T buildConcentration(final Node son, final Map<String, Object> subenv) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        if (concentrationClass != null) {
            final String args = son.getNodeValue();
            final ArrayList<String> arguments = new ArrayList<String>(1);
            if (!args.isEmpty()) {
                arguments.add(args);
            }
            final List<Constructor<Concentration<T>>> list = unsafeExtractConstructors(concentrationClass);
            return tryToBuild(list, arguments, subenv, random).getContent();
        }
        L.error("concentration class not yet defined");
        return null;
    }

    private Condition<T> buildCondition(final Node son, final Map<String, Object> env) throws InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        return buildK(son, env, "it.unibo.alchemist.model.implementations.conditions.");
    }

    private TimeDistribution<T> buildTimeDist(final Node son, final Map<String, Object> env) throws InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        return buildK(son, env, "it.unibo.alchemist.model.implementations.timedistributions.");
    }

    private <K> K buildK(final Node son, final Map<String, Object> env, final String defaultPackage) throws InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        final NamedNodeMap attributes = son.getAttributes();
        final Node typeNode = attributes.getNamedItem(TYPE);
        String type;
        if (typeNode == null) {
            type = "";
        } else {
            type = typeNode.getNodeValue();
            type = type.contains(".") ? type : defaultPackage + type;
        }
        return coreOperations(env, son, type, random);
    }

    /**
     * Actually builds the environment given the AST built in the constructor.
     * 
     * @throws InstantiationException
     *             malformed XML
     * @throws IllegalAccessException
     *             malformed XML
     * @throws InvocationTargetException
     *             malformed XML
     * @throws ClassNotFoundException
     *             your classpath does not include all the classes you are using
     * @throws IOException
     *             if there is an error reading the file
     * @throws SAXException
     *             if the XML is not correctly formatted
     * @throws ParserConfigurationException
     *             should not happen.
     */
    private void buildEnvironment() throws InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, SAXException, IOException, ParserConfigurationException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document doc = builder.parse(xmlFile);
        L.debug("Starting processing");
        random = null;
        final Node root = doc.getFirstChild();
        if (root.getNodeName().equals("environment") && doc.getChildNodes().getLength() == 1) {
            final NamedNodeMap atts = root.getAttributes();
            String type = atts.getNamedItem(TYPE).getNodeValue();
            type = type.contains(".") ? type : "it.unibo.alchemist.model.implementations.environments." + type;
            result = coreOperations(new ConcurrentHashMap<String, Object>(), root, type, null);
            synchronized (result) {
                final Node nameNode = atts.getNamedItem(NAME);
                final String name = nameNode == null ? "" : nameNode.getNodeValue();
                final Map<String, Object> env = new ConcurrentHashMap<String, Object>();
                env.put("ENV", result);
                if (!name.equals("")) {
                    env.put(name, result);
                }
                final NodeList children = root.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    final Node son = children.item(i);
                    final String kind = son.getNodeName();
                    L.debug(kind);
                    if (!kind.equals(TEXT)) {
                        final Node sonNameAttr = son.getAttributes().getNamedItem(NAME);
                        final String sonName = sonNameAttr == null ? "" : sonNameAttr.getNodeValue();
                        Object sonInstance = null;
                        if (kind.equals("molecule")) {
                            sonInstance = buildMolecule(son, env);
                        } else if (kind.equals("concentration")) {
                            if (concentrationClass == null) {
                                setConcentration(son);
                            }
                        } else if (kind.equals("position")) {
                            if (positionClass == null) {
                                setPosition(son);
                            }
                        } else if (kind.equals("random")) {
                            setRandom(son, env);
                        } else if (kind.equals("linkingrule")) {
                            result.setLinkingRule(buildLinkingRule(son, env));
                        } else if (kind.equals("condition")) {
                            sonInstance = buildCondition(son, env);
                        } else if (kind.equals("action")) {
                            sonInstance = buildAction(son, env);
                        } else if (kind.equals("reaction")) {
                            sonInstance = buildReaction(son, env);
                        } else if (kind.equals("node")) {
                            final it.unibo.alchemist.model.interfaces.Node<T> node = buildNode(son, env);
                            final Position pos = buildPosition(son, env);
                            sonInstance = node;
                            result.addNode(node, pos);
                        } else if (kind.equals("time")) {
                            sonInstance = buildTime(son, env);
                        }
                        if (sonInstance != null) {
                            env.put(sonName, sonInstance);
                        }
                    }
                }
                /*
                 * This operation forces a reset to the random generator. It
                 * ensures that if the user reloads the same random seed she
                 * passed in the specification, the simulation will still be
                 * reproducible.
                 */
                random.setSeed(seed);
            }
        } else {
            L.error("XML does not contain one and one only environment.");
        }
    }

    private LinkingRule<T> buildLinkingRule(final Node rootLinkingRule, final Map<String, Object> env) throws InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        final NamedNodeMap attributes = rootLinkingRule.getAttributes();
        // final Node nameNode = attributes.getNamedItem(NAME);
        final String name = "LINKINGRULE";
        String type = attributes.getNamedItem(TYPE).getNodeValue();
        type = type.contains(".") ? type : LINKINGRULES_DEFAULT_PACKAGE + type;
        final LinkingRule<T> res = coreOperations(env, rootLinkingRule, type, random);
        env.put(name, res);
        return res;
    }

    private Molecule buildMolecule(final Node son, final Map<String, Object> env) throws InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        final NamedNodeMap attributes = son.getAttributes();
        String type = attributes.getNamedItem(TYPE).getNodeValue();
        type = type.contains(".") ? type : "it.unibo.alchemist.model.implementations.molecules." + type;
        return coreOperations(env, son, type, random);
    }

    private it.unibo.alchemist.model.interfaces.Node<T> buildNode(final Node rootNode, final Map<String, Object> env) throws InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        final NamedNodeMap attributes = rootNode.getAttributes();
        final Node nameNode = attributes.getNamedItem(NAME);
        final String name = nameNode == null ? "" : nameNode.getNodeValue();
        String type = attributes.getNamedItem(TYPE).getNodeValue();
        type = type.contains(".") ? type : "it.unibo.alchemist.model.implementations.nodes." + type;
        final it.unibo.alchemist.model.interfaces.Node<T> res = coreOperations(env, rootNode, type, random);
        if (res == null) {
            L.error("Failed to build " + type);
        }
        env.put(name, res);
        env.put("NODE", res);
        final NodeList children = rootNode.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final Node son = children.item(i);
            final String kind = son.getNodeName();
            if (!kind.equals(TEXT)) {
                final Node sonNameAttr = son.getAttributes().getNamedItem(NAME);
                final String sonName = sonNameAttr == null ? "" : sonNameAttr.getNodeValue();
                String objType = null;
                Object sonInstance = null;
                if (kind.equals("condition")) {
                    final Condition<T> cond = buildCondition(son, env);
                    sonInstance = cond;
                    objType = "CONDITION";
                } else if (kind.equals("action")) {
                    final Action<T> act = buildAction(son, env);
                    sonInstance = act;
                    objType = "ACTION";
                } else if (kind.equals("content")) {
                    final NamedNodeMap moleculesMap = son.getAttributes();
                    for (int j = 0; j < moleculesMap.getLength(); j++) {
                        final Node molNode = moleculesMap.item(j);
                        final String molName = molNode.getNodeName();
                        L.debug("checking molecule " + molName);
                        if (env.containsKey(molName)) {
                            L.debug(molName + " found");
                            final Object molObj = env.get(molName);
                            if (molObj instanceof Molecule) {
                                L.debug(molName + " matches in environment");
                                final Molecule mol = (Molecule) molObj;
                                final T conc = buildConcentration(molNode, env);
                                L.debug(molName + " concentration: " + conc);
                                sonInstance = conc;
                                res.setConcentration(mol, conc);
                            } else {
                                L.warn(molObj + "(class " + molObj.getClass().getCanonicalName() + " is not subclass of Molecule!");
                            }
                        } else {
                            L.warn("molecule " + molName + " is not yet defined.");
                        }
                    }
                } else if (kind.equals("reaction")) {
                    final Reaction<T> reaction = buildReaction(son, env);
                    res.addReaction(reaction);
                    sonInstance = reaction;
                    objType = "REACTION";
                } else if (kind.equals("time")) {
                    sonInstance = buildTime(son, env);
                } else if (kind.equals("timedistribution")) {
                    sonInstance = buildTimeDist(son, env);
                    objType = "TIMEDIST";
                }
                updateEnv(sonName, objType, sonInstance, env);
            }
        }
        env.remove(name);
        env.remove("NODE");
        return res;
    }

    private void updateEnv(final String name, final String curElem, final Object elem, final Map<String, Object> env) {
        if (elem != null) {
            if (name != null) {
                env.put(name, elem);
            }
            if (curElem != null) {
                env.put(curElem, elem);
            }
        }
    }

    private Position buildPosition(final Node son, final Map<String, Object> env) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        if (positionClass == null) {
            L.error("position class not yet defined.");
        } else {
            final NamedNodeMap attributes = son.getAttributes();
            final Node posNode = attributes.getNamedItem("position");
            if (posNode == null) {
                L.warn("a node has no position!");
            } else {
                final String args = posNode.getNodeValue();
                final StringTokenizer tk = new StringTokenizer(args, " ,;");
                final ArrayList<String> arguments = new ArrayList<String>();
                while (tk.hasMoreElements()) {
                    arguments.add(tk.nextToken());
                }
                arguments.trimToSize();
                final List<Constructor<Position>> list = unsafeExtractConstructors(positionClass);
                return tryToBuild(list, arguments, env, random);
            }
        }
        return null;
    }

    private Reaction<T> buildReaction(final Node rootReact, final Map<String, Object> env) throws InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        final NamedNodeMap attributes = rootReact.getAttributes();
        final Node nameNode = attributes.getNamedItem(NAME);
        final String name = nameNode == null ? "" : nameNode.getNodeValue();
        String type = attributes.getNamedItem(TYPE).getNodeValue();
        type = type.contains(".") ? type : REACTIONS_DEFAULT_PACKAGE + type;
        final Reaction<T> res = coreOperations(env, rootReact, type, random);
        if (!name.equals("")) {
            env.put(name, res);
        }
        env.put("REACTION", res);
        populateReaction(env, res, rootReact);
        return res;
    }

    private Time buildTime(final Node son, final Map<String, Object> env) throws InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        return buildTime(son, env, random);
    }

    private void populateReaction(final Map<String, Object> subenv, final Reaction<T> res, final Node rootReact) throws InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        final NodeList children = rootReact.getChildNodes();
        final ArrayList<Condition<T>> conditions = new ArrayList<Condition<T>>();
        final ArrayList<Action<T>> actions = new ArrayList<Action<T>>();
        for (int i = 0; i < children.getLength(); i++) {
            final Node son = children.item(i);
            final String kind = son.getNodeName();
            if (!kind.equals(TEXT)) {
                final Node sonNameAttr = son.getAttributes().getNamedItem(NAME);
                final String sonName = sonNameAttr == null ? "" : sonNameAttr.getNodeValue();
                Object sonInstance = null;
                if (kind.equals("condition")) {
                    final Condition<T> cond = buildCondition(son, subenv);
                    conditions.add(cond);
                    sonInstance = cond;
                } else if (kind.equals("action")) {
                    final Action<T> act = buildAction(son, subenv);
                    actions.add(act);
                    sonInstance = act;
                }
                if (sonInstance != null) {
                    subenv.put(sonName, sonInstance);
                }
            }
        }
        conditions.trimToSize();
        actions.trimToSize();
        if (!conditions.isEmpty()) {
            res.setConditions(conditions);
        }
        if (!actions.isEmpty()) {
            res.setActions(actions);
        }
    }

    private void setConcentration(final Node son) throws ClassNotFoundException {
        final NamedNodeMap attributes = son.getAttributes();
        String type = attributes.getNamedItem(TYPE).getNodeValue();
        type = type.contains(".") ? type : "it.unibo.alchemist.model.implementations.concentrations." + type;
        concentrationClass = Class.forName(type);
        L.debug("Concentration type set to " + concentrationClass);
    }

    private void setPosition(final Node son) throws ClassNotFoundException {
        final NamedNodeMap attributes = son.getAttributes();
        String type = attributes.getNamedItem(TYPE).getNodeValue();
        if (!type.contains(".")) {
            type = "it.unibo.alchemist.model.implementations.positions." + type;
        }
        positionClass = Class.forName(type);
        L.debug("Position type set to " + positionClass);
    }

    private void setRandom(final Node son, final Map<String, Object> env) throws InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        final NamedNodeMap attributes = son.getAttributes();
        String type = attributes.getNamedItem(TYPE).getNodeValue();
        String seed = attributes.getNamedItem("seed").getNodeValue();
        /*
         * This workaround ensures compatibility with pre-PVeStA integration
         * XMLs generated with the SAPERE DSL.
         */
        if (type.equals("cern.jet.random.engine.MersenneTwister")) {
            type = "org.apache.commons.math3.random.MersenneTwister";
        }
        type = type.contains(".") ? type : "org.apache.commons.math3.random." + type;
        seed = seed.equalsIgnoreCase("RANDOM") ? Integer.toString(internalRandom.nextInt()) : seed;
        final List<String> params = new ArrayList<>(1);
        params.add(seed);
        final Class<?> randomEngineClass = Class.forName(type);
        final List<Constructor<RandomGenerator>> consList = unsafeExtractConstructors(randomEngineClass);
        random = tryToBuild(consList, params, env, null);
        this.seed = Integer.parseInt(seed);
    }

    /**
     * Sets a new seed for the random engine. Thread unsafe. Handle with care.
     * 
     * @param seed
     *            the new random engine seed
     */
    public void setRandomGeneratorSeed(final int seed) {
        random.setSeed(seed);
    }


    private static Time buildTime(final Node son, final Map<String, Object> env, final RandomGenerator random) throws InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        final NamedNodeMap attributes = son.getAttributes();
        String type = attributes.getNamedItem(TYPE).getNodeValue();
        type = type.contains(".") ? type : "it.unibo.alchemist.model.implementations.times." + type;
        return (Time) coreOperations(env, son, type, random);
    }

    @SuppressWarnings("unchecked")
    private static <E> E coreOperations(final Map<String, Object> environment, final Node root, final String type, final RandomGenerator random) throws InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        final NamedNodeMap atts = root.getAttributes();
        final Node nameNode = atts.getNamedItem(NAME);
        final String name = nameNode == null ? "" : nameNode.getNodeValue();
        if (!name.equals("") && atts.getLength() == 1 && environment.containsKey(name)) {
            return (E) environment.get(name);
        }
        final Class<?> objClass = (Class<?>) Class.forName(type);
        final List<Constructor<E>> consList = unsafeExtractConstructors(objClass);
        final ArrayList<String> params = new ArrayList<String>();
        int index = 0;
        for (Node param = atts.getNamedItem("p0"); param != null; param = atts.getNamedItem("p" + (++index))) {
            params.add(param.getNodeValue());
        }
        params.trimToSize();
        final E res = tryToBuild(consList, params, environment, random);
        environment.put(name, res);
        return res;
    }

    private static Optional<Number> extractNumber(final String n) {
        long resl = 0;
        double resd = 0;
        boolean isDouble = false;
        try {
            resl = Long.parseLong(n);
        } catch (final NumberFormatException e) {
            try {
                isDouble = true;
                resd = Double.parseDouble(n);
            } catch (final NumberFormatException nested) {
                return Optional.empty();
            }
        }
        if (isDouble) {
            if (resd % 1 == 0 && resd < Long.MAX_VALUE) {
                return Optional.of((long) resd);
            }
            return Optional.of(resd);
        }
        return Optional.of(resl);
    }

    @SuppressWarnings("unchecked")
    private static Object parseAndCreate(final Class<?> clazz, final String val, final Map<String, Object> env, final RandomGenerator random) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        if (clazz.isAssignableFrom(RandomGenerator.class) && val.equalsIgnoreCase("random")) {
            L.debug("Random detected! Class " + clazz.getSimpleName() + ", param: " + val);
            if (random == null) {
                L.error("Random instatiation required, but RandomGenerator not yet defined.");
            }
            return random;
        }
        if (clazz.isPrimitive() || PrimitiveUtils.classIsWrapper(clazz)) {
            L.debug(val + " is a primitive or a wrapper: " + clazz);
            if ((clazz.isAssignableFrom(Boolean.TYPE) || clazz.isAssignableFrom(Boolean.class)) && (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("false"))) {
                return Boolean.parseBoolean(val);
            }
            /*
             * If Number is in clazz's hierarchy
             */
            if (PrimitiveUtils.classIsNumber(clazz)) {
                final Optional<Number> num = extractNumber(val);
                if (num.isPresent()) {
                    final Optional<Number> castNum = PrimitiveUtils.castIfNeeded(clazz, num.get());
                    /*
                     * If method requires Object or unsupported Number, return
                     * what was parsed.
                     */
                    return castNum.orElse(num.get());
                }
            }
            if (Character.TYPE.equals(clazz) || Character.class.equals(clazz)) {
                return val.charAt(0);
            }
        }
        if (List.class.isAssignableFrom(clazz) && val.startsWith("[") && val.endsWith("]")) {
            final List<Constructor<List<?>>> l = unsafeExtractConstructors(clazz);
            @SuppressWarnings("rawtypes")
            final List list = tryToBuild(l, new ArrayList<String>(0), env, random);
            final StringTokenizer strt = new StringTokenizer(val.substring(1, val.length() - 1), ",; ");
            while (strt.hasMoreTokens()) {
                final String sub = strt.nextToken();
                final Object o = tryToParse(sub, env, random);
                if (o == null) {
                    L.debug("WARNING: list elemnt skipped: " + sub);
                } else {
                    list.add(o);
                }
            }
            return list;
        }
        L.debug(val + " is not a primitive: " + clazz + ". Searching it in the environment...");
        final Object o = env.get(val);
        if (o != null && clazz.isInstance(o)) {
            return o;
        }
        if (Time.class.isAssignableFrom(clazz)) {
            return new DoubleTime(Double.parseDouble(val));
        }
        if (clazz.isAssignableFrom(String.class)) {
            L.debug("String detected! Passing " + val + " back.");
            return val;
        }
        L.debug(val + " not found or class not compatible, unable to go further.");
        return null;
    }

    private static <E> E tryToBuild(final List<Constructor<E>> consList, final List<String> params, final Map<String, Object> env, final RandomGenerator random) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        for (final Constructor<E> c : consList) {
            L.debug("Trying to build with constructor " + c);
            final Class<?>[] args = c.getParameterTypes();
            if (args.length == params.size()) {
                L.debug("Parameters number matches (" + args.length + ").");
                final Object[] finalArgs = new Object[args.length];
                int i = 0;
                boolean success = true;
                for (; i < args.length && success; i++) {
                    final String paramVal = params.get(i);
                    final Class<?> paramClass = args[i];
                    finalArgs[i] = parseAndCreate(paramClass, paramVal, env, random);
                    if (!paramVal.equals("null") && finalArgs[i] == null) {
                        L.debug("Unable to use this constructor.");
                        success = false;
                    }
                }
                if (success && i == args.length) {
                    final E result = c.newInstance(finalArgs);
                    // L.debug("Created object " + result);
                    return result;
                }
            }
        }
        throw new IllegalArgumentException("no compatible constructor find for " + params);
    }

    private static Object tryToParse(final String val, final Map<String, Object> env, final RandomGenerator random) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        for (final Class<?> clazz : TYPES) {
            final Object result = parseAndCreate(clazz, val, env, random);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <Klazz> List<Constructor<Klazz>> unsafeExtractConstructors(final Class<?> clazz) {
        final Constructor<?>[] constructors = clazz.getConstructors();
        final List<Constructor<Klazz>> list = new ArrayList<Constructor<Klazz>>(constructors.length);
        for (final Constructor<?> c : constructors) {
            list.add((Constructor<Klazz>) c);
        }
        return list;
    }

    private static <T> Future<Result<T>> build(final EnvironmentBuilder<T> builder) {
        final ExecutorService ex = Executors.newSingleThreadExecutor();
        final Future<Result<T>> result = ex.submit(() -> {
            builder.buildEnvironment();
            return Result.build(builder.result, builder.random);
        });
        ex.shutdown();
        return result;
    }

    /**
     * @param xml
     *            the stream to process
     * @param <T>
     *            the concentration type
     * 
     * @return a {@link Future} result containing an {@link Environment}
     */
    public static <T> Future<Result<T>> build(final InputStream xml) {
        return build(new EnvironmentBuilder<>(xml));
    }

    /**
     * @param <T>
     */
    public static final class Result<T> implements Serializable {

        private static final long serialVersionUID = -8733706297447819226L;
        private final Environment<T> env;
        @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "All the random engines provided by Apache are Serializable")
        private final RandomGenerator rng;

        private Result(final Environment<T> environment, final RandomGenerator random) {
            env = environment;
            rng = random;
        }

        /**
         * @return the {@link Environment}
         */
        public Environment<T> getEnvironment() {
            return env;
        }

        /**
         * @return the {@link RandomGenerator} used internally
         */
        public RandomGenerator getRandomGenerator() {
            return rng;
        }

        private static <T> Result<T> build(final Environment<T> environment, final RandomGenerator random) {
            return new Result<>(environment, random);
        }

    }

}
