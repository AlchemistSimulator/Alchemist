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
import it.unibo.alchemist.model.implementations.actions.RunProtelisProgram;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Time;
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
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "This is intentional")
    public ProtelisNode(final Environment<Object, P> environment) {
        super(environment);
        this.environment = environment;
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
        networkManagers.put(program, networkManager);
    }

    /**
     * @param program
     *            the {@link RunProtelisProgram}
     * @return the {@link AlchemistNetworkManager} for this specific
     *         {@link RunProtelisProgram}
     */
    public AlchemistNetworkManager getNetworkManager(final RunProtelisProgram<?> program) {
        Objects.requireNonNull(program);
        return networkManagers.get(program);
    }

    /**
     * @return all the {@link AlchemistNetworkManager} in this node
     */
    public Map<RunProtelisProgram<?>, AlchemistNetworkManager> getNetworkManagers() {
        return Collections.unmodifiableMap(networkManagers);
    }

    private static <P extends Position<P>> Molecule makeMol(final String id) {
        return new ProtelisIncarnation<P>().createMolecule(id);
    }

    @Override
    public boolean has(final String id) {
        return contains(makeMol(id));
    }

    @Override
    public Object get(final String id) {
        final Molecule mid = makeMol(id);
        return Optional.ofNullable(getConcentration(mid))
            .orElse(environment.getLayer(mid)
                .map(it -> it.getValue(environment.getPosition(this)))
                .orElse(null)
            );
    }

    @Override
    public Object get(final String id, final Object defaultValue) {
        return Optional.ofNullable(get(id)).orElse(defaultValue);
    }

    @Override
    public boolean put(final String id, final Object v) {
        setConcentration(makeMol(id), v);
        return true;
    }

    /**
     * Writes a Map representation of the Field on the environment.
     *
     * @param id variable name
     * @param v the {@link Field}
     * @return true
     */
    public boolean putField(final String id, final Field v) {
        setConcentration(makeMol(id), v.toMap());
        return true;
    }

    @Override
    public Object remove(final String id) {
        final Object res = get(id);
        removeConcentration(makeMol(id));
        return res;
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
        return getContents().keySet().stream()
                .map(Molecule::getName)
                .collect(ImmutableSet.toImmutableSet());
    }
}
