/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.orchid

import com.eden.orchid.api.compilers.TemplateFunction
import com.eden.orchid.api.compilers.TemplateTag
import com.eden.orchid.api.registration.OrchidModule

class AlchemistModule : OrchidModule() {

    override fun configure() {
        addToSet(TemplateFunction::class.java, ShortenPackage::class.java, Snippet::class.java)
        addToSet(TemplateTag::class.java, GistIt::class.java)
    }
}
