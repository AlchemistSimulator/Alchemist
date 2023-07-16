/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.server.utility

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.boundary.webui.common.model.surrogate.MoleculeSurrogate
import it.unibo.alchemist.boundary.webui.server.surrogates.utility.toMoleculeSurrogate
import it.unibo.alchemist.model.Molecule

class ToMoleculeSurrogateTest : StringSpec({

    val molecule = Molecule { "Alchemist Molecule" }

    "ToMoleculeSurrogate should map a Molecule to a MoleculeSurrogate" {
        checkToMoleculeSurrogate(molecule, molecule.toMoleculeSurrogate())
    }
})

fun checkToMoleculeSurrogate(molecule: Molecule, moleculeSurrogate: MoleculeSurrogate) {
    molecule.name shouldBe moleculeSurrogate.name
}
