/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.nodes;

import com.google.common.collect.ImmutableSet;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.ProtelisIncarnation;
import it.unibo.alchemist.model.implementations.BaseProtelisCapability;
import it.unibo.alchemist.model.implementations.actions.RunProtelisProgram;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Time;
import it.unibo.alchemist.model.interfaces.capabilities.ProtelisCapability;
import it.unibo.alchemist.protelis.AlchemistNetworkManager;
import org.protelis.lang.datatype.DeviceUID;
import org.protelis.lang.datatype.Field;
import org.protelis.vm.ExecutionEnvironment;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 *
 * @param <P> Position type
 */
public final class ProtelisNode<P extends Position<? extends P>>
        extends AbstractNode<Object>
        implements DeviceUID, ExecutionEnvironment {

    private static final long serialVersionUID = 7411790948884770553L;
    private final Map<RunProtelisProgram<?>, AlchemistNetworkManager> networkManagers = new LinkedHashMap<>();
    private final Environment<Object, P> environment;

    /**
     * Builds a new {@link ProtelisNode}.
     *
     * @param environment
     *            the environment
     */
    @SuppressWarnings("rawtypes")
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "This is intentional")
    public ProtelisNode(final Environment<Object, P> environment) {
        super(environment);
        this.environment = environment;
        this.addCapability(new BaseProtelisCapability((ProtelisIncarnation<?>) environment.getIncarnation(), this));
    }

    @Override
    protected Object createT() {
        return null;
    }

    @Override
    public String toString() {
        return Long.toString(getId());
    }

    /**
     * Adds a new {@link AlchemistNetworkManager}.
     *
     * @param program
     *            the {@link RunProtelisProgram}
     * @param networkManager
     *            the {@link AlchemistNetworkManager}
     */
    public void addNetworkManger(final RunProtelisProgram<?> program, final AlchemistNetworkManager networkManager) {
        this.asCapability(ProtelisCapability.class).addNetworkManger(program, networkManager);
    }

    /**
     * @param program
     *            the {@link RunProtelisProgram}
     * @return the {@link AlchemistNetworkManager} for this specific
     *         {@link RunProtelisProgram}
     */
    public AlchemistNetworkManager getNetworkManager(final RunProtelisProgram<?> program) {
        Objects.requireNonNull(program);
        return this.asCapability(ProtelisCapability.class).getNetworkManager(program);
    }

    /**
     * @return all the {@link AlchemistNetworkManager} in this node
     */
    public Map<RunProtelisProgram<?>, AlchemistNetworkManager> getNetworkManagers() {
        return this.asCapability(ProtelisCapability.class).getNetworkManagers();
    }

    private static <P extends Position<P>> Molecule makeMol(final String id) {
        return new ProtelisIncarnation<P>().createMolecule(id);
    }

    @Override
    public boolean has(final String id) {
        return this.asCapability(ProtelisCapability.class).has(id);
    }

    @Override
    public Object get(final String id) {
        return this.asCapability(ProtelisCapability.class).get(id);
    }

    @Override
    public Object get(final String id, final Object defaultValue) {
        return this.asCapability(ProtelisCapability.class).get(id, defaultValue);
    }

    @Override
    public boolean put(final String id, final Object v) {
        return this.asCapability(ProtelisCapability.class).put(id, v);
    }

    /**
     * Writes a Map representation of the Field on the environment.
     *
     * @param id variable name
     * @param v the {@link Field}
     * @return true
     */
    public boolean putField(final String id, final Field v) {
        return this.asCapability(ProtelisCapability.class).putField(id, v);
    }

    @Override
    public Object remove(final String id) {
        return this.asCapability(ProtelisCapability.class).remove(id);
    }

    @Override
    public void commit() {
    }

    @Override
    public void setup() {
    }

    @Override
    public ProtelisNode<P> cloneNode(final Time currentTime) {
        final ProtelisNode<P> result = new ProtelisNode<>(environment);
        getContents().forEach(result::setConcentration);
        getReactions().forEach(r -> result.addReaction(r.cloneOnNewNode(result, currentTime)));
        return result;
    }

    @Override
    public Set<String> keySet() {
        return this.asCapability(ProtelisCapability.class).keySet();
    }
}
