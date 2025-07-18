/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.molecules;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import it.unibo.alchemist.model.Molecule;

import javax.annotation.Nonnull;
import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 *         Simple implementation of Molecule.
 */
public class SimpleMolecule implements Molecule {

    @Serial
    private static final long serialVersionUID = 1L;

    private int hash32;
    private final String n;

    /**
     * @param name
     *            the molecule name
     */
    public SimpleMolecule(@Nonnull final CharSequence name) {
        this(Objects.requireNonNull(name).toString().intern());
    }

    /**
     * @param name
     *            the molecule name
     */
    public SimpleMolecule(@Nonnull final String name) {
        this.n = Objects.requireNonNull(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && getClass() == obj.getClass()) {
            final SimpleMolecule other = (SimpleMolecule) obj;
            return hashCode() == obj.hashCode() && n.equals(other.n);
        }
        return false;
    }

    @Override
    public final String getName() {
        return n;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        if (hash32 == 0) {
            final HashCode hashCode = Hashing.murmur3_32_fixed().hashString(n, StandardCharsets.UTF_16);
            hash32 = hashCode.asInt();
        }
        return hash32;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return n;
    }

}
