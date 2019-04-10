/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

/**
 * 
 */
package it.unibo.alchemist.model.implementations.molecules;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;

import it.unibo.alchemist.model.interfaces.Molecule;


/**
 *         Simple implementation of Molecule. Ids are generated through a simple
 *         Singleton Pattern, no thread safeness is provided.
 * 
 */
public class SimpleMolecule implements Molecule {

    private static final long serialVersionUID = 1L;

    private byte[] hash;
    private int hash32;
    private long hash64;
    private final CharSequence n;

    /**
     * @param name
     *            the molecule name
     */
    public SimpleMolecule(final CharSequence name) {
        this.n = name;
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
            if (n == other.n) {
                return true;
            }
            return hashCode() == other.hashCode() && getId() == other.getId() && Arrays.equals(hash, other.hash);
        }
        return false;
    }

    @Override
    public final long getId() {
        initHash();
        return hash64;
    }

    @Override
    public final String getName() {
        return n.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        initHash();
        return hash32;
    }

    private void initHash() {
        if (hash == null) {
            final HashCode hashCode = Hashing.murmur3_128().hashString(n, StandardCharsets.UTF_8);
            hash32 = hashCode.asInt();
            hash64 = hashCode.asLong();
            hash = hashCode.asBytes();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return n.toString();
    }

}
