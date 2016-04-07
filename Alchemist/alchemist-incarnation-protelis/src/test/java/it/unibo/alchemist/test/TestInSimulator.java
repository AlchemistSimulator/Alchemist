/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.core.implementations.Engine.StateCommand;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.language.EnvironmentBuilder;
import it.unibo.alchemist.language.protelis.ProtelisDSLStandaloneSetup;
import it.unibo.alchemist.model.implementations.actions.RunProtelisProgram;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.Charsets;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.generator.IGenerator;
import org.eclipse.xtext.generator.InMemoryFileSystemAccess;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.junit.Test;
import org.protelis.lang.datatype.Field;
import org.xml.sax.SAXException;

import com.google.inject.Injector;


/**
 */
public class TestInSimulator {

    private static final XtextResourceSet XTEXT;
    private static final Injector INJECTOR;
    private static final int LONG_SIMULATION_FINAL_TIME = 30000;

    static {
        new org.eclipse.emf.mwe.utils.StandaloneSetup().setPlatformUri(".");
        INJECTOR = new ProtelisDSLStandaloneSetup().createInjectorAndDoEMFRegistration();
        XTEXT = INJECTOR.getInstance(XtextResourceSet.class);
        XTEXT.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
    }

    /**
     * @throws Exception in case of failure
     */
    @Test
    public void testSimple01() throws Exception { 
        runSimulation("simple01.psim", 2, checkProgramValueOnAll(v -> assertEquals(1.0, v)));
    }

    /**
     * @throws Exception in case of failure
     */
    @Test
    public void testNbr01() throws Exception { 
        runSimulation("nbr01.psim", 2, checkProgramValueOnAll(v -> {
            assertTrue(v instanceof Field);
            final Field res = (Field) v;
            res.valIterator().forEach(fval -> assertEquals(1.0, fval));
        }));
    }

    /**
     * @throws Exception in case of failure
     */
    @Test
    public void testNbr02() throws Exception { 
        runSimulation("nbr02.psim", LONG_SIMULATION_FINAL_TIME, env -> {
            final double val = (Double) env.getNodes().stream()
                    .flatMap(n -> n.getContents().entrySet().stream())
                    .filter(e -> e.getKey() instanceof RunProtelisProgram)
                    .findAny().get().getValue();
            checkProgramValueOnAll(v -> assertEquals(val, v)).accept(env);
        });
    }

    /**
     * @throws Exception in case of failure
     */
    @Test
    public void testDistanceTo() throws Exception { 
        runSimulation("distanceTo.psim", LONG_SIMULATION_FINAL_TIME);
    }

    @SafeVarargs
    private static <T> void runSimulation(final String relativeFilePath, final double finalTime, final Consumer<Environment<Object>>... checkProcedures) throws InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, SAXException, IOException, ParserConfigurationException, InterruptedException, ExecutionException  {
        final Resource res = XTEXT.getResource(URI.createURI("classpath:/simulations/" + relativeFilePath), true);
        final IGenerator generator = INJECTOR.getInstance(IGenerator.class);
        final InMemoryFileSystemAccess fsa = INJECTOR.getInstance(InMemoryFileSystemAccess.class);
        generator.doGenerate(res, fsa);
        final Collection<CharSequence> files = fsa.getTextFiles().values();
        if (files.size() != 1) {
            fail();
        }
        final ByteArrayInputStream strIS = new ByteArrayInputStream(files.stream().findFirst().get().toString().getBytes(Charsets.UTF_8));
        final Environment<Object> env = EnvironmentBuilder.build(strIS).get().getEnvironment();
        final Simulation<Object> sim = new Engine<>(env, new DoubleTime(finalTime));
        sim.addCommand(new StateCommand<>().run().build());
        /*
         * Use this thread: intercepts failures.
         */
        sim.run();
        Arrays.stream(checkProcedures).forEachOrdered(p -> p.accept(env));
    }

    private static <T> Consumer<Environment<T>> checkOnNodes(final Consumer<Node<T>> proc) {
        return env -> env.forEach(n -> {
            proc.accept(n);
        });
    }

    private static <T> Consumer<Environment<T>> checkProgramValueOnAll(final Consumer<Object> proc) {
        return checkOnNodes(checkProtelisProgramValue(proc));
    }

    private static <T> Consumer<Node<T>> checkProtelisProgramValue(final Consumer<Object> check) {
        return n -> n.forEach(r -> {
            r.getActions().parallelStream()
                .filter(a -> a instanceof RunProtelisProgram)
                .forEach(a -> {
                    check.accept(n.getConcentration((RunProtelisProgram) a));
                });
            });
    }

}
