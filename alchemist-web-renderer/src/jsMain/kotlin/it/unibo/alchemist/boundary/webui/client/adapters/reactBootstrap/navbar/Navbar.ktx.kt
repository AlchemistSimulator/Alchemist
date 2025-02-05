/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.client.adapters.reactBootstrap.navbar

import react.FC
import react.PropsWithChildren

/**
 * Navbar.Brand component.
 * Note: an explicit cast is required here, as the original Javascript structure is dynamic.
 */
@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
val NavbarBrand: FC<PropsWithChildren> = Navbar.asDynamic().Brand as FC<PropsWithChildren>
